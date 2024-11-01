(defproject org.duct-framework/module.web "0.8.0"
  :description "Duct module for running web applications"
  :url "https://github.com/duct-framework/module.web"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.11.4"]
                 [org.duct-framework/logger "0.4.0"]
                 [org.duct-framework/server.http.jetty "0.3.0"]
                 [compojure "1.7.1"]
                 [integrant "0.13.0"]
                 [metosin/jsonista "0.3.11"]
                 [metosin/muuntaja "0.6.10"]
                 [org.slf4j/slf4j-nop "2.0.16"]
                 [org.webjars/normalize.css "5.0.0"]
                 [ring/ring-core "1.13.0"]
                 [ring/ring-devel "1.13.0"]
                 [ring/ring-defaults "0.5.0"]
                 [ring-webjars "0.3.0"]]
  :profiles
  {:dev {:dependencies [[ring/ring-mock "0.4.0"]]}})
