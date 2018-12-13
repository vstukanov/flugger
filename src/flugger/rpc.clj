(ns flugger.rpc
  (:require [yaml.core :as yaml]))

(def registry (atom {}))
(def call-counter (atom 0))

(defmacro defapi [name rpc-meta args & body]
  `(def ~name (with-meta (fn ~args ~@body) (assoc ~rpc-meta :rpc-call true))))

(defn load-descriptor-table [path]
  (let [table (yaml/from-file path)]
    (prn table)))

(defn save-descriptor-table [data path]
  (let [content (yaml/generate-string data :dumper-options {:flow-style :block})]
    (spit path content)))

(defn- get-call-meta [call]
  (-> call var-get meta))

(defn- rpc-call? [call]
  (contains? (get-call-meta call) :rpc-call))

(defn register-ns [ns]
  (let [ns-calls (into [] (ns-publics ns))
        rpc-calls (filter #(rpc-call? (second %)) ns-calls)
        table (map #(merge {}
                           (get-call-meta (second %))
                           {:ns (ns-name ns)
                            :name (first %)})
                   rpc-calls)]
    (clojure.pprint/pprint table)))
