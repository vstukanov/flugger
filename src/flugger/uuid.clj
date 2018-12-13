(ns flugger.uuid)

(defn generate-random []
  (java.util.UUID/randomUUID))

(defn <-string [uuid]
  (java.util.UUID/fromString uuid))

(defn ->bytes [uuid]
  (-> (java.nio.ByteBuffer/wrap (byte-array 16))
      (.putLong (.getMostSignificantBits uuid))
      (.putLong (.getLeastSignificantBits uuid))
      (.array)))

(defn <-bytes [bytes]
  (let [bb (java.nio.ByteBuffer/wrap bytes)
        h (.getLong bb)
        l (.getLong bb)]
    (new java.util.UUID h l)))

(defn ->string [uuid]
  (.toString uuid))
