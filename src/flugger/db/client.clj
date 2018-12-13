(ns flugger.db.client
  (:require [clojure.java.jdbc :as jdbc]))

(def ^:dynamic *db* {:dbtype "postgresql"
                     :dbname "flugger"
                     :host "localhost"
                     :user "flugger"
                     :password "flugger"})

(defn query [& args]
  (apply jdbc/query *db* args))

(defn exec! [& args]
  (apply jdbc/execute! *db* args))
