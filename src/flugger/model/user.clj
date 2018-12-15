(ns flugger.model.user
  (:require [flugger.db.entity :as entity]
            [flugger.events :as events]
            [manifold.deferred :refer [chain] :as d]))

(defn get [id]
  (entity/get-by-id :users id))

(defn get-by-external-id [service-id external-id]
  (entity/get-by-external-id :users service-id external-id))

(defn get-page [service-id & props]
  (apply entity/get-page-related
         :users
         :service_id service-id
         props))

(defn create [props]
  (let [k [:name :service_id :attributes :external_id]
        p (select-keys props k)]
    (chain (entity/insert! :users p)
           (events/emit-event "user:created"))))

(defn update [id props]
  (let [k [:name :attributes :external_id]
        p (select-keys props k)]
    (chain (entity/update! :users id p)
           (events/emit-event "user:updated"))))

(defn archive [id]
  (chain (entity/archive :users id)
         (events/emit-event "user:archived")))
