(defproject duct/module.web "0.2.0"
  :description "Duct module for running web applications"
  :url "https://github.com/duct-framework/module.web"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [duct/core "0.2.1"]
                 [duct/logger "0.1.1"]
                 [duct/server.http.jetty "0.1.2"]
                 [compojure "1.6.0-beta3"]
                 [integrant "0.4.0"]
                 [org.slf4j/slf4j-nop "1.7.21"]
                 [org.webjars/normalize.css "5.0.0"]
                 [ring/ring-core "1.6.0-RC3"]
                 [ring/ring-devel "1.6.0-RC3"]
                 [ring/ring-defaults "0.3.0-beta3"]
                 [ring-webjars "0.2.0-beta1"]]
  :profiles
  {:dev {:dependencies [[ring/ring-mock "0.3.0"]]}})
