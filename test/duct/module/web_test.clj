(ns duct.module.web-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [duct.core :as core]
            [duct.module.web :as web]
            [integrant.core :as ig]))

(def api-config
  {:duct.core/project-ns  'foo
   :duct.core/environment :production
   :duct.core/modules     [(ig/ref :duct.module.web/api)]
   :duct.module.web/api   {}})

(def site-config
  {:duct.core/project-ns  'foo
   :duct.core/environment :production
   :duct.core/modules     [(ig/ref :duct.module.web/site)]
   :duct.module.web/site  {}})

(deftest api-module-test
  (is (= (core/prep api-config)
         (merge api-config
                {:duct.middleware.web/log-requests
                 {:logger (ig/ref :duct.core/logger)}
                 :duct.core.web/handler
                 {:endpoints []
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
                 {:port 3000, :handler (ig/ref :duct.core.web/handler)}
                 :duct.middleware.web/hide-errors
                 {:response "Internal Server Error"}
                 :duct.middleware.web/not-found
                 {:response "Resource Not Found"}
                 :duct.middleware.web/log-errors
                 {:logger (ig/ref :duct.core/logger)}}))))

(deftest site-module-test
  (is (= (core/prep site-config)
         (merge site-config
                {:duct.middleware.web/log-requests
                 {:logger (ig/ref :duct.core/logger)}
                 :duct.core.web/handler
                 {:endpoints []
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
                 {:port 3000, :handler (ig/ref :duct.core.web/handler)}
                 :duct.middleware.web/webjars {}
                 :duct.middleware.web/hide-errors
                 {:response (io/resource "duct/module/web/errors/500.html")}
                 :duct.middleware.web/not-found
                 {:response (io/resource "duct/module/web/errors/404.html")}
                 :duct.middleware.web/log-errors
                 {:logger (ig/ref :duct.core/logger)}}))))
