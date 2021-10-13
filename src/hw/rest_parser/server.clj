(ns hw.rest-parser.server
  (:require [clojure.string :as str]
            [environ.core :refer [env]]
            [malli.core :as m]
            [malli.error :as me])
  (:import [java.text SimpleDateFormat])
  (:gen-class))

(def env-spec [:map [:webserver-port [:string {:min 1}]]])

(defn validate-env [] (m/validate env-spec env))
(defn explain-env [] (m/explain env-spec env))

(defn -main
  [& _]
  (if-not (validate-env)
    (->> (explain-env)
         (me/humanize)
         (prn-str)
         (str "[ERROR] Problems below with input $env variables. Please fix & retry\n")
         (println))
    (println "TODO: init server & impl endpoints")))
