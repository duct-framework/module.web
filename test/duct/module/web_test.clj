(ns duct.module.web-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [duct.core :as core]
            [duct.module.web :as web]
            [integrant.core :as ig]))

(core/load-hierarchy)

(derive :duct.logger/fake :duct/logger)
(derive :duct.router/fake :duct/router)
(derive :duct.server.http/fake :duct.server/http)

(def base-config
  {:duct.core/project-ns  'foo
   :duct.core/environment :production
   :duct.logger/fake      {}
   :duct.module/web       {}})

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

(deftest base-module-test
  (is (= (core/prep base-config)
         (merge base-config
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
                 :duct.handler.static/bad-request
                 {:headers {"Content-Type" "text/plain; charset=UTF-8"}
                  :body "Bad Request"}
                 :duct.handler.static/not-found
                 {:headers {"Content-Type" "text/plain; charset=UTF-8"}
                  :body "Not Found"}
                 :duct.handler.static/method-not-allowed
                 {:headers {"Content-Type" "text/plain; charset=UTF-8"}
                  :body "Method Not Allowed"}
                 :duct.handler.static/internal-server-error
                 {:headers {"Content-Type" "text/plain; charset=UTF-8"}
                  :body "Internal Server Error"}
                 :duct.middleware.web/stacktrace {}
                 :duct.middleware.web/hide-errors
                 {:error-handler (ig/ref :duct.handler.static/internal-server-error)}
                 :duct.middleware.web/not-found
                 {:error-handler (ig/ref :duct.handler.static/not-found)}
                 :duct.middleware.web/log-requests
                 {:logger (ig/ref :duct/logger)}
                 :duct.middleware.web/log-errors
                 {:logger (ig/ref :duct/logger)}}))))

(deftest api-module-test
  (is (= (core/prep api-config)
         (merge api-config
                {:duct.router/cascading []
                 :duct.core/handler
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
                              :absolute-redirects true
                              :content-types true
                              :default-charset "utf-8"}}
                 :duct.server.http/jetty
                 {:port 3000
                  :handler (ig/ref :duct.core/handler)
                  :logger  (ig/ref :duct/logger)}
                 :duct.handler.static/bad-request
                 {:body {:error :bad-request}}
                 :duct.handler.static/not-found
                 {:body {:error :not-found}}
                 :duct.handler.static/method-not-allowed
                 {:body {:error :method-not-allowed}}
                 :duct.handler.static/internal-server-error
                 {:body {:error :internal-server-error}}
                 :duct.middleware.web/format {}
                 :duct.middleware.web/stacktrace {}
                 :duct.middleware.web/hide-errors
                 {:error-handler (ig/ref :duct.handler.static/internal-server-error)}
                 :duct.middleware.web/not-found
                 {:error-handler (ig/ref :duct.handler.static/not-found)}
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
                  :session   {:flash true, :cookie-attrs {:http-only true, :same-site :strict}}
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
                 :duct.handler.static/bad-request
                 {:headers {"Content-Type" "text/html; charset=UTF-8"}
                  :body (io/resource "duct/module/web/errors/400.html")}
                 :duct.handler.static/not-found
                 {:headers {"Content-Type" "text/html; charset=UTF-8"}
                  :body (io/resource "duct/module/web/errors/404.html")}
                 :duct.handler.static/method-not-allowed
                 {:headers {"Content-Type" "text/html; charset=UTF-8"}
                  :body (io/resource "duct/module/web/errors/405.html")}
                 :duct.handler.static/internal-server-error
                 {:headers {"Content-Type" "text/html; charset=UTF-8"}
                  :body (io/resource "duct/module/web/errors/500.html")}
                 :duct.middleware.web/hide-errors
                 {:error-handler (ig/ref :duct.handler.static/internal-server-error)}
                 :duct.middleware.web/not-found
                 {:error-handler (ig/ref :duct.handler.static/not-found)}
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

(deftest ring-defaults-test
  (testing "api config"
    (let [config  (assoc api-config
                         :duct.middleware.web/defaults
                         {:params {:keywordize false}})
        prepped (core/prep config)]
    (is (= (:params (:duct.middleware.web/defaults prepped))
           {:urlencoded true
            :keywordize false}))))
  (testing "site config"
    (let [config  (assoc site-config
                         :duct.middleware.web/defaults
                         {:params {:multipart false}})
          prepped (core/prep config)]
      (is (= (:params (:duct.middleware.web/defaults prepped))
             {:urlencoded true
              :multipart  false
              :nested     true
              :keywordize true})))))
