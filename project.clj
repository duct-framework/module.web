(defproject org.duct-framework/module.web "0.12.8"
  :description "Duct module for running web applications"
  :url "https://github.com/duct-framework/module.web"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.12.1"]
                 [org.duct-framework/logger "0.4.0"]
                 [org.duct-framework/handler "0.1.1"]
                 [org.duct-framework/server.http.jetty "0.3.0"]
                 [org.duct-framework/router.reitit "0.5.0"]
                 [com.rpl/specter "1.1.4"]
                 [integrant "0.13.1"]
                 [hiccup "2.0.0-RC5"]
                 [org.slf4j/slf4j-nop "2.0.17"]
                 [org.webjars/normalize.css "5.0.0"]
                 [ring/ring-core "1.14.1"]
                 [ring/ring-devel "1.14.1"]
                 [ring/ring-defaults "0.6.0"]
                 [ring-webjars "0.3.0"]]
  :plugins [[eftest "0.6.0"]]
  :profiles
  {:dev {:dependencies [[ring/ring-mock "0.6.1"]]}})
