(ns flugger.http
  (:require [manifold.deferred :as d :refer [chain on-realized]]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [aleph.http :as http]
            [ring.middleware.json :as jm]
            [ring.util.response :as r]
            [clojure.data.json :as json]
            [struct.core :as st]
            [flugger.jsonrpc.request :as request]
            [flugger.jsonrpc.response :as response]
            [flugger.jsonrpc.errors :as errors]
            [flugger.model.channel]
            [flugger.uuid :as uuid]
            [flugger.rpc :refer [make-resolver]]))

(defonce server (atom nil))

(defn create-context [req]
  {:request req
   :message (-> req :body)
   :id (-> req :body :id)
   :uuid (uuid/generate-random)
   :response nil})

(def api-resolver (make-resolver ['flugger.model.channel]))

(defn- invoke-method [ctx]
  (let [params (-> ctx :message :params)
        m (-> ctx :message :method)
        api (api-resolver m)
        _ (log/debug "api" api)
        [pe mp] (st/validate params (:spec api) {:strip true})]
    (if-not (some? api)
      (throw (errors/method-not-found-error ctx)))
    (if (some? pe)
      (throw (errors/invalid-params-error (json/write-str pe) ctx)))

    (log/debug "invoke api: " mp)
    (chain ((:fn api) mp)
           #(assoc-in ctx [:response :data] %))))

(defn- json-rpc-handler [req]
  (log/debug "Income request: " req)

  (let [ctx (-> req request/parse-body create-context)]
    (log/debug "Initiated context: " ctx)
    (-> (chain ctx
               request/validate
               invoke-method
               response/build
               #(do (log/debug "response: " %) %))
        (d/catch Exception #(response/make-error % ctx)))))

(defn stop-server []
  (when-not (nil? @server)
    (log/info "stop listening")
    (.close @server)
    (Thread/sleep 300)
    (reset! server nil)))

(defn start-server [port]
  (stop-server)
  (reset! server (http/start-server #'json-rpc-handler {:port port}))
  (log/infof "start listening :%d" (.port @server)))
