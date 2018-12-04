(ns flugger.ws-client
  (:require [clojure.tools.logging :as log]
            [gniazdo.core :as ws]))

(def ^:dynamic *client* nil)

(defn create-local-client [& args]
  (apply ws/connect "ws://127.0.0.1:9090" args))

(defmacro with-client [name uri opts & body]
  `(let [ws-client# (ws/client)]
     (.start ws-client#)
     (let [~name (ws/connect ~uri :client ws-client#)]
       (do ~@body)
       (ws/close ~name))))
  


