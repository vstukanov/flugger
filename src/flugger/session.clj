(ns flugger.session
  (:require [flugger.uuid :as uuid]))

(def sessions (atom {}))
(def ^:dynamic *current-session* nil)

(defmacro with-session [ch session-name & body]
  `(let [~session-name (session-init! ~ch)] ~@body))

(defn session-create []
  {:id (uuid/generate-random)})

(defn session-get [channel]
  (get @sessions channel))

(defn session-init! [channel]
  (-> (swap! sessions update channel #(if (:id %) % (session-create)))
      (get channel)))

(defn session-close! [channel]
  (swap! sessions dissoc channel))

(defn session-assoc! [channel & args]
  (swap! sessions update channel #(apply assoc % args)))

(defn session-reset! []
  (reset! sessions {}))

(defn session->str [session]
  (apply format "[%s %s %s %s]"
         (->> [:id :service :user :role]
              (map #(get session % "nil")))))




