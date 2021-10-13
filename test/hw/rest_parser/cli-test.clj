(ns hw.rest-parser.cli-test
  (:require [environ.core :as environ]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :refer :all]
            [hw.rest-parser.cli :refer :all]
            [malli.instrument :as mi]))

;; We'd normally handle this via state-management lib to allow instrumentation in any app flow
;; Not only in tests, but also in any CI environments. (How confident are you in your specs? :)
(mi/instrument!)

(def expected-table
  "LastName   FirstName  Email      Favorit... DateOfB...
  ======================================================
  Pierce     Dennis     dennis.... black      9/7/1984
  Fitzpat... Donna      donna@f... blue       8/10/1972
  Mcclure    Joey       joey.mc... magenta    3/22/1991
  Morris     Tyler      tyler.m... orange     8/6/1951
  Stewart    Dora       dora.st... purple     9/6/1976
  Price      Lucille    lucille... silver     1/1/1946

  LastName   FirstName  Email      Favorit... DateOfB...
  ======================================================
  Price      Lucille    lucille... silver     1/1/1946
  Morris     Tyler      tyler.m... orange     8/6/1951
  Fitzpat... Donna      donna@f... blue       8/10/1972
  Stewart    Dora       dora.st... purple     9/6/1976
  Pierce     Dennis     dennis.... black      9/7/1984
  Mcclure    Joey       joey.mc... magenta    3/22/1991

  LastName   FirstName  Email      Favorit... DateOfB...
  ======================================================
  Stewart    Dora       dora.st... purple     9/6/1976
  Price      Lucille    lucille... silver     1/1/1946
  Pierce     Dennis     dennis.... black      9/7/1984
  Morris     Tyler      tyler.m... orange     8/6/1951
  Mcclure    Joey       joey.mc... magenta    3/22/1991
  Fitzpat... Donna      donna@f... blue       8/10/1972")

(defn trim-lines [table]
  (->> (str/split table #"\n")
       (map str/trim)
       (str/join "\n")))

(deftest main-test
  (testing "Merged file records return expected string table"
    (with-env {:filepaths "data/basic_record.csv,data/basic_record.psv,data/basic_record.ssv"
               :column-length 10}
      (is (= (trim-lines (with-out-str (-main)))
             ;; https://www.youtube.com/watch?v=QyrDgEz3DR0
             (trim-lines expected-table))))))

(deftest run-test
  (testing "Basic CSV record loading returns expected results"
    (is (= (run {:filepaths "data/basic_record.csv"})
           '({:LastName "Mcclure" :FirstName "Joey" :Email "joey.mc@gmail.com"
              :FavoriteColor "magenta" :DateOfBirth #inst "1991-03-22T08:00:00.000-00:00"}
             {:LastName "Fitzpatrick" :FirstName "Donna" :Email "donna@fitzpatrick.com"
              :FavoriteColor "blue" :DateOfBirth #inst "1972-08-10T07:00:00.000-00:00"}))))

  (testing "Merged file records return expected results"
    (is (= (run {:filepaths "data/basic_record.csv,data/basic_record.psv,data/basic_record.ssv"})
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
              :DateOfBirth #inst "1946-01-01T08:00:00.000-00:00"})))))

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
