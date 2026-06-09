(ns slag.test.utils.async
  #?(:cljs (:require-macros slag.test.utils.async))
  (:require
    [clojure.core.async :as async]
    clojure.test))

(defmacro async [cb & body]
  (if (:ns &env)
    `(clojure.test/async ~cb ~@body)
    `(let [prom# (promise)
           ~cb #(deliver prom# nil)
           result# (do ~@body)]
       (when (= ::timeout (deref prom# 5000 ::timeout))
         (throw (ex-info "failed to complete async test within timeout" {})))
       result#)))

(defmacro <!
  ([ch]
   `(<! ~ch 500))
  ([ch ms]
   `(<! ~ch ~ms ::timeout))
  ([ch ms or-else]
   `(async/alt! ~ch ([v#] v#)
                (async/timeout ~ms) ~or-else)))

#?(:clj
   (defn <!!
     ([ch]
      (<!! ch 500))
     ([ch ms]
      (<!! ch ms ::timeout))
     ([ch ms or-else]
      (async/alt!! ch ([v] v)
                   (async/timeout ms) or-else))))
