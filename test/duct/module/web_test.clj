(ns duct.module.web-test
  (:require [clojure.test :refer [deftest is]]
            [clojure.java.io :as io]
            [duct.module.web :as web]
            [integrant.core :as ig]))

(deftest base-module-test
  (is (= {:duct.handler/root
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
                       :absolute-redirects     true
                       :content-types          true
                       :default-charset        "utf-8"}}
          :duct.server.http/jetty
          {:port    (ig/var 'port)
           :handler (ig/ref :duct.handler/root)
           :logger  (ig/refset :duct/logger)}
          :duct.handler.static/bad-request
          {:headers {"Content-Type" "text/plain; charset=UTF-8"}
           :body    "Bad Request"}
          :duct.handler.static/not-found
          {:headers {"Content-Type" "text/plain; charset=UTF-8"}
           :body    "Not Found"}
          :duct.handler.static/method-not-allowed
          {:headers {"Content-Type" "text/plain; charset=UTF-8"}
           :body    "Method Not Allowed"}
          :duct.handler.static/internal-server-error
          {:headers {"Content-Type" "text/plain; charset=UTF-8"}
           :body    "Internal Server Error"}
          :duct.middleware.web/stacktrace {}
          :duct.middleware.web/hide-errors
          {:error-handler (ig/ref :duct.handler.static/internal-server-error)}
          :duct.middleware.web/not-found
          {:error-handler (ig/ref :duct.handler.static/not-found)}
          :duct.middleware.web/log-requests {:logger (ig/ref :duct/logger)}
          :duct.middleware.web/log-errors   {:logger (ig/ref :duct/logger)}}
         (ig/expand {:duct.module/web {}}
                    (ig/deprofile [:main])))))

(deftest api-module-test
  (is (= {:duct.handler/root
          {:router (ig/ref :duct/router)
           :middleware
           [(ig/ref :duct.middleware.web/not-found)
            (ig/ref :duct.middleware.web/format)
            (ig/ref :duct.middleware.web/defaults)
            (ig/ref :duct.middleware.web/log-requests)
            (ig/ref :duct.middleware.web/log-errors)
            (ig/ref :duct.middleware.web/hide-errors)]}
          :duct.middleware.web/defaults
          {:params    {:urlencoded true, :keywordize true}
           :responses {:not-modified-responses true
                       :absolute-redirects     true
                       :content-types          true
                       :default-charset        "utf-8"}}
          :duct.server.http/jetty
          {:port    (ig/var 'port)
           :handler (ig/ref :duct.handler/root)
           :logger  (ig/refset :duct/logger)}
          :duct.handler.static/bad-request
          {:body {:error :bad-request}}
          :duct.handler.static/not-found
          {:body {:error :not-found}}
          :duct.handler.static/method-not-allowed
          {:body {:error :method-not-allowed}}
          :duct.handler.static/internal-server-error
          {:headers {"Content-Type" "application/json"}
           :body    (io/resource "duct/module/web/errors/500.json")}
          :duct.middleware.web/format     {}
          :duct.middleware.web/stacktrace {}
          :duct.middleware.web/hide-errors
          {:error-handler (ig/ref :duct.handler.static/internal-server-error)}
          :duct.middleware.web/not-found
          {:error-handler (ig/ref :duct.handler.static/not-found)}
          :duct.middleware.web/log-requests {:logger (ig/ref :duct/logger)}
          :duct.middleware.web/log-errors   {:logger (ig/ref :duct/logger)}}
         (ig/expand {:duct.module.web/api {}}
                    (ig/deprofile [:main])))))

(deftest site-module-test
  (is (= {:duct.handler/root
          {:router (ig/ref :duct/router)
           :middleware
           [(ig/ref :duct.middleware.web/not-found)
            (ig/ref :duct.middleware.web/webjars)
            (ig/ref :duct.middleware.web/defaults)
            (ig/ref :duct.middleware.web/log-requests)
            (ig/ref :duct.middleware.web/log-errors)
            (ig/ref :duct.middleware.web/hide-errors)]}
          :duct.middleware.web/defaults
          {:params    {:urlencoded true
                       :multipart  true
                       :nested     true
                       :keywordize true}
           :cookies   true
           :session   {:flash true, :cookie-attrs {:http-only true, :same-site :strict}}
           :security  {:anti-forgery         true
                       :frame-options        :sameorigin
                       :content-type-options :nosniff}
           :static    {:resources ["duct/module/web/public"]
                       :files ["static"]}
           :responses {:not-modified-responses true
                       :absolute-redirects     true
                       :content-types          true
                       :default-charset        "utf-8"}}
          :duct.server.http/jetty
          {:port    (ig/var 'port)
           :handler (ig/ref :duct.handler/root)
           :logger  (ig/refset :duct/logger)}
          :duct.middleware.web/webjars    {}
          :duct.middleware.web/stacktrace {}
          :duct.handler.static/bad-request
          {:headers {"Content-Type" "text/html; charset=UTF-8"}
           :body    (io/resource "duct/module/web/errors/400.html")}
          :duct.handler.static/not-found
          {:headers {"Content-Type" "text/html; charset=UTF-8"}
           :body    (io/resource "duct/module/web/errors/404.html")}
          :duct.handler.static/method-not-allowed
          {:headers {"Content-Type" "text/html; charset=UTF-8"}
           :body    (io/resource "duct/module/web/errors/405.html")}
          :duct.handler.static/internal-server-error
          {:headers {"Content-Type" "text/html; charset=UTF-8"}
           :body    (io/resource "duct/module/web/errors/500.html")}
          :duct.middleware.web/hide-errors
          {:error-handler (ig/ref :duct.handler.static/internal-server-error)}
          :duct.middleware.web/not-found
          {:error-handler (ig/ref :duct.handler.static/not-found)}
          :duct.middleware.web/log-requests {:logger (ig/ref :duct/logger)}
          :duct.middleware.web/log-errors   {:logger (ig/ref :duct/logger)}}
         (ig/expand {:duct.module.web/site {}}
                    (ig/deprofile [:main])))))
