(ns flugger.message
  (:import (flugger.protobuf RPC$Request
                             RPC$AuthRequest))
  (:require [clojure.java.io :refer [input-stream output-stream]]
            [protobuf.core :as protobuf]))

(defn text->stream [text]
  (input-stream (.getBytes text)))

(defn msg->bytes [type data]
  (-> (protobuf/create type data)
      protobuf/->bytes))

(defn request->bytes [req-meta type args]
  (let [args-bytes (msg->bytes type args)
        req-data (assoc req-meta :args args-bytes)]
    (msg->bytes RPC$Request req-data)))

(defn bytes->msg [bytes type]
  (let [req (protobuf/create type)]
    (protobuf/bytes-> req bytes)))

(defn bytes->request [bytes arg-type-resolver]
  (let [req (bytes->msg bytes RPC$Request)
        args-type (arg-type-resolver (:call req))
        args-bytes (-> req :args .toByteArray)
        args (bytes->msg args-bytes args-type)]
    (merge {} req {:args args})))
