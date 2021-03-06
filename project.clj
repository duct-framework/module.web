(defproject duct/module.web "0.7.3"
  :description "Duct module for running web applications"
  :url "https://github.com/duct-framework/module.web"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [duct/core "0.8.0"]
                 [duct/logger "0.3.0"]
                 [duct/server.http.jetty "0.2.1"]
                 [compojure "1.6.2"]
                 [integrant "0.8.0"]
                 [metosin/jsonista "0.3.3"]
                 [metosin/muuntaja "0.6.8"]
                 [org.slf4j/slf4j-nop "1.7.31"]
                 [org.webjars/normalize.css "5.0.0"]
                 [ring/ring-core "1.9.3"]
                 [ring/ring-devel "1.9.3"]
                 [ring/ring-defaults "0.3.3"]
                 [ring-webjars "0.2.0"]]
  :profiles
  {:dev {:dependencies [[ring/ring-mock "0.4.0"]]}})
