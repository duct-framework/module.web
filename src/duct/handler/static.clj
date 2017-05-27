(ns duct.handler.static
  (:require [clojure.java.io :as io]
            [integrant.core :as ig]
            [ring.core.protocols :as p]))

(extend-protocol p/StreamableResponseBody
  java.net.URL
  (write-body-to-stream [url response stream]
    (p/write-body-to-stream (io/input-stream url) response stream)))

(defn- make-handler [response]
  (fn
    ([_] response)
    ([_ respond _] (respond response))))

(defmethod ig/init-key :duct.handler/static [_ response]
  (make-handler response))

(defmethod ig/init-key ::ok [_ response]
  (make-handler (assoc response :status 200)))

(defmethod ig/init-key ::bad-request [_ response]
  (make-handler (assoc response :status 400)))

(defmethod ig/init-key ::not-found [_ response]
  (make-handler (assoc response :status 404)))

(defmethod ig/init-key ::method-not-allowed [_ response]
  (make-handler (assoc response :status 405)))

(defmethod ig/init-key ::internal-server-error [_ response]
  (make-handler (assoc response :status 500)))
