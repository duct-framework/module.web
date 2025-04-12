(ns duct.module.web
  (:require [clojure.java.io :as io]
            [com.rpl.specter :as s]
            [integrant.core :as ig]))

(defn- plaintext-response [text]
  {:headers {"Content-Type" "text/plain; charset=UTF-8"}, :body text})

(defn- html-response [html]
  {:headers {"Content-Type" "text/html; charset=UTF-8"}, :body html})

(defn- nested-route? [route]
  (and (vector? route) (some vector? route)))

(def ^:private ROUTES
  (s/recursive-path [] p
    (s/if-path nested-route? [s/ALL vector? p] s/STAY)))

(def ^:private request-methods
  #{:get :head :patch :delete :options :post :put :trace})

(def ^:private ROUTE-METHODS
  [map? (s/submap request-methods) s/MAP-VALS])

(defn- normalize-handlers [x]
  (if (qualified-keyword? x) {:name x, :handler x} x))

(defn- normalize-method-handlers [x]
  (if (qualified-keyword? x) {:handler x} x))

(def ^:private ENDPOINTS
  [ROUTES s/LAST
   (s/multi-path (s/view normalize-handlers)
                 [ROUTE-METHODS (s/view normalize-method-handlers)])])

(def ^:private HANDLERS
  [ENDPOINTS map? (s/must :handler)])

(defmethod ig/expand-key :duct.module/web
  [_ {:keys [features routes handler-opts]
      :or   {routes [], handler-opts {}}}]
  (let [featureset (set features)
        api?       (featureset :api)
        site?      (featureset :site)
        routes     (s/transform [HANDLERS qualified-keyword?] ig/ref routes)]
    `{:duct.server.http/jetty
      {:port    ~(ig/var 'port)
       :logger  ~(ig/refset :duct/logger)
       :handler ~(ig/ref :duct/router)}

      :duct.router/reitit
      {:routes ~routes
       :data
       {~@(when api?
            [:muuntaja {}, :coercion :malli]) ~@[]
        ~@(when site?
            [:module-middleware [(ig/ref :duct.middleware.web/hiccup)]]) ~@[]}
       :module-middleware
       [~@(when site? [(ig/ref :duct.middleware.web/webjars)])
        ~(ig/ref :duct.middleware.web/defaults)
        ~(ig/ref :duct.middleware.web/log-requests)
        ~(ig/ref :duct.middleware.web/log-errors)
        ~(ig/profile
          :repl (ig/ref :duct.middleware.web/stacktrace)
          :main (ig/ref :duct.middleware.web/hide-errors))]
       :handlers
       [~@(when site?
            [(ig/ref :duct.handler/file)
             (ig/ref :duct.handler/resource)])
        ~(ig/ref :duct.handler.reitit/default)]}

      :duct.middleware.web/defaults
      ~(if site?
         {:params    {:urlencoded true, :multipart true
                      :nested true, :keywordize true}
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
         {:params    {:urlencoded true, :keywordize true}
          :responses {:not-modified-responses true
                      :absolute-redirects true
                      :content-types true
                      :default-charset "utf-8"}})

      :duct.middleware.web/log-requests {:logger ~(ig/ref :duct/logger)}
      :duct.middleware.web/log-errors   {:logger ~(ig/ref :duct/logger)}
      :duct.middleware.web/stacktrace   {}

      ~@(when site? [:duct.middleware.web/webjars {}]) ~@[]
      ~@(when site? [:duct.middleware.web/hiccup {}])  ~@[]

      ~@(->> (s/select [HANDLERS ig/ref? :key] routes)
             (mapcat (fn [k] [k handler-opts])))
      ~@[]

      :duct.middleware.web/hide-errors
      {:error-handler ~(ig/ref :duct.handler.static/internal-server-error)}

      ~@(when site?
          [:duct.handler/file
           {:paths {"/" {:root "static"}}}
           :duct.handler/resource
           {:paths {"/" {:root "duct/module/web/public"}}}])
      ~@[]

      :duct.handler.reitit/default
      {:not-found          ~(ig/ref :duct.handler.static/not-found)
       :method-not-allowed ~(ig/ref :duct.handler.static/method-not-allowed)
       :not-acceptable     ~(ig/ref :duct.handler.static/not-acceptable)}

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
