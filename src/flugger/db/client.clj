(ns flugger.db.client
  (:require [postgres.async :as psql]
            [manifold.deferred :as d]
            [clojure.core.async :as async :refer [<!! <! >! >!!]]))

(def db (psql/open-db {:database "flugger"
                       :hostname "localhost"
                       :username "flugger"
                       :password "flugger"}))

(defmacro defdefer [name arg-list f]
  `(defn ~name
    (~arg-list
     (let [defer# (d/deferred)
           handler# #(if %2 (d/error! defer# %2)
                         (d/success! defer# %1))
           arg# (concat ~arg-list [handler#])]
       (apply ~f db arg#)
       defer#))))

(defdefer execute! [sql] psql/execute!)
(defdefer query! [sql] psql/query!)
(defdefer insert! [dt m] psql/insert!)
(defdefer update! [dt m] psql/update!)
