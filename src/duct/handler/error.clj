(ns duct.handler.error
  (:require [compojure.response :as compojure]
            [integrant.core :as ig]
            [ring.util.response :as response]))

(defn- make-handler [status response]
  (fn handler
    ([request]
     (-> (compojure/render response request)
         (response/status status)))
    ([request respond raise]
     (respond (handler request)))))

(defmethod ig/init-key ::not-found [_ {:keys [response]}]
  (make-handler 404 response))

(defmethod ig/init-key ::internal-error [_ {:keys [response]}]
  (make-handler 500 response))
