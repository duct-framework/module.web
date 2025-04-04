(ns duct.middleware.web-test
  (:require [clojure.test :refer :all]
            [duct.logger :as logger]
            [duct.middleware.web :refer :all]
            [integrant.core :as ig]
            [ring.mock.request :as mock])
  (:import [java.nio.file Files]
           [java.nio.file.attribute FileAttribute]))

(defrecord TestLogger [logs]
  logger/Logger
  (-log [_ level ns-str file line id event data]
    (swap! logs conj [level event data])))

(deftest test-wrap-log-requests
  (let [response {:status 200, :headers {}, :body "foo"}]
    (testing "synchronous"
      (let [logs    (atom [])
            handler  (wrap-log-requests (constantly response) (->TestLogger logs))]
        (is (= (handler (mock/request :get "/")) response))
        (is (= @logs [[:info :duct.middleware.web/request
                       {:request-method :get, :uri "/"}]]))))

    (testing "synchronous with log level"
      (let [logs    (atom [])
            handler (wrap-log-requests (constantly response)
                                       (->TestLogger logs)
                                       {:level :trace})]
        (is (= (handler (mock/request :get "/")) response))
        (is (= @logs [[:trace :duct.middleware.web/request
                       {:request-method :get, :uri "/"}]]))))

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
                       {:request-method :get, :uri "/"}]]))))))

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

(defn- make-error-handler [message]
  (fn handler
    ([_] {:headers {}, :body message})
    ([_ respond _] (respond {:headers {}, :body message}))))

(deftest test-wrap-hide-errors
  (let [err-handler (make-error-handler "Internal Error")
        response    {:status 500, :headers {} :body "Internal Error"}]
    (testing "synchronous"
      (let [handler (-> (fn [_] (throw (Exception. "testing")))
                        (wrap-hide-errors err-handler))]
        (is (= (handler (mock/request :get "/")) response))))

    (testing "asynchronous"
      (let [handler (-> (fn [_ _ raise] (raise (Exception. "testing")))
                        (wrap-hide-errors err-handler))
            respond (promise)
            raise   (promise)]
        (handler (mock/request :get "/") respond raise)
        (is (not (realized? raise)))
        (is (= @respond response))))))

(deftest test-wrap-defaults
  (let [attrs   (make-array FileAttribute 0)
        tempdir (Files/createTempDirectory "duct" attrs)]
    (Files/delete tempdir)
    (ig/init-key :duct.middleware.web/defaults
                 {:static {:files [(str tempdir)]}})
    (is (.exists (.toFile tempdir)))
    (Files/deleteIfExists tempdir)))

(deftest test-wrap-hiccup
  (is (= {:status  200
          :headers {"Content-Type" "text/html;charset=UTF-8"}
          :body    "<!DOCTYPE html>\n<title>foo</title>"}
         ((wrap-hiccup (constantly [:title "foo"])) {})))
  (is (= {:status  200
          :headers {"Content-Type" "text/html;charset=UTF-8"}
          :body    "<!DOCTYPE html>\n<title>foo</title>"}
         ((wrap-hiccup (constantly {:body [:title "foo"]})) {})))
  (is (= {:status  201
          :headers {"Content-Type" "text/html;charset=UTF-8"}
          :body    "<!DOCTYPE html>\n<title>foo</title>"}
         ((wrap-hiccup (constantly {:status 201, :body [:title "foo"]})) {})))
  (let [response {:status  200
                  :headers {"Content-Type" "text/plain;charset=UTF-8"}
                  :body    "Foobar"}]
    (is (= response ((wrap-hiccup (constantly response)) {})))))
