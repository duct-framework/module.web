(ns duct.handler.static-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [duct.handler.static :as static]
            [integrant.core :as ig]))

(deftest test-response-bodies
  (testing "nil body"
    (let [handler (ig/init-key :duct.handler/static {:body nil})]
      (is (= (handler {}) {:body nil}))))

  (testing "string body"
    (let [handler (ig/init-key :duct.handler/static {:body "foo"})]
      (is (= (handler {}) {:body "foo"}))))

  (testing "file body"
    (let [file     (io/file "test/duct/assets/test.txt")
          handler  (ig/init-key :duct.handler/static {:body file})
          response (handler {})]
      (is (= (get-in response [:headers "Content-Type"]) "text/plain"))
      (is (= (get-in response [:headers "Content-Length"]) "4"))
      (is (string? (get-in response [:headers "Last-Modified"])))
      (is (= (slurp (:body response)) "foo\n"))))

  (testing "file body"
    (let [file     (io/resource "duct/assets/test.txt")
          handler  (ig/init-key :duct.handler/static {:body file})
          response (handler {})]
      (is (= (get-in response [:headers "Content-Type"]) "text/plain"))
      (is (= (get-in response [:headers "Content-Length"]) "4"))
      (is (string? (get-in response [:headers "Last-Modified"])))
      (is (= (slurp (:body response)) "foo\n")))))

(deftest test-response-statuses
  (testing "200 OK"
    (let [handler (ig/init-key ::static/ok {:body "foo"})]
      (is (= (handler {}) {:status 200, :body "foo"}))))

  (testing "400 Bad Request"
    (let [handler (ig/init-key ::static/bad-request {:body "foo"})]
      (is (= (handler {}) {:status 400, :body "foo"}))))

  (testing "404 Not Found"
    (let [handler (ig/init-key ::static/not-found {:body "foo"})]
      (is (= (handler {}) {:status 404, :body "foo"}))))

  (testing "405 Method Not Allowed"
    (let [handler (ig/init-key ::static/method-not-allowed {:body "foo"})]
      (is (= (handler {}) {:status 405, :body "foo"}))))

  (testing "500 Internal Server Error"
    (let [handler (ig/init-key ::static/internal-server-error {:body "foo"})]
      (is (= (handler {}) {:status 500, :body "foo"})))))
