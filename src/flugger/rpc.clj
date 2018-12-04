(ns flugger.rpc)

(def registry (atom {}))
(def call-counter (atom 0))

(defmacro defapi [name rpc-meta args & body]
  `(def ~name (with-meta (fn ~args ~@body) (assoc ~rpc-meta :rpc-call true))))

(defn- rpc-call? [call]
  (contains? (-> call var-get meta) :rpc-call))

(defn register-ns [ns]
  (let [ns-calls (vals (ns-publics ns))
        rpc-calls (filter rpc-call? ns-calls)]
    
    (clojure.pprint/pprint rpc-calls)))
