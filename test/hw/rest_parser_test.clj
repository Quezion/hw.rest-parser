(ns hw.rest-parser-test
  (:require [environ.core :as environ]
            [clojure.edn :as edn]
            [clojure.test :refer :all]
            [hw.rest-parser :refer [-main
                                    with-env]]))

;; There's more graceful ways to do this -- but let's roll it quick & dirty
(deftest test-main
  (testing "Basic CSV record loading returns expected results"
    (with-env {:filepaths "data/basic_record.csv"}
      (is (= (-> (with-out-str (-main))
                 (edn/read-string))
             ;; https://www.youtube.com/watch?v=QyrDgEz3DR0
             '({:LastName "Mcclure" :FirstName "Joey" :Email "joey.mc@gmail.com" :FavoriteColor "magenta" :DateOfBirth "3/22/1991"}
               {:LastName "Fitzpatrick" :FirstName "Donna" :Email "donna@fitzpatrick.com" :FavoriteColor "blue" :DateOfBirth "8/10/1972"})))))

  (testing "Merged file records return expected results"
    (with-env {:filepaths "data/basic_record.csv,data/basic_record.psv,data/basic_record.ssv"}
      (is (= (-> (with-out-str (-main))
                 (edn/read-string))
             '({:LastName "Mcclure",
                :FirstName "Joey",
                :Email "joey.mc@gmail.com",
                :FavoriteColor "magenta",
                :DateOfBirth "3/22/1991"}
               {:LastName "Fitzpatrick",
                :FirstName "Donna",
                :Email "donna@fitzpatrick.com",
                :FavoriteColor "blue",
                :DateOfBirth "8/10/1972"}
               {:LastName "Morris",
                :FirstName "Tyler",
                :Email "tyler.morris@example.com",
                :FavoriteColor "orange",
                :DateOfBirth "8/6/1951"}
               {:LastName "Stewart",
                :FirstName "Dora",
                :Email "dora.stewart@example.com",
                :FavoriteColor "purple",
                :DateOfBirth "9/6/1976"}
               {:LastName "Pierce",
                :FirstName "Dennis",
                :Email "dennis.pierce@example.com",
                :FavoriteColor "black",
                :DateOfBirth "9/7/1984"}
               {:LastName "Price",
                :FirstName "Lucille",
                :Email "lucille.price@example.com",
                :FavoriteColor "silver",
                :DateOfBirth "1/1/1946"}))))))
