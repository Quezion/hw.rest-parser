(ns hw.rest-parser.app
  (:require [aleph.http :as http]
            [clojure.string :as str]
            [environ.core :refer [env]]
            [hw.rest-parser.cli :as cli]
            [hw.rest-parser.server :as server]
            [malli.core :as m]
            [malli.error :as me]
            [mount.core :refer [defstate] :as mount])
  (:gen-class))

(def modes #{"cli"
             "server"})

(def default-mode "server")

(def env-spec [:map [:mode {:optional true}
                     (into [:enum] modes)]])

(defn validate-env [] (m/validate env-spec env))
(defn explain-env [] (m/explain env-spec env))

(m/=> run
      [:=> [:cat (into [:enum] modes)]
       :any])

(defn run
  [mode]
  (cond
    (= mode "cli") (cli/-main)
    (= mode "server") (server/-main)
    :else (throw (Exception. (str "Invalid mode " mode)))))

(defn -main
  [& _]
  (if-not (validate-env)
    (->> (explain-env)
         (me/humanize)
         (prn-str)
         (str "[ERROR] Problems below with input $env variables. Please fix & retry\n")
         (println))
    (->> (or (:mode env) default-mode)
         (run))))
