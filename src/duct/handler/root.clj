(ns duct.handler.root
  (:require [integrant.core :as ig]))

(defmethod ig/prep-key :duct.handler/root [_ config]
  (merge {:router (ig/ref :duct/router)} config))

(defmethod ig/init-key :duct.handler/root [_ {:keys [middleware router]}]
  ((apply comp (reverse middleware)) router))
