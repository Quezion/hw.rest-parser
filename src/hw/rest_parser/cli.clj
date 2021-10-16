(ns hw.rest-parser.cli
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [environ.core :refer [env]]
            [malli.core :as m]
            [malli.error :as me])
  (:import [java.text SimpleDateFormat])
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
  "Allowed line separators in correct sequence for line-matching"
  [#","
   #"\|"
   #" "])

(def match-allowed-separator
  "Matches line against all allowed separators & return first match"
  (partial line-match-separator allowed-separators-regexp))

(defn line->separator
  "Returns one of known valid string separators or nil if none could be determined"
  [line]
  (match-allowed-separator line))

(defn parse-datestring
  "Parses date in expected format of MM/DD/YYYY"
  [date]
  (-> (SimpleDateFormat. "MM/dd/yyyy")
      (.parse date)))

(defn line->record
  [separator line]
  (as-> line <>
    (str/split <> separator)
    (map str/trim <>)
    (zipmap row-headers <>)
    (update <> :DateOfBirth parse-datestring)))

(defn load-record
  "Loads record at target filepath into memory.
  A more graceful implementation might hold files open for the entire runtime,
  but side-effect complexity becomes messy without care & is better skipped for a sample app"
  [filepath]
  ;; Assume no header lines at top of input files
  (with-open [rdr (io/reader filepath)]
    (let [lines (line-seq rdr)
          separator (some-> (first lines)
                            (line->separator))]
      (when separator
        (->> lines
             (map (partial line->record separator))
             doall))))) ;; Force in-memory realization now before file closes

(def env-spec [:map [:filepaths [:string {:min 1}]
                     :column-length [:string {:optional true
                                              :min 1}]]])

(defn validate-env [] (m/validate env-spec env))
(defn explain-env [] (m/explain env-spec env))

(defn string-compare-lowercase-asc [a b] (compare (str/lower-case a) (str/lower-case b)))
(defn string-compare-lowercase-desc [a b] (compare (str/lower-case b) (str/lower-case a)))

(def color-lastname [[:FavoriteColor string-compare-lowercase-asc]
                     [:LastName string-compare-lowercase-asc]])

(def dob [[:DateOfBirth compare]])

(def lastname-desc [[:LastName string-compare-lowercase-desc]])

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

(defn run
  [{:keys [filepaths]}]
  (as-> filepaths <>
    (str/split filepaths #",")
    (map load-record <>)
    (flatten <>)))

(def record-spec [:map [:LastName [:string {:min 1}]
                        :FirstName [:string {:min 1}]
                        :Email [:string {:min 5}]
                        :FavoriteColor [:string {:min 4 :max 40}]
                        :DateOfBirth inst?]])

;; Register `run` with Malli. This definition reads "takes env-spec & returns record"
(m/=> run
      [:=> [:cat env-spec]
       [:sequential record-spec]])

(def output-views [color-lastname
                   dob
                   lastname-desc])

(defn trunc
  "This is unexpectedly missing in clojure.string"
  [s n]
  (subs s 0 (min (count s) n)))

(defn force-str-to-length
  "Forces string to specific width, padding with empty spaces as necessary.
  Longer strings are truncated with trailing ..."
  [length s]
  (let [difference (- length (count s))]
    (cond
      (pos? difference) (apply str s (repeat difference " "))
      (neg? difference) (str (trunc s (- length 3)) "...")
      :else s)))

(defn render-view-cast-to-string
  "Casts any record value to string"
  [x]
  (cond
    (string? x) x
    (inst? x) (.format (SimpleDateFormat. "M/d/yyyy") x)
    :else (throw (Exception. (str "Invalid value <x,typeOf(x)> " x (type x))))))

;; Little kludge to derive this from env. In a larger project,
;; we'd parse env into a final map, validate it, & expose it via state management lib
(defn env-column-length
  []
  (try
    (if (integer? (:column-length environ.core/env))
      (:column-length environ.core/env)
      (Integer/parseInt (:column-length environ.core/env)))
    (catch Exception _
      16)))

(defn render-view-data
  "Given sequence of maps with common keys, renders into ASCII table of minimum width"
  ([mapseq]
   (render-view-data mapseq nil))
  ([mapseq {:keys [column-length]
            :or {column-length (env-column-length)}}]
   (let [map-keys (keys (first mapseq))
         labels (->> (map name map-keys)
                     (map #(-> (force-str-to-length column-length %)
                               (str " ")))
                     (str/join)
                     (drop-last)
                     (str/join ""))
         lines (->> (map vals mapseq)
                    (map #(map (fn [s]
                                 (str (->> (render-view-cast-to-string s)
                                           (force-str-to-length column-length))
                                      " ")) %))
                    (map (comp str/join drop-last str/join))
                    (str/join "\n"))]
     (str labels "\n"
          (str/join (repeat (count labels) "="))
          "\n"
          lines))))

(defn run-render-views
  [env]
  (as-> (run (merge environ.core/env env)) <>
    (repeat <>)
    (map sort-records <> output-views)
    (map render-view-data <>)))

(defn -main
  [& _]
  ;; You could accept a file via Java program ARGS... but for sake of simplicity, just use $env
  (if-not (validate-env)
    (->> (explain-env)
         (me/humanize)
         (prn-str)
         (str "[ERROR] Problems below with input $env variables. Please fix & retry\n")
         (println))
    (->> (run-render-views env)
         (str/join "\n\n")
         (println))))

;; This technically belongs in user.clj or test, but including here to make usage obvious
(defmacro with-env
  [env & body]
  `(with-redefs [environ.core/env ~env]
     ~@body))

(comment ;; Try at a REPL. Modify to input different files
  (with-env {:filepaths "data/basic_record.csv,data/basic_record.psv,data/basic_record.ssv"
             :column-length 12}
    (-main)))
