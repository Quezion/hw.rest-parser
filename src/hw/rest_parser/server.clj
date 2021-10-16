(ns hw.rest-parser.server
  (:require [aleph.http :as http]
            [byte-streams :as bs]
            [clojure.string :as str]
            [environ.core :refer [env]]
            [malli.core :as m]
            [malli.error :as me]
            [mount.core :refer [defstate] :as mount])
  (:import [java.text SimpleDateFormat])
  (:gen-class))

(def defaults {:host "localhost"
               :port 3000})

(defstate webserver-host
  :start (if (seq (:webserver-host env))
           (:webserver-host env)
           (:host defaults)))

(defstate webserver-port
  :start (try (Integer/parseInt (str (:webserver-port env)))
              (catch Exception _
                (:port defaults))))

(defn handler [req]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "hello!"})

(defstate server
  :start (do (println "[WEB] Starting server on host:port"
                      (str webserver-host ":" webserver-port))
             (http/start-server handler {:host webserver-host
                                         :port webserver-port}))
  :stop (.close server))

(def env-spec [:map
               [:webserver-host {:min 1
                                 :optional true} :string]
               [:webserver-port {:min 1
                                 :optional true} :string]])

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
    (do (mount/start)
        (println "[SUCCESS] App initialized"))))
