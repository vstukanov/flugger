(ns flugger.events
  (:require [clojure.tools.logging :as log]))

(defn emit-event
  ([name] #(emit-event name %))
  ([name payload]
   (log/infof "event: [%s] %s" name payload)
   (identity payload)))
