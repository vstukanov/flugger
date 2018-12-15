(ns flugger.model.service
  (:require [flugger.db.entity :as entity]
            [flugger.uuid :as uuid]))

(defn get [id]
  (entity/get-by-id :services id))

(defn- generate-random-key []
  (-> (uuid/generate-random)
      (uuid/->string)))

(defn get-page [& opts]
  (apply entity/get-page :services opts))

(defn create [name]
  (entity/insert! :services {:name name
                             :private_key (generate-random-key)}))

(defn update [name]
  (entity/update! :services {:name name}))

(def test-service (first @(get-page :start-from 0 :count 1)))
