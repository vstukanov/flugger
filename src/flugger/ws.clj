(ns flugger.ws
  (:require [flugger.session :as session]
            [clojure.tools.logging :as log]
            [org.httpkit.server :as server]))

(defonce *server* (atom nil))
             
(defn ws-handle-close [channel session status]
  (log/info "session closed" status session)
  (session/session-close! channel))

(defn ws-handle-receive [channel session data]
  (log/debug "msg" session data))

(defn- ws-bind-handler [ch method]
  (fn [& args]
    (session/with-session ch session
      (apply method ch session args))))

(defn ws-handler [request]
  (server/with-channel request ch
    (server/on-close ch (ws-bind-handler ch ws-handle-close))
    (server/on-receive ch (ws-bind-handler ch ws-handle-receive))))

(defn ws-stop-server []
  (when-not (nil? @*server*)
    (log/info "stop existing server")
    (@*server* :timeout 100)
    (session/session-reset!)
    (reset! *server* nil)))

(defn ws-start-server []
  (if (nil? @*server*)
    (do (log/info "Start WebSocket server at port 9090")
        (reset! *server* (server/run-server ws-handler {:port 9090})))
    (log/warn "Server already started.")))

(defn ws-restart-server []
  (ws-stop-server)
  (ws-start-server))
  
