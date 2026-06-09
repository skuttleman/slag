(ns slag.test.cljs.executor
  (:require
    [clojure.test :as t]
    [slag.test.utils.executor :as exec]
    slag.utils.fns-test
    slag.utils.keywords-test
    slag.utils.maps-test))

(defn ^:export test! []
  (exec/run-tests! (fn []
                     (t/run-tests
                       'slag.utils.fns-test
                       'slag.utils.keywords-test
                       'slag.utils.maps-test))))
