(ns duct.module.web-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [duct.core :as core]
            [duct.module.web :as web]
            [integrant.core :as ig]))

(derive :duct.logger/fake :duct/logger)
(derive :duct.router/fake :duct/router)
(derive :duct.server.http/fake :duct.server/http)

(def api-config
  {:duct.core/project-ns  'foo
   :duct.core/environment :production
   :duct.logger/fake      {}
   :duct.module.web/api   {}})

(def site-config
  {:duct.core/project-ns  'foo
   :duct.core/environment :production
   :duct.logger/fake      {}
   :duct.module.web/site  {}})

(deftest api-module-test
  (is (= (core/prep api-config)
         (merge api-config
                {:duct.router/cascading {:endpoints []}
                 :duct.core/handler
                 {:router (ig/ref :duct/router)
                  :middleware
                  [(ig/ref :duct.middleware.web/not-found)
                   (ig/ref :duct.middleware.web/defaults)
                   (ig/ref :duct.middleware.web/log-requests)
                   (ig/ref :duct.middleware.web/log-errors)
                   (ig/ref :duct.middleware.web/hide-errors)]}
                 :duct.middleware.web/defaults
                 {:params    {:urlencoded true, :keywordize true}
                  :responses {:not-modified-responses true
                              :absolute-redirects true
                              :content-types true
                              :default-charset "utf-8"}}
                 :duct.server.http/jetty
                 {:port 3000
                  :handler (ig/ref :duct.core/handler)
                  :logger  (ig/ref :duct/logger)}
                 :duct.middleware.web/stacktrace {}
                 :duct.middleware.web/hide-errors
                 {:response "Internal Server Error"}
                 :duct.middleware.web/not-found
                 {:response "Resource Not Found"}
                 :duct.middleware.web/log-requests
                 {:logger (ig/ref :duct/logger)}
                 :duct.middleware.web/log-errors
                 {:logger (ig/ref :duct/logger)}}))))

(deftest site-module-test
  (is (= (core/prep site-config)
         (merge site-config
                {:duct.router/cascading {:endpoints []}
                 :duct.core/handler
                 {:router (ig/ref :duct/router)
                  :middleware
                  [(ig/ref :duct.middleware.web/not-found)
                   (ig/ref :duct.middleware.web/webjars)
                   (ig/ref :duct.middleware.web/defaults)
                   (ig/ref :duct.middleware.web/log-requests)
                   (ig/ref :duct.middleware.web/log-errors)
                   (ig/ref :duct.middleware.web/hide-errors)]}
                 :duct.middleware.web/defaults
                 {:params   {:urlencoded true
                             :multipart  true
                             :nested     true
                             :keywordize true}
                  :cookies   true
                  :session   {:flash true, :cookie-attrs {:http-only true}}
                  :security  {:anti-forgery         true
                              :xss-protection       {:enable? true, :mode :block}
                              :frame-options        :sameorigin
                              :content-type-options :nosniff}
                  :static    {:resources ["duct/module/web/public" "foo/public"]}
                  :responses {:not-modified-responses true
                              :absolute-redirects     true
                              :content-types          true
                              :default-charset        "utf-8"}}
                 :duct.server.http/jetty
                 {:port 3000
                  :handler (ig/ref :duct.core/handler)
                  :logger  (ig/ref :duct/logger)}
                 :duct.middleware.web/webjars {}
                 :duct.middleware.web/stacktrace {}
                 :duct.middleware.web/hide-errors
                 {:response (io/resource "duct/module/web/errors/500.html")}
                 :duct.middleware.web/not-found
                 {:response (io/resource "duct/module/web/errors/404.html")}
                 :duct.middleware.web/log-requests
                 {:logger (ig/ref :duct/logger)}
                 :duct.middleware.web/log-errors
                 {:logger (ig/ref :duct/logger)}}))))

(deftest http-server-test
  (let [config  (assoc api-config :duct.server.http/fake {:port 8080})
        prepped (core/prep config)]
    (is (not (contains? prepped :duct.server.http/jetty)))
    (is (= (:duct.server.http/fake prepped)
           {:port    8080
            :handler (ig/ref :duct.core/handler)
            :logger  (ig/ref :duct/logger)}))))

(deftest router-test
  (let [config  (assoc api-config :duct.router/fake {})
        prepped (core/prep config)]
    (is (not (contains? prepped :duct.router/cascading)))
    (is (= (:duct.router/fake prepped) {}))))
