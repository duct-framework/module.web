(defproject duct/module.web "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [duct/core "0.9.0-SNAPSHOT"]
                 [duct/server.http.jetty "0.1.0-SNAPSHOT"]
                 [compojure "1.5.1"]
                 [integrant "0.2.1"]
                 [meta-merge "1.0.0"]
                 [org.slf4j/slf4j-nop "1.7.21"]
                 [ring/ring-core "1.5.1"]
                 [ring/ring-devel "1.5.1"]
                 [ring/ring-defaults "0.2.3"]
                 [ring-webjars "0.1.1"]
                 [org.webjars/normalize.css "5.0.0"]]
  :profiles
  {:dev {:dependencies [[ring/ring-mock "0.3.0"]]}})
