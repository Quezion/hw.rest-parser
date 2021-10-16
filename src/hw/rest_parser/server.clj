(ns hw.rest-parser.server
  (:require [aleph.http :as http]
            [bidi.bidi :as bidi]
            [bidi.ring :as bidi-ring]
            [byte-streams :as bs]
            [clojure.string :as str]
            [environ.core :refer [env]]
            [hw.rest-parser.cli :as cli]
            [malli.core :as m]
            [malli.error :as me]
            [mount.core :refer [defstate] :as mount])
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

(defstate db
  :start (atom {}))

(defn post-record-handler
  [request]
  {:status 200 :body "POST"})

(defn get-records-handler
  [sort-name sort-comparator request]
  {:status 200 :body sort-name})

(def routes
  ["/" {"records" {:post post-record-handler
                   :get {"/color" (partial get-records-handler "color" cli/color-lastname)
                         "/birthdate" (partial get-records-handler "color" cli/dob)
                         "/name" (partial get-records-handler "color" cli/lastname-desc)}}}])

(def handler (bidi-ring/make-handler routes))

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
