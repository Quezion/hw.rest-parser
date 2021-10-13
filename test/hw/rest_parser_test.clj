(ns hw.rest-parser-test
  (:require [environ.core :as environ]
            [clojure.edn :as edn]
            [clojure.test :refer :all]
            [hw.rest-parser :refer :all]))

;; There's more graceful ways to do this -- but let's roll it quick & dirty
(deftest main-test
  (testing "Basic CSV record loading returns expected results"
    (with-env {:filepaths "data/basic_record.csv"}
      (is (= (-> (with-out-str (-main))
                 (edn/read-string))
             ;; https://www.youtube.com/watch?v=QyrDgEz3DR0
             '({:LastName "Mcclure" :FirstName "Joey" :Email "joey.mc@gmail.com"
                :FavoriteColor "magenta" :DateOfBirth #inst "1991-03-22T08:00:00.000-00:00"}
               {:LastName "Fitzpatrick" :FirstName "Donna" :Email "donna@fitzpatrick.com"
                :FavoriteColor "blue" :DateOfBirth #inst "1972-08-10T07:00:00.000-00:00"})))))

  (testing "Merged file records return expected results"
    (with-env {:filepaths "data/basic_record.csv,data/basic_record.psv,data/basic_record.ssv"}
      (is (= (-> (with-out-str (-main))
                 (edn/read-string))
             '({:LastName "Mcclure"
                :FirstName "Joey"
                :Email "joey.mc@gmail.com"
                :FavoriteColor "magenta"
                :DateOfBirth #inst "1991-03-22T08:00:00.000-00:00"}
               {:LastName "Fitzpatrick"
                :FirstName "Donna"
                :Email "donna@fitzpatrick.com"
                :FavoriteColor "blue"
                :DateOfBirth #inst "1972-08-10T07:00:00.000-00:00"}
               {:LastName "Morris"
                :FirstName "Tyler"
                :Email "tyler.morris@example.com"
                :FavoriteColor "orange"
                :DateOfBirth #inst "1951-08-06T07:00:00.000-00:00"}
               {:LastName "Stewart"
                :FirstName "Dora"
                :Email "dora.stewart@example.com"
                :FavoriteColor "purple"
                :DateOfBirth #inst "1976-09-06T07:00:00.000-00:00"}
               {:LastName "Pierce"
                :FirstName "Dennis"
                :Email "dennis.pierce@example.com"
                :FavoriteColor "black"
                :DateOfBirth #inst "1984-09-07T07:00:00.000-00:00"}
               {:LastName "Price"
                :FirstName "Lucille"
                :Email "lucille.price@example.com"
                :FavoriteColor "silver"
                :DateOfBirth #inst "1946-01-01T08:00:00.000-00:00"}))))))

(def minimal-unsorted-records
  "Small handrolled set to guarantee correct sorting"
  [{:FavoriteColor "blue" :LastName "Parker"
    :DateOfBirth #inst "1991-03-22T08:00:00.000-00:00"}
   {:FavoriteColor "red" :LastName "Bombadil"
    :DateOfBirth #inst "1972-08-10T07:00:00.000-00:00"}
   {:FavoriteColor "blue" :LastName "Packer"
    :DateOfBirth #inst "1951-08-06T07:00:00.000-00:00"}
   {:FavoriteColor "pink" :LastName "Stelsior"
    :DateOfBirth #inst "1984-09-07T07:00:00.000-00:00"}])

(deftest sort-records-test
  (let [[parker bombadil packer stelsior] minimal-unsorted-records]
    (is (= (sort-records minimal-unsorted-records lastname-desc)
           [stelsior parker packer bombadil]))
    (is (= (sort-records minimal-unsorted-records dob)
           [packer bombadil stelsior parker]))
    (is (= (sort-records minimal-unsorted-records color-lastname)
           [packer parker stelsior bombadil]))))
