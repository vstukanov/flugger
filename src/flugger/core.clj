(ns flugger.core
  (:require
   [clojure.tools.logging :as log]
   [flugger.session :refer [with-session] :as session]
   [flugger.ws :as ws]
   [flugger.rpc :as rpc :refer [defapi]])
  (:gen-class))

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
