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
  (let [response {:status 200, :headers {}, :body "foo"}]
    (testing "synchronous"
      (let [logs    (atom [])
            handler  (wrap-log-requests (constantly response) (->TestLogger logs))]
        (is (= (handler (mock/request :get "/")) response))
        (is (= @logs [[:info :duct.middleware.web/request
                       {:request-method :get, :uri "/", :query-string nil}]]))))
    
    (testing "asynchronous"
      (let [logs     (atom [])
            handler  (wrap-log-requests
                      (fn [_ respond _] (respond response))
                      (->TestLogger logs))
            respond  (promise)
            raise    (promise)]
        (handler (mock/request :get "/") respond raise)
        (is (not (realized? raise)))
        (is (= @respond response))
        (is (= @logs [[:info :duct.middleware.web/request
                       {:request-method :get, :uri "/", :query-string nil}]]))))))

(deftest test-wrap-log-errors
  (let [ex (Exception. "testing")]
    (testing "synchronous"
      (let [logs    (atom [])
            handler (wrap-log-errors (fn [_] (throw ex)) (->TestLogger logs))]
        (is (thrown? Exception (handler (mock/request :get "/"))))
        (is (= @logs [[:error :duct.middleware.web/handler-error ex]]))))

    (testing "asynchronous"
      (let [logs    (atom [])
            handler (wrap-log-errors (fn [_ _ raise] (raise ex)) (->TestLogger logs))
            respond (promise)
            raise   (promise)]
        (handler (mock/request :get "/") respond raise)
        (is (not (realized? respond)))
        (is (= @raise ex))
        (is (= @logs [[:error :duct.middleware.web/handler-error ex]]))))))

(deftest test-wrap-hide-errors
  (let [response {:status 500, :headers {"Content-Type" "text/html"} :body "Internal Error"}]
    (testing "synchronous"
      (let [handler (-> (fn [_] (throw (Exception. "testing")))
                        (wrap-hide-errors "Internal Error"))]
        (is (= (handler (mock/request :get "/")) response))))

    (testing "asynchronous"
      (let [handler (-> (fn [_ _ raise] (raise (Exception. "testing")))
                        (wrap-hide-errors "Internal Error"))
            respond (promise)
            raise   (promise)]
        (handler (mock/request :get "/") respond raise)
        (is (not (realized? raise)))
        (is (= @respond response))))))

(deftest test-wrap-not-found
  (let [response {:status 404, :headers {"Content-Type" "text/html"} :body "Not Found"}]
    (testing "synchronous"
      (let [handler (wrap-not-found (constantly nil) "Not Found")]
        (is (= (handler (mock/request :get "/")) response))))

    (testing "asynchronous"
      (let [handler (wrap-not-found (fn [_ respond _] (respond nil)) "Not Found")
            respond (promise)
            raise   (promise)]
        (handler (mock/request :get "/") respond raise)
        (is (not (realized? raise)))
        (is (= @respond response))))))

(deftest test-wrap-route-aliases
  (let [response {:status  200
                  :headers {"Content-Type" "text/html; charset=utf-8"}
                  :body    "foo"}
        handler  (-> (compojure/GET "/index.html" [] "foo")
                     (wrap-route-aliases {"/" "/index.html"}))]
    (testing "synchronous"
      (is (= (handler (mock/request :get "/")) response)))

    (testing "asynchronous"
      (let [respond (promise)
            raise   (promise)]
        (handler (mock/request :get "/") respond raise)
        (is (not (realized? raise)))
        (is (= @respond response))))))
