(ns duct.session-store.cookie
  (:require [integrant.core :as ig]
            [ring.middleware.session.cookie :as cookie])
  (:import [org.apache.commons.codec.binary Hex]))

(defmethod ig/assert-key :duct.session-store/cookie [_ {:keys [key]}]
  (assert (or (nil? key)
              (and (string? key) (re-matches #"[0-9a-fA-F]{32}" key)))
          ":key should be nil or a randomly generated 16-byte hex string"))

(defmethod ig/init-key :duct.session-store/cookie [_ {:keys [key]}]
  (if (some? key)
    (cookie/cookie-store {:key (Hex/decodeHex ^String key)})
    (cookie/cookie-store)))

(defmethod ig/resume-key :duct.session-store/cookie 
  [k opts old-opts old-store]
  (if (= opts old-opts)
    old-store
    (ig/init-key k opts)))
