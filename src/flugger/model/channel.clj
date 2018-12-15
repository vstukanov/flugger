(ns flugger.model.channel
  (:require [flugger.db.entity :as entity]
            [manifold.deferred :refer [chain]]
            [flugger.events :as events]))

(defn get [id]
  (entity/get-by-id :channels id))

(defn get-by-external-id [service-id external-id]
  (entity/get-by-external-id :channels service-id external-id))

(defn get-page [service-id & props]
  (apply entity/get-page
         :channels
         :where [:= :service_id service-id]
         props))

(defn create [props]
  (let [k [:title :service_id :attributes :external_id]
        p (select-keys props k)]
    (chain (entity/insert! :channels p)
           (events/emit-event "channel:created"))))

(defn update [id props]
  (let [k [:title :attributes :external_id]
        p (select-keys props k)]
    (chain (entity/update! :channels id p)
           (events/emit-event "channel:updated"))))

(defn archive [id]
  (chain (entity/archive :channels id)
         (events/emit-event "channel:archived")))
