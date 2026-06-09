(ns slag.utils.fns-test
  (:require
    [clojure.test :refer [deftest is testing]]
    [slag.test.utils.spies :as spies]
    [slag.utils.fns :as fns]))


(deftest apply-all-test
  (testing "when applying fns"
    (let [spy1 (spies/create vector)
          spy2 (spies/create vector)
          f (fns/apply-all spy1 nil spy2)
          result (f :foo :bar)]
      (testing "calls all non-nil spies"
        (is (spies/called-with? spy1 [:foo :bar]))
        (is (spies/called-with? spy2 [:foo :bar])))

      (testing "returns nil"
        (is (nil? result))))))

(deftest safe-comp-test
  (testing "when composing fns with nil"
    (let [f (fns/safe-comp inc nil inc)]
      (testing "and when calling the fn"
        (let [result (f 4)]
          (testing "calls the composition"
            (is (= 6 result))))))))

(deftest smap-test
  (testing "is a -> compatible mapper"
    (is (= {:foo [{:a 1} {:a 1 :b 2}]}
           (update {:foo [{} {:b 2}]} :foo fns/smap assoc :a 1)))))
