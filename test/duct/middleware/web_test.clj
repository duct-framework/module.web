(ns duct.middleware.web-test
  (:require [clojure.test :refer :all]
            [compojure.core :as compojure]
            [duct.logger :as logger]
            [duct.middleware.web :refer :all]
            [ring.mock.request :as mock]))

(defrecord TestLogger [logs]
  logger/Logger
  (-log [_ level ns-str file line event data]
    (swap! logs conj [level event data])))

(deftest test-wrap-log-requests
  (let [logs    (atom [])
        handler (wrap-log-requests
                 (constantly {:status 200, :headers {}, :body "foo"})
                 (->TestLogger logs))]
    (handler (mock/request :get "/"))
    (is (= @logs
           [[:info :duct.middleware.web/request
             {:request-method :get, :uri "/", :query-string nil}]]))))

(deftest test-wrap-log-errors
  (let [logs    (atom [])
        ex      (Exception. "testing")
        handler (wrap-log-errors
                 (fn [_] (throw ex))
                 (->TestLogger logs))]
    (try (handler (mock/request :get "/")) (catch Exception _))
    (is (= @logs
           [[:error :duct.middleware.web/handler-error ex]]))))

(deftest test-wrap-hide-errors
  (let [handler (wrap-hide-errors
                 (fn [_] (throw (Exception. "testing")))
                 "Internal Error")]
    (is (= (handler (mock/request :get "/"))
           {:status 500, :headers {"Content-Type" "text/html"} :body "Internal Error"}))))

(deftest test-wrap-not-found
  (let [handler (wrap-not-found (constantly nil) "Not Found")]
    (is (= (handler (mock/request :get "/"))
           {:status 404, :headers {"Content-Type" "text/html"} :body "Not Found"}))))

(deftest test-wrap-route-aliases
  (let [handler (wrap-route-aliases
                 (compojure/GET "/index.html" [] "foo")
                 {"/" "/index.html"})]
    (is (= (:body (handler (mock/request :get "/")))
           "foo"))))
