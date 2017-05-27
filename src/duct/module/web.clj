(ns duct.module.web
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [duct.core :as core]
            [duct.core.env :as env]
            [duct.core.merge :as merge]
            [duct.handler.static :as static]
            [duct.middleware.web :as mw]
            [duct.router.cascading :as router]
            [integrant.core :as ig]
            [ring.middleware.defaults :as defaults]))

(derive :duct.server.http/jetty :duct.server/http)

(def ^:private server-port
  (env/env '["PORT" Int :or 3000]))

(defn- get-environment [config options]
  (:environment options (:duct.core/environment config :production)))

(defn- get-project-ns [config options]
  (:project-ns options (:duct.core/project-ns config)))

(defn- name-to-path [sym]
  (-> sym name (str/replace "-" "_") (str/replace "." "/")))

(defn- derived-key [m k default]
  (if-let [kv (ig/find-derived-1 m k)] (key kv) default))

(defn- http-server-key [config]
  (derived-key config :duct.server/http :duct.server.http/jetty))

(defn- server-config [config]
  {(http-server-key config) {:port (merge/displace server-port)}})

(defn- router-config [config]
  (if-not (ig/find-derived-1 config :duct/router)
    {:duct.router/cascading []}
    {}))

(def ^:private logging-config
  {::mw/log-requests {:logger (ig/ref :duct/logger)}
   ::mw/log-errors   {:logger (ig/ref :duct/logger)}
   ::core/handler    {:middleware ^:distinct [(ig/ref ::mw/log-requests)
                                              (ig/ref ::mw/log-errors)]}})

(def ^:private error-configs
  {:production
   {::core/handler {:middleware ^:distinct [(ig/ref ::mw/hide-errors)]}}
   :development
   {::core/handler {:middleware ^:distinct [(ig/ref ::mw/stacktrace)]}}})

(def ^:private common-config
  {::mw/not-found    {:error-handler (merge/displace (ig/ref ::static/not-found))}
   ::mw/hide-errors  {:error-handler (merge/displace (ig/ref ::static/internal-server-error))}
   ::mw/stacktrace   {}
   ::core/handler    {:router  (merge/displace (ig/ref :duct/router))}
   :duct.server/http {:handler (merge/displace (ig/ref ::core/handler))
                      :logger  (merge/displace (ig/ref :duct/logger))}})

(defn- plaintext-response [text]
  {:headers {"Content-Type" (merge/displace "text/plain; charset=UTF-8")}
   :body    (merge/displace text)})

(defn- html-response [html]
  {:headers {"Content-Type" (merge/displace "text/html; charset=UTF-8")}
   :body    (merge/displace html)})

(def ^:private base-config
  {::static/bad-request           (plaintext-response "Bad Request")
   ::static/not-found             (plaintext-response "Not Found")
   ::static/method-not-allowed    (plaintext-response "Method Not Allowed")
   ::static/internal-server-error (plaintext-response "Internal Server Error")
   ::mw/defaults                  (merge/displace defaults/api-defaults)
   ::core/handler                 {:middleware ^:distinct [(ig/ref ::mw/not-found)
                                                           (ig/ref ::mw/defaults)]}})

(def ^:private api-config
  {::static/bad-request           {:body (merge/displace {:error :bad-request})}
   ::static/not-found             {:body (merge/displace {:error :not-found})}
   ::static/method-not-allowed    {:body (merge/displace {:error :method-not-allowed})}
   ::static/internal-server-error {:body (merge/displace {:error :internal-server-error})}
   ::mw/format                    {}
   ::mw/defaults                  (merge/displace defaults/api-defaults)
   ::core/handler                 {:middleware ^:distinct [(ig/ref ::mw/not-found)
                                                           (ig/ref ::mw/format)
                                                           (ig/ref ::mw/defaults)]}})

(def ^:private error-400 (io/resource "duct/module/web/errors/400.html"))
(def ^:private error-404 (io/resource "duct/module/web/errors/404.html"))
(def ^:private error-405 (io/resource "duct/module/web/errors/405.html"))
(def ^:private error-500 (io/resource "duct/module/web/errors/500.html"))

(defn- site-resource-paths [project-ns]
  ["duct/module/web/public" (str (name-to-path project-ns) "/public")])

(defn- site-defaults [project-ns]
  (assoc-in defaults/site-defaults [:static :resources] (site-resource-paths project-ns)))

(defn- site-config [project-ns]
  {::static/bad-request           (html-response error-400)
   ::static/not-found             (html-response error-404)
   ::static/method-not-allowed    (html-response error-405)
   ::static/internal-server-error (html-response error-500)
   ::mw/webjars                   {}
   ::mw/defaults                  (merge/displace (site-defaults project-ns))
   ::core/handler                 {:middleware ^:distinct [(ig/ref ::mw/not-found)
                                                           (ig/ref ::mw/webjars)
                                                           (ig/ref ::mw/defaults)]}})

(derive :duct.module/web :duct/module)
(derive ::api            :duct/module)
(derive ::site           :duct/module)

(defmethod ig/init-key :duct.module/web [_ options]
  {:req #{:duct/logger}
   :fn  (fn [config]
          (core/merge-configs config
                              (server-config config)
                              (router-config config)
                              common-config
                              base-config
                              logging-config
                              (error-configs (get-environment config options))))})

(defmethod ig/init-key ::api [_ options]
  {:req #{:duct/logger}
   :fn  (fn [config]
          (core/merge-configs config
                              (server-config config)
                              (router-config config)
                              common-config
                              api-config
                              logging-config
                              (error-configs (get-environment config options))))})

(defmethod ig/init-key ::site [_ options]
  {:req #{:duct/logger}
   :fn  (fn [config]
          (core/merge-configs config
                              (server-config config)
                              (router-config config)
                              common-config
                              (site-config (get-project-ns config options))
                              logging-config
                              (error-configs (get-environment config options))))})
