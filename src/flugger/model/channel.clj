(ns flugger.model.channel
  (:require [flugger.db.entity :as entity]
            [flugger.db.client :as db]
            [manifold.deferred :refer [chain]]
            [honeysql.helpers :as q]
            [flugger.events :as events]))

(defn get [id]
  (entity/get-by-id :channels id))

(defn get-by-external-id [service-id external-id]
  (entity/get-by-external-id :channels
                             service-id
                             external-id))

(defn get-page [service-id & props]
  (apply entity/get-page-related
         :channels
         :service_id service-id
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

(defn is-member? [id user-id]
  (chain (-> (q/select :*)
             (q/from :members)
             (q/where [:= :user_id user-id]
                      [:= :channel_id id])
             (db/query-one))
         #(if (some? %) true (throw (Exception. "User does not belongs to channel.")))))

(defn send-message [id service-id user-id message]
  (chain (is-member? id user-id)
         (fn [_] (entity/insert! :messages {:service_id service-id
                                            :channel_id id
                                            :user_id user-id
                                            :message message}))
         (events/emit-event "message:send")))

(defn get-messages [id & props]
  (apply entity/get-page-related-reverse
         :messages
         :channel_id id
         props))

(defn add-member [id user-id]
  (chain (entity/insert! :members {:channel_id id
                                   :user_id user-id})
         (events/emit-event "member:invited")))

(defn get-members [id & opts]
  (apply entity/get-many-to-many
         :users
         :members
         :user_id
         [:channel_id id] opts))
