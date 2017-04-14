(ns duct.module.web
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [duct.core :as core]
            [duct.core.env :as env]
            [duct.core.merge :as merge]
            [duct.core.web :as web]
            [duct.middleware.web :as mw]
            [duct.server.http.jetty :as jetty]
            [integrant.core :as ig]
            [ring.middleware.defaults :as defaults]))

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

(def ^:private logging-config
  {::web/handler     {:middleware ^:distinct [(ig/ref ::mw/log-requests)
                                              (ig/ref ::mw/log-errors)]}
   ::mw/log-requests {:logger (ig/ref ::core/logger)}
   ::mw/log-errors   {:logger (ig/ref ::core/logger)}})

(def ^:private error-configs
  {:production
   {::web/handler {:middleware ^:distinct [(ig/ref ::mw/hide-errors)]}}
   :development
   {::web/handler {:middleware ^:distinct [(ig/ref ::mw/stacktrace)]}}})

(def ^:private api-config
  {:duct.server/http {:handler (ig/ref ::web/handler)}
   ::web/handler     {:endpoints  []
                      :middleware ^:distinct [(ig/ref ::mw/not-found)
                                              (ig/ref ::mw/defaults)]}
   ::mw/not-found    {:response (merge/displace "Resource Not Found")}
   ::mw/hide-errors  {:response (merge/displace "Internal Server Error")}
   ::mw/defaults     (merge/displace defaults/api-defaults)})

(def ^:private error-404 (io/resource "duct/module/web/errors/404.html"))
(def ^:private error-500 (io/resource "duct/module/web/errors/500.html"))

(defn- site-resource-paths [project-ns]
  ["duct/module/web/public" (str (name-to-path project-ns) "/public")])

(defn- site-defaults [project-ns]
  (assoc-in defaults/site-defaults [:static :resources] (site-resource-paths project-ns)))

(defn- site-config [project-ns]
  {:duct.server/http {:handler (ig/ref ::web/handler)}
   ::web/handler     {:endpoints  []
                      :middleware ^:distinct [(ig/ref ::mw/not-found)
                                              (ig/ref ::mw/webjars)
                                              (ig/ref ::mw/defaults)]}
   ::mw/webjars      {}
   ::mw/not-found    {:response (merge/displace error-404)}
   ::mw/hide-errors  {:response (merge/displace error-500)}
   ::mw/defaults     (merge/displace (site-defaults project-ns))})

(defmethod ig/init-key ::api [_ options]
  (fn [config]
    (core/merge-configs config
                        (server-config config)
                        api-config
                        logging-config
                        (error-configs (get-environment config options)))))

(defmethod ig/init-key ::site [_ options]
  (fn [config]
    (core/merge-configs config
                        (server-config config)
                        (site-config (get-project-ns config options))
                        logging-config
                        (error-configs (get-environment config options)))))
