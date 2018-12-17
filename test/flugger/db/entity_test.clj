(ns flugger.db.entity-test
  (:require [clojure.test :refer :all]
            [flugger.db.client :as db]
            [flugger.model.service :as service]
            [honeysql.helpers :as q]))

(def test-db {:database "flugger-test"
              :hostname "localhost"
              :username "flugger"
              :password "flugger"})

(defn clean-db []
  @(db/truncat-table! :messages)
  @(db/truncat-table! :channels)
  @(db/truncat-table! :users)
  @(db/truncat-table! :services))

;; TODO add database creation on demand.
(defn with-test-db [f]
  (with-redefs [db/db (db/open! test-db)]
    (clean-db)
    (f)
    (clean-db)
    (db/close!)))
