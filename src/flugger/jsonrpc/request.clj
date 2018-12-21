(ns flugger.jsonrpc.request
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]
            [flugger.jsonrpc.errors :as errors]
            [struct.core :as st]))

(def +msg-spec+ {:jsonrpc [st/required
                           st/string
                           [st/member ["2.0"] :message "must be exactly 2.0"]]
                 :method [st/required st/string]
                 :params [st/map]
                 :id [st/number]})

(defn parse-body [req]
  (update req :body #(-> (io/reader %)
                         (json/read :key-fn keyword))))

(defn validate [ctx]
  (let [data (-> ctx :request :body)
        [err, _] (st/validate data +msg-spec+)]
    (if (some? err)
      (throw (-> err (json/write-str) (errors/parse-error ctx)))
      ctx)))
