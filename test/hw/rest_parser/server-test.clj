(ns hw.rest-parser.cli-test
  (:require [environ.core :as environ]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :refer :all]
            [hw.rest-parser.server :refer :all]
            [malli.instrument :as mi]))
