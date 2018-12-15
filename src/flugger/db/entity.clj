(ns flugger.db.entity
  (:require [honeysql.core :as sql]
            [honeysql.helpers :as q]
            [flugger.db.client :as db]
            [flugger.uuid :as uuid]
            [manifold.deferred :refer [chain] :as d]))

(defn- sql-now []
  (-> (java.util.Date.)
      (.getTime)
      (java.sql.Timestamp.)))

(defn by-page [sql start count]
  (-> sql
      (q/merge-where [:> :order_id (or start 0)])
      (q/merge-order-by [:order_id :asc])
      (q/limit (or count 10))))

(defn by-page-reverse [sql start count]
  (-> sql
      (q/merge-where [:< :order_id (or start "Infinity")])
      (q/merge-order-by [:order_id :desc])
      (q/limit (or count 10))))

(defn select-all [table]
  (-> (q/select :*)
      (q/from table)))

(defn select-related [table fk fv]
  (-> (select-all table)
      (q/merge-where [:= fk fv]
                     [:enabled true])))

(defn get-by-id [table id]
  (-> (q/select :*)
      (q/from table)
      (q/merge-where [:= :id id]
                     [:= :enabled true])
      (db/query-one)))

(defn get-by-external-id [table service-id external-id]
  (-> (select-related table :service_id service-id)
      (q/merge-where [:= :external_id external-id])
      (db/query-one)))

(defn insert! [table props]
  (-> (db/insert! {:table (name table) :returning "*"} props)
      (db/fetch-one)))

(defn update! [table id changes]
  (-> (db/update! {:table (name table)
                   :where ["id = $1" id]
                   :returning "*"}
                  (assoc changes :updated_at (sql-now)))
      (db/fetch-one)))

(defn archive [table id]
  (update! table id {:enabled false}))

(defn get-page [table & {:keys [start-from count]}]
  (-> (select-all table)
      (by-page start-from count)
      (db/query-many)))

(defn get-page-related [table fk fv & {:keys [start-from count]}]
  (-> (select-related table fk fv)
      (by-page start-from count)
      (db/query-many)))

(defn get-page-related-reverse [table fk fv & {:keys [start-from count]}]
  (-> (select-related table fk fv)
      (by-page-reverse start-from count)
      (db/query-many)))
