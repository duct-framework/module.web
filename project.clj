(defproject duct/module.web "0.5.4"
  :description "Duct module for running web applications"
  :url "https://github.com/duct-framework/module.web"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [duct/core "0.5.0"]
                 [duct/logger "0.1.1"]
                 [duct/server.http.jetty "0.1.5"]
                 [compojure "1.6.0"]
                 [integrant "0.4.1"]
                 [metosin/muuntaja "0.2.1"]
                 [org.slf4j/slf4j-nop "1.7.25"]
                 [org.webjars/normalize.css "5.0.0"]
                 [ring/ring-core "1.6.2"]
                 [ring/ring-devel "1.6.2"]
                 [ring/ring-defaults "0.3.0"]
                 [ring-webjars "0.2.0"]]
  :profiles
  {:dev {:dependencies [[ring/ring-mock "0.3.0"]]}})
