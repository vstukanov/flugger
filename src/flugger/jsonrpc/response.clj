(ns flugger.jsonrpc.response
  (:require [clojure.tools.logging :as log]
            [clojure.data.json :as json]
            [ring.util.response :as r]
            [flugger.uuid :as uuid])
  (:import (java.io PrintWriter)))

(defn- write-timestamp [ts ^PrintWriter out]
  (.print out (.getTime ts)))

(defn- write-uuid [id ^PrintWriter out]
  (.print out (json/write-str (uuid/->string id))))

(extend java.sql.Timestamp json/JSONWriter
        {:-write write-timestamp})

(extend java.util.UUID json/JSONWriter
        {:-write write-uuid})

(defn make-response [data status]
  (-> (r/response  (json/write-str data))
      (r/status status)
      (r/content-type "application/json-rpc")))

(defn make-body [& {:keys[id result error]}]
  (cond-> {:jsonrpc "2.0"}
    (some? id) (assoc :id id)
    (some? result) (assoc :result result)
    (some? error) (assoc :error error)))

(defn build [ctx]
  (let [id (-> ctx :id)]
    (make-response (make-body :id id :result (-> ctx :response :data))
                   (if id 200 204))))

(defn make-error [err ctx]
  (log/error err)
  (let [err-info (ex-data err)
        handled? (some? (:rpc err-info))
        context (or (:context err-info) ctx)
        em (.getMessage err)]
    (if-not handled?
      (log/error err "Unhandled error:")
      (log/debug "Handled error:" em))
    (make-response (make-body :id (-> context :id)
                              :error {:code (-> err-info :code)
                                      :message em})
                   (or (:http-code err-info) 500))))
