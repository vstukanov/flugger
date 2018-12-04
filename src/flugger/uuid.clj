(ns flugger.uuid)

(defn generate-random []
  (java.util.UUID/randomUUID))

(defn <-string [uuid]
  (java.util.UUID/fromString uuid))

(defn ->string [uuid]
  (.toString uuid))
