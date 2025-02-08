(ns duct.module.web
  (:require [clojure.java.io :as io]
            [integrant.core :as ig]))

(defn- plaintext-response [text]
  {:headers {"Content-Type" "text/plain; charset=UTF-8"}, :body text})

(defn- html-response [html]
  {:headers {"Content-Type" "text/html; charset=UTF-8"}, :body html})

(defn- walk-route-data [route f]
  (if (and (vector? route) (seq route))
    (if (vector? (first route))
      (mapv #(walk-route-data % f) route)
      (let [[path data] route]
        [path (cond
                (vector? data)  (walk-route-data data f)
                (record? data)  data
                (map? data)     (f data)
                (keyword? data) (f {:name data})
                :else           data)]))
    route))

(defn- route-data-seq [route]
  (when (vector? route)
    (if (vector? (first route))
      (mapcat route-data-seq route)
      (let [[_ data] route]
        (cond
          (vector? data)  (route-data-seq data)
          (record? data)  nil
          (map? data)     (list data)
          (keyword? data) (list {:name data}))))))

(def ^:private request-methods
  #{:get :head :patch :delete :options :post :put :trace})

(def ^:private handler-indexes
  (set (concat [[:handler]]
               (map vector request-methods)
               (map (fn [m] [m :handler]) request-methods))))

(defn- add-ref-to-key [m ks]
  (if (qualified-keyword? (get-in m ks))
    (update-in m ks ig/ref)
    m))

(defn- add-refs-to-route-data [route-data]
  (if (some #(get-in route-data %) handler-indexes)
    (reduce add-ref-to-key route-data handler-indexes)
    (assoc route-data :handler (ig/ref (:name route-data)))))

(defn- find-handlers-in-route-data [route-data]
  (->> handler-indexes
       (keep #(get-in route-data %))
       (filter ig/ref?)
       (map :key)))

(defmethod ig/expand-key :duct.module/web
  [_ {:keys [features routes handler-opts]
      :or   {routes [], handler-opts {}}}]
  (let [featureset (set features)
        api?       (featureset :api)
        site?      (featureset :site)
        routes     (walk-route-data routes add-refs-to-route-data)]
    `{:duct.server.http/jetty
      {:port    ~(ig/var 'port)
       :logger  ~(ig/refset :duct/logger)
       :handler ~(ig/ref :duct/router)}

      :duct.router/reitit
      {:routes ~routes
       :data {~@(when api?
                  [:muuntaja {}, :coercion :malli]) ~@[]
              ~@(when site?
                  [:middleware [(ig/ref :duct.middleware.web/hiccup)]]) ~@[]}
       :middleware
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

      ~@(->> (route-data-seq routes)
             (mapcat find-handlers-in-route-data)
             (mapcat (fn [key] [key handler-opts])))
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
