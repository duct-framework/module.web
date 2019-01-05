(defproject duct/module.web "0.7.0-beta1"
  :description "Duct module for running web applications"
  :url "https://github.com/duct-framework/module.web"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [duct/core "0.7.0"]
                 [duct/logger "0.3.0"]
                 [duct/server.http.jetty "0.2.0"]
                 [compojure "1.6.1"]
                 [integrant "0.7.0"]
                 [metosin/muuntaja "0.6.3"]
                 [org.slf4j/slf4j-nop "1.7.25"]
                 [org.webjars/normalize.css "5.0.0"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-devel "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring-webjars "0.2.0"]]
  :profiles
  {:dev {:dependencies [[ring/ring-mock "0.3.2"]]}})
