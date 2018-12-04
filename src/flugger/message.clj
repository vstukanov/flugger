(ns flugger.message
  (:import (flugger.protobuf RPC$Request))
  (:require [clojure.java.io :refer [input-stream]]
            [protobuf.core :as protobuf]))

(defn text->stream [text]
  (input-stream (.getBytes text)))

(defn unmarshall [msg]
  (let [msg-stream (text->stream msg)
        empty-request (protobuf/create RPC$Request)]
    (protobuf/read empty-request message-stream)))
