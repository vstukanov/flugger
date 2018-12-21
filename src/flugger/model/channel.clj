(ns flugger.model.channel
  (:require [flugger.db.entity :as entity]
            [flugger.db.client :as db]
            [flugger.rpc :refer [defapi]]
            [manifold.deferred :refer [chain]]
            [honeysql.helpers :as q]
            [flugger.events :as events]
            [struct.core :as st]))

(def +uuid!+ [st/required st/uuid-str])
(def +page-size+ [st/integer [st/in-range 1 50]])
(def +page-offset+ [st/integer])
(def +string+ [st/string])
(def +string!+ [st/required st/string])

(defapi get-by-id
  {:spec {:id +uuid!+}}
  [id] (entity/get-by-id :channels id))

(defapi get-by-external-id
  {:spec {:service-id +uuid!+
          :external-id +string!+}}
  [service-id external-id]
  (entity/get-by-external-id :channels
                             service-id
                             external-id))

(defapi get-page
  {:spec {:service-id +uuid!+
          :page-offset +page-offset+
          :page-size +page-size+}}
  [service-id page-offset page-size]
  (entity/get-page-related
         :channels
         :service-id service-id
         page-offset page-size))

(defapi create
  {:spec {:title +string!+
          :service-id +uuid!+
          :attributes +string+
          :external-id +string+}}
  [title service-id attributes external-id]
  (chain (entity/insert! :channels {:title title
                                    :service_id service-id
                                    :attributes attributes
                                    :external_id external-id})
         (events/emit-event "channel:created")))

(defapi update
  {:spec {:id +uuid!+
          :title +string+
          :attributes +string+
          :external-id +string+}}
  [id title attributes external-id]
  ;; TODO filter empty fields
  (chain (entity/update! :channels id {:title title
                                       :attributs attributes
                                       :external_id external-id})
         (events/emit-event "channel:updated")))

(defapi archive
  {:spec {:id +uuid!+}}
  [id]
  (chain (entity/archive :channels id)
         (events/emit-event "channel:archived")))

(defn is-member? [id user-id]
  (chain (-> (q/select :*)
             (q/from :members)
             (q/where [:= :user_id user-id]
                      [:= :channel_id id])
             (db/query-one))
         #(if (some? %) true (throw (Exception. "User does not belongs to channel.")))))

(defapi send-message
  {:spec [:id +uuid!+
          :service-id +uuid!+
          :user-id +uuid!+
          :message +string+]}
  [id service-id user-id message]
  (chain (is-member? id user-id)
         (fn [_] (entity/insert! :messages {:service_id service-id
                                            :channel_id id
                                            :user_id user-id
                                            :message message}))
         (events/emit-event "message:send")))

(defapi get-messages
  {:spec {:id +uuid!+
          :page-offset +page-offset+
          :page-size +page-size+}}
  [id page-offset page-size]
  (entity/get-page-related-reverse
         :messages
         :channel_id id
         page-offset page-size))

(defapi add-member
  {:spec {:id +uuid!+
          :user-id +uuid!+}}
  [id user-id]
  (chain (entity/insert! :members {:channel_id id
                                   :user_id user-id})
         (events/emit-event "member:invited")))

(defapi get-members
  {:spec {:id +uuid!+
          :page-offset +page-offset+
          :page-size +page-size+}}
  [id page-offset page-size]
  (entity/get-many-to-many
         :users
         :members
         :user_id
         [:channel_id id]
         page-offset page-size))
