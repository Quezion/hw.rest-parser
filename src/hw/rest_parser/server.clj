(ns hw.rest-parser.server
  (:require [aleph.http :as http]
            [bidi.bidi :as bidi]
            [bidi.ring :as bidi-ring]
            [byte-streams :as bs]
            [cheshire.core :as json]
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
  :start (atom []))

(defn post-record-handler
  [request]
  (try
    (let [body (-> :body request bs/to-string)
          separator (cli/line->separator body)]
      (if separator
        (let [line (cli/line->record separator body)]
          (swap! db conj line)
          {:status 200 :body "Accepted record"})))
    (catch Exception _
      ;; In our simple view of the world, for this sample app... blame the user
      {:status 400 :body "Probably bad user input ¯\\_(ツ)_/¯"})))

(defn get-records-handler
  [sort-definition request]
  {:status 200
   :headers {"content-type" "application/json"}
   :body (-> (cli/sort-records @db sort-definition)
             (json/encode))})

(def routes
  ["/" {"records" {:post post-record-handler
                   :get {"/color" (partial get-records-handler cli/color-lastname)
                         "/birthdate" (partial get-records-handler cli/dob)
                         "/name" (partial get-records-handler cli/lastname-desc)}}}])

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
