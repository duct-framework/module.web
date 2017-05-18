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
                {:duct.router/cascading []
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
                 :duct.handler.error/bad-request
                 {:response "Bad Request"}
                 :duct.handler.error/not-found
                 {:response "Resource Not Found"}
                 :duct.handler.error/method-not-allowed
                 {:response "Method Not Allowed"}
                 :duct.handler.error/internal-error
                 {:response "Internal Server Error"}
                 :duct.middleware.web/stacktrace {}
                 :duct.middleware.web/hide-errors
                 {:error-handler (ig/ref :duct.handler.error/internal-error)}
                 :duct.middleware.web/not-found
                 {:error-handler (ig/ref :duct.handler.error/not-found)}
                 :duct.middleware.web/log-requests
                 {:logger (ig/ref :duct/logger)}
                 :duct.middleware.web/log-errors
                 {:logger (ig/ref :duct/logger)}}))))

(deftest site-module-test
  (is (= (core/prep site-config)
         (merge site-config
                {:duct.router/cascading []
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
                 :duct.handler.error/bad-request
                 {:response (io/resource "duct/module/web/errors/400.html")}
                 :duct.handler.error/not-found
                 {:response (io/resource "duct/module/web/errors/404.html")}
                 :duct.handler.error/method-not-allowed
                 {:response (io/resource "duct/module/web/errors/405.html")}
                 :duct.handler.error/internal-error
                 {:response (io/resource "duct/module/web/errors/500.html")}
                 :duct.middleware.web/hide-errors
                 {:error-handler (ig/ref :duct.handler.error/internal-error)}
                 :duct.middleware.web/not-found
                 {:error-handler (ig/ref :duct.handler.error/not-found)}
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
