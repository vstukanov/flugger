(ns flugger.db.entity
  (:require [honeysql.core :as sql]
            [honeysql.helpers :as q]
            [flugger.db.client :as db]
            [flugger.uuid :as uuid]
            [manifold.deferred :refer [chain] :as d]))

(defn- prn-context [ctx]
  (prn ctx)
  ctx)

(defn- sql-now []
  (-> (java.util.Date.)
      (.getTime)
      (java.sql.Timestamp.)))

(defn get-first-row [defer]
  (chain defer #(:rows %) first))

(defn pg-format [ctx & {:keys [params]}]
  (sql/format ctx
              :parameterizer :postgresql
              :params params))

(defn insert! [table props]
  (-> (db/insert! {:table (name table) :returning "*"} props)
      (get-first-row)))

(defn get-by-id [table id]
  (-> (q/select :*)
      (q/from table)
      (q/where [:= :id :?id]
               [:= :enabled true])
      (pg-format :params {:id id})
      (db/execute!)
      (get-first-row)))

(defn update! [table id changes]
  (-> (db/update! {:table (name table)
                   :where ["id = $1" id]
                   :returning "*"}
                  (assoc changes :updated_at (sql-now)))
      (get-first-row)))

(defn archive [table id]
  (update-row table id {:enabled false}))

(defn get-page [table & {:keys [start-from count] :or {start-from 0 count 10}}]
  (-> (q/select :*)
      (q/from table)
      (q/where [:> :order_id start-from])
      (q/limit count)
      (q/order-by :order_id)
      (pg-format)
      (prn-context)
      (db/query!)))
