(ns hw.rest-parser-test
  (:require [environ.core :as environ]
            [clojure.edn :as edn]
            [clojure.test :refer :all]
            [hw.rest-parser :refer [-main
                                    with-env]]))

;; There's more graceful ways to do this -- but let's roll it quick & dirty
(deftest test-main-basic-csv
  (testing "CSV record loading returns expected results"
    (with-env {:filepath "data/basic_record.csv"}
      (is (= (-> (with-out-str (-main))
                 (edn/read-string))
             ;; https://www.youtube.com/watch?v=QyrDgEz3DR0
             '({:LastName "Mcclure" :FirstName "Joey" :Email "joey.mc@gmail.com" :FavoriteColor "magenta" :DateOfBirth "3/22/1991"}
               {:LastName "Fitzpatrick" :FirstName "Donna" :Email "donna@fitzpatrick.com" :FavoriteColor "blue" :DateOfBirth "8/10/1972"}))))))
