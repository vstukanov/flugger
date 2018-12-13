(ns flugger.core
  (:require
   [clojure.tools.logging :as log]
   [flugger.session :refer [with-session] :as session]
   [flugger.ws :as ws]
   [flugger.rpc :as rpc :refer [defapi]]
   [flugger.uuid :as uuid])
  (:gen-class))

(defn filter-keys
  ([model keys] (apply dissoc model keys))
  ([keys] #(filter-keys % keys)))

(defn serialize-keys
  ([model mapper] (reduce #(update %1 %2 (get mapper %2)) model (keys mapper)))
  ([mapper] #(serialize-keys % mapper)))

(defn inst->ms [time]
  (.getTime time))

(defapi auth-new
  {:role :*
   :request "AuthRequest"}
  []
  (print "Hello"))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (rpc/register-ns 'flugger.core)
  (ws/ws-start-server))
