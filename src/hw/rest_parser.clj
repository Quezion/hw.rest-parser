(ns hw.rest-parser
  (:require [clojure.string :as str]
            [environ.core :refer [env]]
            [malli.core :as m]
            [malli.error :as me])
  (:gen-class))

(def row-headers [:LastName :FirstName :Email :FavoriteColor :DateOfBirth])

(defn line-matches-separator?
  "Whether string line of file matches one of the input separators for the entire line.
  separators - set of string separators that could be between values"
  [separators line]
  (->> (map #(when (= (count (re-seq % line))
                      (- (count row-headers) 1))
               %)
            separators)
       (filter some?)
       first))

(def match-allowed-separator (partial line-matches-separator? #{#","
                                                                #"\|"
                                                                #" "}))

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
