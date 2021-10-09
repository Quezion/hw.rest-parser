(ns hw.rest-parser
  (:require [clojure.string :as str]
            [environ.core :refer [env]]
            [malli.core :as m]
            [malli.error :as me])
  (:gen-class))

;; For beginning -- we'll make a program that loads a simple hand-gen CSV record & prints it

(def row-headers [:LastName :FirstName :Email :FavoriteColor :DateOfBirth])

(defn split-row-csv
  [row]
  (str/split row #",\s"))

(defn load-record
  "Loads record at target filepath into memory.
  A more graceful implementation might hold files open for the entire runtime,
  but side-effect complexity becomes messy without care & is better skipped for a sample app"
  [filepath]
  ;; For now, assume no header line & that file format is correct
  (with-open [rdr (clojure.java.io/reader filepath)]
    (->> (line-seq rdr)
         (map (comp (partial zipmap row-headers)
                    split-row-csv))
         doall))) ;; Force in-memory realization now before file closes

(def env-spec [:map [:filepath [:string {:min 1}]]])

(defn validate-env [] (m/validate env-spec env))
(defn explain-env [] (m/explain env-spec env))

(defn -main
  [& _]
  ;; You could accept a file via Java program ARGS... but for sake of simplicity, just use $env
  (if-not (validate-env)
    (->> (explain-env)
         (me/humanize)
         (prn-str)
         (str "[ERROR] Problems below with input $env variables. Please fix & retry\n")
         (println))
    (let [record (-> (:filepath env)
                     (load-record))]
      (prn record))))

;; This technically belongs in dev.clj or test, but including here to make usage obvious
(defmacro with-env
  [env & body]
  `(with-redefs [environ.core/env ~env]
     ~@body))

(comment ;; Try at a REPL. Modify to input different files
  (with-env {:filepath "data/basic_record.csv"}
    (-main)))
