(ns duct.module.web-test
  (:require [clojure.test :refer [deftest is]]
            [clojure.java.io :as io]
            [duct.module.web :as web]
            [integrant.core :as ig]))

(deftest base-module-test
  (is (= {:duct.router/reitit
          {:routes []
           :data {:middleware []}
           :middleware
           [(ig/ref :duct.middleware.web/defaults)
            (ig/ref :duct.middleware.web/log-requests)
            (ig/ref :duct.middleware.web/log-errors)
            (ig/ref :duct.middleware.web/hide-errors)]
           :handlers [(ig/ref :duct.handler.reitit/default)]}
          :duct.middleware.web/defaults
          {:params    {:urlencoded true, :keywordize true}
           :responses {:not-modified-responses true
                       :absolute-redirects     true
                       :content-types          true
                       :default-charset        "utf-8"}}
          :duct.server.http/jetty
          {:port    (ig/var 'port)
           :handler (ig/ref :duct/router)
           :logger  (ig/refset :duct/logger)}
          :duct.handler.reitit/default
          {:not-found
           (ig/ref :duct.handler.static/not-found)
           :method-not-allowed
           (ig/ref :duct.handler.static/method-not-allowed)
           :not-acceptable
           (ig/ref :duct.handler.static/not-acceptable)}
          :duct.handler.static/bad-request
          {:headers {"Content-Type" "text/plain; charset=UTF-8"}
           :body    "Bad Request"}
          :duct.handler.static/not-found
          {:headers {"Content-Type" "text/plain; charset=UTF-8"}
           :body    "Not Found"}
          :duct.handler.static/method-not-allowed
          {:headers {"Content-Type" "text/plain; charset=UTF-8"}
           :body    "Method Not Allowed"}
          :duct.handler.static/not-acceptable
          {:headers {"Content-Type" "text/plain; charset=UTF-8"}
           :body    "Not Acceptable"}
          :duct.handler.static/internal-server-error
          {:headers {"Content-Type" "text/plain; charset=UTF-8"}
           :body    "Internal Server Error"}
          :duct.middleware.web/stacktrace {}
          :duct.middleware.web/hide-errors
          {:error-handler (ig/ref :duct.handler.static/internal-server-error)}
          :duct.middleware.web/log-requests {:logger (ig/ref :duct/logger)}
          :duct.middleware.web/log-errors   {:logger (ig/ref :duct/logger)}}
         (ig/expand {:duct.module/web {}}
                    (ig/deprofile [:main])))))

(deftest api-module-test
  (is (= {:duct.router/reitit
          {:routes []
           :data {:muuntaja {}, :coercion :malli, :middleware []}
           :middleware
           [(ig/ref :duct.middleware.web/defaults)
            (ig/ref :duct.middleware.web/log-requests)
            (ig/ref :duct.middleware.web/log-errors)
            (ig/ref :duct.middleware.web/hide-errors)]
           :handlers [(ig/ref :duct.handler.reitit/default)]}
          :duct.middleware.web/defaults
          {:params    {:urlencoded true, :keywordize true}
           :responses {:not-modified-responses true
                       :absolute-redirects     true
                       :content-types          true
                       :default-charset        "utf-8"}}
          :duct.server.http/jetty
          {:port    (ig/var 'port)
           :handler (ig/ref :duct/router)
           :logger  (ig/refset :duct/logger)}
          :duct.handler.reitit/default
          {:not-found
           (ig/ref :duct.handler.static/not-found)
           :method-not-allowed
           (ig/ref :duct.handler.static/method-not-allowed)
           :not-acceptable
           (ig/ref :duct.handler.static/not-acceptable)}
          :duct.handler.static/bad-request
          {:body {:error :bad-request}}
          :duct.handler.static/not-found
          {:body {:error :not-found}}
          :duct.handler.static/method-not-allowed
          {:body {:error :method-not-allowed}}
          :duct.handler.static/not-acceptable
          {:body {:error :not-acceptable}}
          :duct.handler.static/internal-server-error
          {:headers {"Content-Type" "application/json"}
           :body    (io/resource "duct/module/web/errors/500.json")}
          :duct.middleware.web/stacktrace {}
          :duct.middleware.web/hide-errors
          {:error-handler (ig/ref :duct.handler.static/internal-server-error)}
          :duct.middleware.web/log-requests {:logger (ig/ref :duct/logger)}
          :duct.middleware.web/log-errors   {:logger (ig/ref :duct/logger)}}
         (ig/expand {:duct.module/web {:features #{:api}}}
                    (ig/deprofile [:main])))))

(deftest site-module-test
  (is (= {:duct.router/reitit
          {:routes []
           :data
           {:middleware [(ig/ref :duct.middleware.web/hiccup)]}
           :middleware
           [(ig/ref :duct.middleware.web/webjars)
            (ig/ref :duct.middleware.web/defaults)
            (ig/ref :duct.middleware.web/log-requests)
            (ig/ref :duct.middleware.web/log-errors)
            (ig/ref :duct.middleware.web/hide-errors)]
           :handlers
           [(ig/ref :duct.handler/file)
            (ig/ref :duct.handler/resource)
            (ig/ref :duct.handler.reitit/default)]}
          :duct.middleware.web/defaults
          {:params    {:urlencoded true
                       :multipart  true
                       :nested     true
                       :keywordize true}
           :cookies   true
           :session   {:flash true
                       :cookie-attrs {:http-only true, :same-site :strict}}
           :security  {:anti-forgery  {:safe-header "X-Ring-Anti-Forgery"}
                       :frame-options :sameorigin
                       :content-type-options :nosniff}
           :responses {:not-modified-responses true
                       :absolute-redirects     true
                       :content-types          true
                       :default-charset        "utf-8"}
           :websocket {:keepalive true}}
          :duct.server.http/jetty
          {:port    (ig/var 'port)
           :handler (ig/ref :duct/router)
           :logger  (ig/refset :duct/logger)}
          :duct.middleware.web/webjars    {}
          :duct.middleware.web/hiccup     {}
          :duct.middleware.web/stacktrace {}
          :duct.handler/file
          {:paths {"/" {:root "static"}}}
          :duct.handler/resource
          {:paths {"/" {:root "duct/module/web/public"}}}
          :duct.handler.reitit/default
          {:not-found
           (ig/ref :duct.handler.static/not-found)
           :method-not-allowed
           (ig/ref :duct.handler.static/method-not-allowed)
           :not-acceptable
           (ig/ref :duct.handler.static/not-acceptable)}
          :duct.handler.static/bad-request
          {:headers {"Content-Type" "text/html; charset=UTF-8"}
           :body    (io/resource "duct/module/web/errors/400.html")}
          :duct.handler.static/not-found
          {:headers {"Content-Type" "text/html; charset=UTF-8"}
           :body    (io/resource "duct/module/web/errors/404.html")}
          :duct.handler.static/method-not-allowed
          {:headers {"Content-Type" "text/html; charset=UTF-8"}
           :body    (io/resource "duct/module/web/errors/405.html")}
          :duct.handler.static/not-acceptable
          {:headers {"Content-Type" "text/html; charset=UTF-8"}
           :body    (io/resource "duct/module/web/errors/406.html")}
          :duct.handler.static/internal-server-error
          {:headers {"Content-Type" "text/html; charset=UTF-8"}
           :body    (io/resource "duct/module/web/errors/500.html")}
          :duct.middleware.web/hide-errors
          {:error-handler (ig/ref :duct.handler.static/internal-server-error)}
          :duct.middleware.web/log-requests {:logger (ig/ref :duct/logger)}
          :duct.middleware.web/log-errors   {:logger (ig/ref :duct/logger)}}
         (ig/expand {:duct.module/web {:features #{:site}}}
                    (ig/deprofile [:main])))))

(deftest routes-transform-test
  (is (= {:duct.router/reitit
          {:routes
           [["/one" {:get {:handler (ig/ref ::handler1)}}]
            ["/foo"
             ["/two" {:name ::handler2, :handler (ig/ref ::handler2)}]
             ["/three/" ["four" {:handler (ig/ref ::handler3)}]]]
            ["/five" {:post {:handler (ig/ref ::handler4)}}
             ["/six" {:name ::handler5, :handler (ig/ref ::handler5)}]]]
           :data {:middleware []}
           :middleware
           [(ig/ref :duct.middleware.web/defaults)
            (ig/ref :duct.middleware.web/log-requests)
            (ig/ref :duct.middleware.web/log-errors)
            (ig/ref :duct.middleware.web/hide-errors)]
           :handlers [(ig/ref :duct.handler.reitit/default)]}
          :duct.middleware.web/defaults
          {:params    {:urlencoded true, :keywordize true}
           :responses {:not-modified-responses true
                       :absolute-redirects     true
                       :content-types          true
                       :default-charset        "utf-8"}}
          :duct.server.http/jetty
          {:port    (ig/var 'port)
           :handler (ig/ref :duct/router)
           :logger  (ig/refset :duct/logger)}
          ::handler1 {:name :foo}
          ::handler2 {:name :foo}
          ::handler3 {:name :foo}
          ::handler4 {:name :foo}
          ::handler5 {:name :foo}
          :duct.handler.reitit/default
          {:not-found
           (ig/ref :duct.handler.static/not-found)
           :method-not-allowed
           (ig/ref :duct.handler.static/method-not-allowed)
           :not-acceptable
           (ig/ref :duct.handler.static/not-acceptable)}
          :duct.handler.static/bad-request
          {:headers {"Content-Type" "text/plain; charset=UTF-8"}
           :body    "Bad Request"}
          :duct.handler.static/not-found
          {:headers {"Content-Type" "text/plain; charset=UTF-8"}
           :body    "Not Found"}
          :duct.handler.static/method-not-allowed
          {:headers {"Content-Type" "text/plain; charset=UTF-8"}
           :body    "Method Not Allowed"}
          :duct.handler.static/not-acceptable
          {:headers {"Content-Type" "text/plain; charset=UTF-8"}
           :body    "Not Acceptable"}
          :duct.handler.static/internal-server-error
          {:headers {"Content-Type" "text/plain; charset=UTF-8"}
           :body    "Internal Server Error"}
          :duct.middleware.web/stacktrace {}
          :duct.middleware.web/hide-errors
          {:error-handler (ig/ref :duct.handler.static/internal-server-error)}
          :duct.middleware.web/log-requests {:logger (ig/ref :duct/logger)}
          :duct.middleware.web/log-errors   {:logger (ig/ref :duct/logger)}}
         (ig/expand {:duct.module/web
                     {:handler-opts {:name :foo}
                      :routes
                      [["/one" {:get ::handler1}]
                       ["/foo"
                        ["/two" ::handler2]
                        ["/three/" ["four" {:handler ::handler3}]]]
                       ["/five" {:post {:handler ::handler4}}
                        ["/six" ::handler5]]]}}
                    (ig/deprofile [:main])))))

(deftest middleware-test
  (is (= {:duct.router/reitit
          {:routes
           [["/one" {:get {:handler (ig/ref ::handler)}
                     :middleware [(ig/ref ::foo)]}]]
           :data {:middleware [[(ig/ref ::quz) 2]
                                      (ig/ref ::bang)]}
           :middleware
           [(ig/ref ::bar)
            [(ig/ref ::baz) 1]
            (ig/ref :duct.middleware.web/defaults)
            (ig/ref :duct.middleware.web/log-requests)
            (ig/ref :duct.middleware.web/log-errors)
            (ig/ref :duct.middleware.web/hide-errors)]
           :handlers [(ig/ref :duct.handler.reitit/default)]}
          :duct.middleware.web/defaults
          {:params    {:urlencoded true, :keywordize true}
           :responses {:not-modified-responses true
                       :absolute-redirects     true
                       :content-types          true
                       :default-charset        "utf-8"}}
          :duct.server.http/jetty
          {:port    (ig/var 'port)
           :handler (ig/ref :duct/router)
           :logger  (ig/refset :duct/logger)}
          ::handler {}
          ::foo  {:name :foo}
          ::bar  {:name :foo}
          ::baz  {:name :foo}
          ::bang {:name :foo}
          ::quz  {:name :foo}
          :duct.handler.reitit/default
          {:not-found
           (ig/ref :duct.handler.static/not-found)
           :method-not-allowed
           (ig/ref :duct.handler.static/method-not-allowed)
           :not-acceptable
           (ig/ref :duct.handler.static/not-acceptable)}
          :duct.handler.static/bad-request
          {:headers {"Content-Type" "text/plain; charset=UTF-8"}
           :body    "Bad Request"}
          :duct.handler.static/not-found
          {:headers {"Content-Type" "text/plain; charset=UTF-8"}
           :body    "Not Found"}
          :duct.handler.static/method-not-allowed
          {:headers {"Content-Type" "text/plain; charset=UTF-8"}
           :body    "Method Not Allowed"}
          :duct.handler.static/not-acceptable
          {:headers {"Content-Type" "text/plain; charset=UTF-8"}
           :body    "Not Acceptable"}
          :duct.handler.static/internal-server-error
          {:headers {"Content-Type" "text/plain; charset=UTF-8"}
           :body    "Internal Server Error"}
          :duct.middleware.web/stacktrace {}
          :duct.middleware.web/hide-errors
          {:error-handler (ig/ref :duct.handler.static/internal-server-error)}
          :duct.middleware.web/log-requests {:logger (ig/ref :duct/logger)}
          :duct.middleware.web/log-errors   {:logger (ig/ref :duct/logger)}}
         (ig/expand {:duct.module/web
                     {:middleware-opts {:name :foo}
                      :routes [["/one" {:get ::handler, :middleware [::foo]}]]
                      :middleware [::bar [::baz 1]]
                      :route-middleware [[::quz 2] ::bang]}}
                    (ig/deprofile [:main])))))
