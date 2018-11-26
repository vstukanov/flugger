(ns flugger.core
  (:require
   [clojure.java.jdbc :as j]
   [honeysql.core :as sql]
   [honeysql.helpers :refer :all])
  (:gen-class))

(def db-spec {:dbtype "postgresql"
              :dbname "flugger"
              :host "localhost"
              :user "flugger"
              :password "flugger"})

(def db-query (partial j/query db-spec))
(def db-exec! (partial j/execute! db-spec))

(defn uuid-generate-random []
  (.toString (java.util.UUID/randomUUID)))

(defn create-service [name]
  (let [uuid (uuid-generate-random)]
    (-> (insert-into :services)
        (columns :id :name)
        (values [[uuid name]])
        sql/format
        println)
    (j/get-by-id db-spec "services" uuid)))

(defn get-by-id [table id]
  (-> (select :*)
      (from (keyword table))
      (where [:= :id :?id])
      (sql/format :params {:id id})
      db-query))

(j/query db-spec
         ["SELECT * FROM services;"])

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
