(ns duct.session-store.cookie-test
  (:require [clojure.test :refer [deftest is testing]]
            [duct.session-store.cookie]
            [integrant.core :as ig]
            [ring.middleware.session.store :as store]))

(deftest cookie-store-test
  (testing "bad :key option"
    (is (thrown? clojure.lang.ExceptionInfo
                 (ig/init {:duct.session-store/cookie {:key "x"}}))))
  (testing "init produces session store"
    (let [system (ig/init {:duct.session-store/cookie {}})]
      (is (satisfies? store/SessionStore
                      (:duct.session-store/cookie system))))
    (let [system (ig/init {:duct.session-store/cookie
                           {:key "00112233445566778899aabbccddeeff"}})]
      (is (satisfies? store/SessionStore
                      (:duct.session-store/cookie system)))))
  (testing "resume uses old store if options unchanged"
    (let [config {:duct.session-store/cookie {}} 
          system (ig/init config)]
      (ig/suspend! system)
      (let [new-system (ig/resume config system)]
        (is (identical? (:duct.session-store/cookie system)
                        (:duct.session-store/cookie new-system))))))
  (testing "resume uses new store if options changed"
    (let [config {:duct.session-store/cookie {}} 
          system (ig/init config)]
      (ig/suspend! system)
      (let [new-config {:duct.session-store/cookie
                        {:key "00112233445566778899aabbccddeeff"}}
            new-system (ig/resume new-config system)]
        (is (satisfies? store/SessionStore
                        (:duct.session-store/cookie new-system)))
        (is (not (identical? (:duct.session-store/cookie system)
                             (:duct.session-store/cookie new-system))))))))
