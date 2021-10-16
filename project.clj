(defproject hw.rest-parser "0.1.0-SNAPSHOT"
  :description "hw.rest-parser to build basic CLI parsing & HTTP server"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]

                 ;; Provides map of instantiated $environment vars at environ.core/env
                 ;; https://github.com/weavejester/environ
                 [environ "1.2.0"]

                 ;; Wraps clojure.spec to validate data structures & provide FN instrumentation.
                 ;; Cleaner than base spec with extra features & more runtime dynamicism.
                 [metosin/malli "0.6.2"]

                 ;; Simple state management library. There's good arguments for others,
                 ;; but Mount is sufficient for this simple repo.
                 [mount "0.1.16"]

                 ;; Modern HTTP client + server. Usually picked for its option of full server asynchronicity.
                 ;; Battle-proven in prod at several millions of requests / day.
                 [aleph "0.4.6"]

                 ;; Bidirectional URI routing. Alternative to Compojure or similar libs.
                 [bidi "2.1.6"]
                 [ring/ring-core "1.5.0" :exclusions [org.clojure/clojure]] ;; required for ring-bidi

                 ;; Common JSON parsing library
                 [cheshire "5.10.0"]]
  :plugins [[lein-environ "1.2.0"]]
  :main ^:skip-aot hw.rest-parser.server
  :target-path "target/%s"
  :profiles {:test {:resource-paths ["data"]}
             :uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
