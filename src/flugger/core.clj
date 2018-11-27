(ns flugger.core
  (:require
   [clojure.java.jdbc :as j]
   [honeysql.core :as sql]
   [org.httpkit.server :refer [run-server
                               with-channel
                               on-close
                               on-receive
                               send!]])
  (:gen-class))

(def db-spec {:dbtype "postgresql"
              :dbname "flugger"
              :host "localhost"
              :user "flugger"
              :password "flugger"})

(def db-query (partial j/query db-spec))
(def db-exec! (partial j/execute! db-spec))

(defn uuid-generate-v4 []
  (java.util.UUID/randomUUID))

(defn uuid-from-string [uuid]
  (java.util.UUID/fromString uuid))

(defn uuid-to-string [uuid]
  (.toString uuid))

(def channel-states (atom {}))

(defn rpc-ws-handler [request]
  (with-channel request channel
    (on-close channel
              (fn [status]
                (println "channel connection closed.")
                (swap! channel-states dissoc channel)))
    (on-receive channel
                (fn [data]
                  (println "channel data received.")
                  (let [state (get @channel-states channel {:count 0})]
                    (println state)
                    (send! channel (format "%s, count: %d" data (:count state)))
                    (swap! channel-states assoc channel (update state :count inc)))))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Start WebSocket server at :9090")
  (run-server rpc-ws-handler {:port 9090}))
