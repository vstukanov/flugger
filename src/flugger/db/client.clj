(ns flugger.db.client
  (:require [postgres.async :as psql]
            [manifold.deferred :as d :refer [chain]]
            [clojure.tools.logging :as log]
            [honeysql.core :as sql]))

(def db-conf {:database "flugger"
              :hostname "localhost"
              :username "flugger"
              :password "flugger"})

(defn open! [conf]
  (psql/open-db conf))

(def db (open! db-conf))

(defn close! []
  (psql/close-db! db))

(defmacro defdefer [name arg-list f]
  `(defn ~name
    (~arg-list
     (let [defer# (d/deferred)
           handler# #(if %2 (d/error! defer# %2)
                         (d/success! defer# %1))
           arg# (concat ~arg-list [handler#])]
       (log/debug ~arg-list)
       (apply ~f db arg#)
       defer#))))

(defdefer execute! [sql] psql/execute!)
(defdefer query! [sql] psql/query!)
(defdefer insert! [dt m] psql/insert!)
(defdefer update! [dt m] psql/update!)

(defn format [sql & {:keys [params]}]
  (sql/format sql
              :parameterizer :postgresql
              :params params))

(defn fetch-many [ctx]
  (chain ctx #(:rows %)))

(defn fetch-one [ctx]
  (chain ctx #(:rows %) first))

(defn query-many [sql & {:keys [params]}]
  (-> sql
      (format :params params)
      (execute!)
      (fetch-many)))

(defn query-one [sql & {:keys [params]}]
  (-> sql
      (format :params params)
      (execute!)
      (fetch-one)))

(defn truncat-table! [table]
  (execute! [(clojure.core/format "TRUNCATE TABLE %s CASCADE;" (name table))]))
