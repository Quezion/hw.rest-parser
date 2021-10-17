(ns hw.rest-parser.server-test
  (:require [aleph.http :as http]
            [byte-streams :as bs]
            [cheshire.core :as json]
            [environ.core :as environ]
            [clojure.edn :as edn]
            [clojure.instant :as inst]
            [clojure.string :as str]
            [clojure.test :refer :all]
            [hw.rest-parser.server :refer :all]
            [hw.rest-parser.cli :refer [with-env] :as cli]
            [hw.rest-parser.cli-test :as cli-test]
            [malli.instrument :as mi]
            [mount.core :as mount]))

(mi/instrument!)

(defn record->csv
  "Maps record map into comma separated string"
  [record]
  (->> (select-keys record cli/row-headers)
       (map (comp cli/render-view-cast-to-string
                  second))
       (str/join #", ")))

(deftest record->csv-test
  (is (= "Mcclure, Joey, joey.mc@gmail.com, magenta, 3/22/1991"
         (record->csv {:LastName "Mcclure" :FirstName "Joey" :Email "joey.mc@gmail.com"
                       :FavoriteColor "magenta" :DateOfBirth #inst "1991-03-22T08:00:00.000-00:00"}))))

(defn post-record
  "Given base uri to host, POSTs records & asserts return was successful"
  [base-uri records]
  (let [{:keys [status]} @(http/post (str base-uri "/records")
                                     {:body records})]
    (is (= status 200) "Server should accept well-formed records via POST")))

(defn get-records
  "Given base-uri & sort-type (color, birthdate, name), return records from server"
  [base-uri sort-type]
  (let [{:keys [status body]} @(http/get (str base-uri "/records/" sort-type))]
    (is (= status 200) "Server should accept well-formed records via POST")
    (-> (bs/to-string body)
        (json/decode true))))

(def localhost-3000 "http://localhost:3000")

(deftest server-test
  (testing "Webserver inits on correct port & responds to /health"
    (with-env {:webserver-port "5000"}
      (mount/stop)
      (-main)
      (is (-> @(http/get "http://localhost:5000/health")
              :status
              (= 204)))))

  (testing "Webserver accepts POST records & returns records"
    (with-env {:webserver-port "3000"}
      (mount/stop)
      (-main)

      (doseq [record cli-test/merged-file-records]
        (->> (record->csv record)
             (post-record localhost-3000)))

      (is (= (->> (get-records localhost-3000 "color")
                  (map #(update % :DateOfBirth inst/read-instant-date))
                  (into #{}))
             (->> cli-test/merged-file-records
                  (into #{})))))))
