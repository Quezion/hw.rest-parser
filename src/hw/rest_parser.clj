(ns hw.rest-parser
  (:require [clojure.string :as str]
            [environ.core :refer [env]]
            [malli.core :as m]
            [malli.error :as me])
  (:gen-class))

(def row-headers [:LastName :FirstName :Email :FavoriteColor :DateOfBirth])

(defn line-match-separator
  "Returns regex for separator of string line, or nil if line doesn't have consistent separator.
  separators - set of string separators that could be between values
  line - string with "
  [separators line]
  (->> (map #(when (= (count (re-seq % line))
                      (- (count row-headers) 1))
               %)
            separators)
       (filter some?)
       first))

(def allowed-separators-regexp
  #{#","
    #"\|"
    #" "})

(def match-allowed-separator
  "Matches line against all allowed separators & return first match"
  (partial line-match-separator allowed-separators-regexp))

(defn line->separator
  "Returns one of known valid string separators or nil if none could be determined"
  [line]
  (match-allowed-separator line))

(defn load-record
  "Loads record at target filepath into memory.
  A more graceful implementation might hold files open for the entire runtime,
  but side-effect complexity becomes messy without care & is better skipped for a sample app"
  [filepath]
  ;; Assume no header line
  (with-open [rdr (clojure.java.io/reader filepath)]
    (let [lines (line-seq rdr)
          separator (some-> (first lines)
                            (line->separator))]
      (when separator
        (->> lines
             (map (comp #(zipmap row-headers %)
                        #(map str/trim %)
                        #(str/split % separator)))
             doall))))) ;; Force in-memory realization now before file closes

(def env-spec [:map [:filepaths [:string {:min 1}]]])

(defn validate-env [] (m/validate env-spec env))
(defn explain-env [] (m/explain env-spec env))

(defn string-compare-lowercase-asc [a b] (compare (str/lower-case a) (str/lower-case b)))
(defn string-compare-lowercase-desc [a b] (compare (str/lower-case b) (str/lower-case a)))

(def color-lastname [[:FavoriteColor string-compare-lowercase-asc]
                     [:LastName string-compare-lowercase-asc]])

;; TODO: TODO: validate into datetime when parsing in records
;; (def dob [[:DateOfBirth compare]])

(def lastname-desc [[:LastName string-compare-lowercase-desc]])

(def output-views [color-lastname
                   ;;dob
                   lastname-desc])

(defn sort-records
  "Returns sorted list of input records based on sort-definition.
  records - seq of maps representing parsed records
  sort-definition - vec of vecs, where each 2-element subvec is keyfn & comparator"
  [records sort-definition]
  (let [sort-keys (map first sort-definition)
        keyfn (apply juxt sort-keys)
        sort-comparators (map second sort-definition)
        comparator (fn [a b]
                     (loop [[comparator & comp-rest] sort-comparators
                            [a1 & a-rest] a
                            [b1 & b-rest] b]
                       (let [result (comparator a b)]
                         (if (and (zero? result)
                                  (seq sort-comparators))
                           (recur comp-rest a-rest b-rest)
                           result))))]
    (sort-by keyfn comparator records)))

;; Little REPL test here that'll move to test namespace when we add DOB parsing in next commit
(comment
  (def test-records [{:FavoriteColor "blue" :LastName "Aardvark"}
                     {:FavoriteColor "red" :LastName "Bombadill"}
                     {:FavoriteColor "blue" :LastName "Parker"}
                     {:FavoriteColor "pink" :LastName "Stelsior"}])
  (sort-records test-records lastname-desc)
  (sort-records test-records color-lastname))

(defn run
  [{:keys [filepaths]}]
  (as-> filepaths <>
    (str/split filepaths #",")
    (map load-record <>)
    (flatten <>)))

(defn -main
  [& _]
  ;; You could accept a file via Java program ARGS... but for sake of simplicity, just use $env
  (if-not (validate-env)
    (->> (explain-env)
         (me/humanize)
         (prn-str)
         (str "[ERROR] Problems below with input $env variables. Please fix & retry\n")
         (println))
    (->> (run env)
         (prn))))

;; This technically belongs in dev.clj or test, but including here to make usage obvious
(defmacro with-env
  [env & body]
  `(with-redefs [environ.core/env ~env]
     ~@body))

(comment ;; Try at a REPL. Modify to input different files
  (with-env {:filepaths "data/basic_record.csv,data/basic_record.psv,data/basic_record.ssv"}
    (-main)))
