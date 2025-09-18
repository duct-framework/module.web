(defproject org.duct-framework/module.web "0.12.13"
  :description "Duct module for running web applications"
  :url "https://github.com/duct-framework/module.web"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.12.2"]
                 [org.duct-framework/logger "0.4.0"]
                 [org.duct-framework/handler "0.1.2"]
                 [org.duct-framework/server.http.jetty "0.3.3"]
                 [org.duct-framework/router.reitit "0.5.2"]
                 [com.rpl/specter "1.1.5"]
                 [integrant "1.0.0"]
                 [hiccup "2.0.0"]
                 [org.slf4j/slf4j-nop "2.0.17"]
                 [org.webjars/normalize.css "5.0.0"]
                 [ring/ring-core "1.15.2"]
                 [ring/ring-devel "1.15.2"]
                 [ring/ring-defaults "0.7.0"]
                 [ring-webjars "0.3.1"]]
  :plugins [[eftest "0.6.0"]]
  :profiles
  {:dev {:dependencies [[ring/ring-mock "0.6.2"]]}})
