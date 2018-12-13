(ns flugger.model.service
  (:require [flugger.db.entity :as entity]
            [flugger.uuid :as uuid]))

(defn- generate-random-key []
  (-> (uuid/generate-random)
      (uuid/->string)))

(defn list [& opts]
  (apply entity/list :services opts))

(defn create [name]
  (entity/insert-and-get :services {:name name
                                    :private_key (generate-random-key)}))

(defn update [name]
  (entity/update-and-get :services {:name name}))
