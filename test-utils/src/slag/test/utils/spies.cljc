(ns slag.test.utils.spies)

(defn create [f]
  (let [state (atom {::calls []})]
    (with-meta (fn [& args]
                 (swap! state update ::calls conj args)
                 (apply f args))
               {::state state})))

(defn calls [spy]
  (-> spy meta ::state deref ::calls))

(defn called-with? [spy args]
  (boolean (first (filter #{args} (calls spy)))))
