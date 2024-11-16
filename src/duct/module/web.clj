(ns duct.module.web
  (:require [clojure.java.io :as io]
            [integrant.core :as ig]))

(defn- plaintext-response [text]
  {:headers {"Content-Type" "text/plain; charset=UTF-8"}, :body text})

(defn- html-response [html]
  {:headers {"Content-Type" "text/html; charset=UTF-8"}, :body html})

(defmethod ig/expand-key :duct.module/web
  [_ {:keys [features routes]}]
  (let [featureset (set features)
        api?       (featureset :api)
        site?      (featureset :site)]
    `{:duct.server.http/jetty
      {:port    ~(ig/var 'port)
       :logger  ~(ig/refset :duct/logger)
       :handler ~(ig/ref :duct/router)}

      :duct.router/reitit
      {:routes ~(or routes [])
       ~@(when api? [:muuntaja {}]) ~@[]
       :middleware
       [~@(when site? [(ig/ref :duct.middleware.web/webjars)])
        ~(ig/ref :duct.middleware.web/defaults)
        ~(ig/ref :duct.middleware.web/log-requests)
        ~(ig/ref :duct.middleware.web/log-errors)
        ~(ig/profile
          :repl (ig/ref :duct.middleware.web/stacktrace)
          :main (ig/ref :duct.middleware.web/hide-errors))]
       :default-handler
       {:not-found ~(ig/ref :duct.handler.static/not-found)
        :method-not-allowed ~(ig/ref :duct.handler.static/method-not-allowed)
        :not-acceptable ~(ig/ref :duct.handler.static/not-acceptable)}}

      :duct.middleware.web/defaults
      ~(if site?
         {:params    {:urlencoded true, :multipart true
                      :nested true, :keywordize true}
          :cookies   true
          :session   {:flash true
                      :cookie-attrs {:http-only true, :same-site :strict}}
          :security  {:anti-forgery         true
                      :frame-options        :sameorigin
                      :content-type-options :nosniff}
          :static    {:resources ["duct/module/web/public"]
                      :files     ["static"]}
          :responses {:not-modified-responses true
                      :absolute-redirects     true
                      :content-types          true
                      :default-charset        "utf-8"}}
         {:params    {:urlencoded true, :keywordize true}
          :responses {:not-modified-responses true
                      :absolute-redirects true
                      :content-types true
                      :default-charset "utf-8"}})

      :duct.middleware.web/log-requests {:logger ~(ig/ref :duct/logger)}
      :duct.middleware.web/log-errors   {:logger ~(ig/ref :duct/logger)}
      :duct.middleware.web/stacktrace   {}

      ~@(when api?  [:duct.middleware.web/format {}]) ~@[]
      ~@(when site? [:duct.middleware.web/webjars {}]) ~@[]

      :duct.middleware.web/hide-errors
      {:error-handler ~(ig/ref :duct.handler.static/internal-server-error)}

      :duct.handler.static/bad-request
      ~(cond
         site? (html-response (io/resource "duct/module/web/errors/400.html"))
         api?  {:body {:error :bad-request}}
         :else (plaintext-response "Bad Request"))

      :duct.handler.static/not-found
      ~(cond
         site? (html-response (io/resource "duct/module/web/errors/404.html"))
         api?  {:body {:error :not-found}}
         :else (plaintext-response "Not Found"))

      :duct.handler.static/method-not-allowed
      ~(cond
         site? (html-response (io/resource "duct/module/web/errors/405.html"))
         api?  {:body {:error :method-not-allowed}}
         :else (plaintext-response "Method Not Allowed"))

      :duct.handler.static/not-acceptable
      ~(cond
         site? (html-response (io/resource "duct/module/web/errors/406.html"))
         api?  {:body {:error :not-acceptable}}
         :else (plaintext-response "Not Acceptable"))

      :duct.handler.static/internal-server-error
      ~(cond
         site? (html-response (io/resource "duct/module/web/errors/500.html"))
         api?  {:headers {"Content-Type" "application/json"}
                :body    (io/resource "duct/module/web/errors/500.json")}
         :else (plaintext-response "Internal Server Error"))}))
