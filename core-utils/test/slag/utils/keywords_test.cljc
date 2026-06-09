(ns slag.utils.keywords-test
  (:require
    [clojure.test :refer [deftest is testing]]
    [slag.utils.keywords :as kw]))

(deftest str-test
  (testing "when passing a namespaced keyword"
    (testing "returns the stringified keyword"
      (is (= "foo/bar" (kw/str :foo/bar)))))

  (testing "when passing a name-only keyword"
    (testing "returns the stringified keyword"
      (is (= "baz" (kw/str :baz)))))

  (testing "when passing nil"
    (testing "returns nil"
      (is (nil? (kw/str nil))))))
