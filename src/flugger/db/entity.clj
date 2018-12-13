(ns flugger.db.entity
  (:require [honeysql.core :as sql]
            [honeysql.helpers :as q]
            [flugger.db.client :as db]
            [flugger.uuid :as uuid]))

(defn- sql-now []
  (-> (java.util.Date.)
      (.getTime)
      (java.sql.Timestamp.)))

(defn insert [table props]
  (-> (q/insert-into table)
      (q/values [props])
      (sql/format)
      (db/exec!)))

(defn get-by-id [table id]
  (-> (q/select :*)
      (q/from table)
      (q/where [:= :id :?id]
               [:= :enabled true])
      (sql/format :params {:id id})
      (db/query)
      (first)))

(defn insert-and-get [table props]
  (let [id (uuid/generate-random)]
    (insert table (assoc props :id id))
    (get-by-id table id)))

(defn update-item [table id changes]
  (-> (q/update table)
      (q/sset (assoc changes :updated_at (sql-now)))
      (q/where [:= :id id]
               [:= :enabled true])
      (sql/format)
      (db/exec!)))

(defn delete [table id]
  (update-item table id {:enabled false}))

(defn update-and-get [table id changes]
  (update-item table id changes)
  (get-by-id table id))

(defn list [table & {:keys [start-from count]}]
  (-> (q/select :*)
      (q/from table)
      (q/where [:> :order_id start-from])
      (q/limit count)
      (q/order-by :order_id)
      (sql/format)
      (db/query)))
