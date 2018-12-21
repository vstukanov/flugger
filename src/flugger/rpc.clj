(ns flugger.rpc
  (:require [clojure.set :refer [index]]))

(defmacro defapi [name rpc-meta args & body]
  `(def ~name (with-meta (fn [{:keys ~args}] ~@body) (assoc ~rpc-meta :rpc-call true))))

(defn- api-meta [call]
  (-> call var-get meta))

(defn- rpc-call? [call]
  (contains? (api-meta call) :rpc-call))

(defn- ns-api [ns]
  (->> (ns-publics ns)
       (into [])
       (filter #(rpc-call? (second %)))
       (map #(merge {:method (str (ns-name ns) "/" (first %))
                     :fn (ns-resolve ns (symbol (first %)))}
                    (api-meta (second %))))))

(defn- make-api-table [ns-list]
  (->> ns-list
       (map ns-api)
       (reduce concat)))

(defn make-resolver [ns-list]
  (let [t (make-api-table ns-list)]
    (fn [method]
      (-> (filter #(= (:method %) method) t)
          (first)))))
