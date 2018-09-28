(ns duct.handler.root-test
  (:require [clojure.test :refer :all]
            [duct.handler.root :as root]
            [integrant.core :as ig]))

(deftest test-root-handler
  (let [m {:duct.handler/root
           {:router     (fn [_] {:status 200, :headers {}, :body "foo"})
            :middleware [(fn [f] #(assoc-in (f %) [:headers "X-Foo"] "bar"))
                         (fn [f] #(assoc-in (f %) [:headers "X-Foo"] "baz"))]}}
        f (:duct.handler/root (ig/init m))]
    (is (= (f {:request-method :get, :uri "/"})
           {:status 200, :headers {"X-Foo" "baz"}, :body "foo"}))))
