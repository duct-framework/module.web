(defproject duct/module.web "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [duct/server.http.jetty "0.1.0-SNAPSHOT"]
                 [compojure "1.5.1"]
                 [integrant "0.2.0"]
                 [meta-merge "1.0.0"]
                 [ring/ring-core "1.5.1"]
                 [ring/ring-defaults "0.2.2"]]
  :profiles
  {:dev {:dependencies [[ring/ring-mock "0.3.0"]]}})
