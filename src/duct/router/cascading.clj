(ns duct.router.cascading
  (:require [compojure.core :as compojure]
            [integrant.core :as ig]))

(derive :duct.router/cascading :duct/router)

(defmethod ig/init-key :duct.router/cascading [_ routes]
  (apply compojure/routes routes))
