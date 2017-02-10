(ns duct.module.web
  (:require [clojure.java.io :as io]
            [duct.core :refer [assoc-in-default]]
            [duct.core.web :as core]
            [duct.middleware.web :as mw]
            [duct.server.http.jetty :as jetty]
            [integrant.core :as ig]
            [meta-merge.core :refer [meta-merge]]
            [ring.middleware.defaults :as defaults]))

(defn- missing-middleware? [middleware key]
  (not (contains? (set (map :key middleware)) key)))

(defn- update-middleware [middleware func key]
  (cond-> middleware (missing-middleware? middleware key) (func (ig/ref key))))

(defn- add-server [config {:keys [server-port] :or {server-port 3000}}]
  (if-let [[k v] (ig/find-derived-1 config :duct.server/http)]
    (assoc-in-default config [k :port] server-port)
    (assoc config :duct.server.http/jetty {:port server-port})))

(defn- add-handler [config]
  (let [[k v] (ig/find-derived-1 config :duct.server/http)]
    (-> config
        (assoc-in [k :handler] (ig/ref ::core/handler))
        (assoc-in-default [::core/handler :endpoints]  [])
        (assoc-in-default [::core/handler :middleware] []))))

(defn add-middleware [config func key value]
  (-> config
      (update-in [::core/handler :middleware] update-middleware func key)
      (update key (partial meta-merge value))))

(defn- get-env [config options]
  (:environment options (:duct.core/environment config :production)))

(defn- add-log-middleware [config]
  (let [[server _] (ig/find-derived-1 config :duct.server/http)]
    (if-let [[logger _] (ig/find-derived-1 config :duct/logger)]
      (-> config
          (assoc-in-default [server :logger] (ig/ref logger))
          (add-middleware conj ::mw/log-requests {:logger (ig/ref logger)})
          (add-middleware conj ::mw/log-errors   {:logger (ig/ref logger)}))
      config)))

(defn- add-error-middleware [config options response]
  (let [env (get-env config options)]
    (cond-> config
      (= env :production)  (add-middleware conj ::mw/hide-errors {:response response})
      (= env :development) (add-middleware conj ::mw/stacktrace  {}))))

(defmethod ig/init-key ::api [_ options]
  (fn [config]
    (-> config
        (add-server options)
        (add-handler)
        (add-middleware conj ::mw/not-found {:response "Resource Not Found"})
        (add-middleware conj ::mw/defaults  defaults/api-defaults)
        (add-log-middleware)
        (add-error-middleware options "Internal Server Error"))))

(def ^:private error-404 (io/resource "duct/module/web/errors/404.html"))
(def ^:private error-500 (io/resource "duct/module/web/errors/500.html"))

(defn- site-defaults [{:keys [static-resources]}]
  (assoc-in defaults/site-defaults [:static :resources]
            (into ["duct/module/web/public"] (if static-resources [static-resources]))))

(defmethod ig/init-key ::site [_ options]
  (fn [config]
    (-> config
        (add-server options)
        (add-handler)
        (add-middleware conj ::mw/not-found {:response error-404})
        (add-middleware conj ::mw/webjars   {})
        (add-middleware conj ::mw/defaults  (site-defaults options))
        (add-log-middleware)
        (add-error-middleware options error-500))))
