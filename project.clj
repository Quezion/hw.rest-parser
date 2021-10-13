(defproject hw.rest-parser "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]

                 ;; Provides map of instantiated $environment vars at environ.core/env
                 ;; https://github.com/weavejester/environ
                 [environ "1.2.0"]

                 ;; Wraps clojure.spec to validate data structures & provide FN instrumentation.
                 ;; Cleaner than base spec with extra features & more runtime dynamicism.
                 [metosin/malli "0.6.2"]]
  :plugins [[lein-environ "1.2.0"]]
  :main ^:skip-aot hw.rest-parser.server
  :target-path "target/%s"
  :profiles {:test {:resource-paths ["data"]}
             :uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
