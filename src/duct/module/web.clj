(ns duct.module.web
  (:require [clojure.java.io :as io]
            [integrant.core :as ig]))

(defn- plaintext-response [text]
  {:headers {"Content-Type" "text/plain; charset=UTF-8"}, :body text})

(defn- html-response [html]
  {:headers {"Content-Type" "text/html; charset=UTF-8"}, :body html})

(defmethod ig/expand-key :duct.module/web [_ _]
  {:duct.server.http/jetty
   {:port    (ig/var 'port)
    :logger  (ig/refset :duct/logger)
    :handler (ig/ref :duct.handler/root)}

   :duct.handler/root
   {:router (ig/ref :duct/router)
    :middleware
    [(ig/ref :duct.middleware.web/not-found)
     (ig/ref :duct.middleware.web/defaults)
     (ig/ref :duct.middleware.web/log-requests)
     (ig/ref :duct.middleware.web/log-errors)
     (ig/profile
      :repl (ig/ref :duct.middleware.web/stacktrace)
      :main (ig/ref :duct.middleware.web/hide-errors))]}

   :duct.middleware.web/defaults
   {:params    {:urlencoded true, :keywordize true}
    :responses {:not-modified-responses true
                :absolute-redirects true
                :content-types true
                :default-charset "utf-8"}}

   :duct.middleware.web/log-requests {}
   :duct.middleware.web/log-errors {}
   :duct.middleware.web/stacktrace {}

   :duct.middleware.web/not-found
   {:error-handler (ig/ref :duct.handler.static/not-found)}
   :duct.middleware.web/hide-errors
   {:error-handler (ig/ref :duct.handler.static/internal-server-error)}

   :duct.handler.static/bad-request (plaintext-response "Bad Request")
   :duct.handler.static/not-found   (plaintext-response "Not Found")
   :duct.handler.static/method-not-allowed
   (plaintext-response "Method Not Allowed")
   :duct.handler.static/internal-server-error
   (plaintext-response "Internal Server Error")})

(defmethod ig/expand-key ::api [_ _]
  (merge
   (ig/expand-key :duct.module/web {})
   {:duct.handler/root
    {:router (ig/ref :duct/router)
     :middleware
     [(ig/ref :duct.middleware.web/not-found)
      (ig/ref :duct.middleware.web/format)
      (ig/ref :duct.middleware.web/defaults)
      (ig/ref :duct.middleware.web/log-requests)
      (ig/ref :duct.middleware.web/log-errors)
      (ig/profile
       :repl (ig/ref :duct.middleware.web/stacktrace)
       :main (ig/ref :duct.middleware.web/hide-errors))]}

    :duct.middleware.web/format {}

    :duct.handler.static/bad-request {:body {:error :bad-request}}
    :duct.handler.static/not-found   {:body {:error :not-found}}
    :duct.handler.static/method-not-allowed
    {:body {:error :method-not-allowed}}
    :duct.handler.static/internal-server-error
    {:headers {"Content-Type" "application/json"}
     :body    (io/resource "duct/module/web/errors/500.json")}}))

(defmethod ig/expand-key ::site [_ _]
  (merge
   (ig/expand-key :duct.module/web {})
   {:duct.handler/root
    {:router (ig/ref :duct/router)
     :middleware
     [(ig/ref :duct.middleware.web/not-found)
      (ig/ref :duct.middleware.web/webjars)
      (ig/ref :duct.middleware.web/defaults)
      (ig/ref :duct.middleware.web/log-requests)
      (ig/ref :duct.middleware.web/log-errors)
      (ig/profile
       :repl (ig/ref :duct.middleware.web/stacktrace)
       :main (ig/ref :duct.middleware.web/hide-errors))]}

    :duct.middleware.web/webjars {}
    :duct.middleware.web/defaults
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

    :duct.handler.static/bad-request
    (html-response (io/resource "duct/module/web/errors/400.html"))
    :duct.handler.static/not-found
    (html-response (io/resource "duct/module/web/errors/404.html"))
    :duct.handler.static/method-not-allowed
    (html-response (io/resource "duct/module/web/errors/405.html"))
    :duct.handler.static/internal-server-error
    (html-response (io/resource "duct/module/web/errors/500.html"))}))
