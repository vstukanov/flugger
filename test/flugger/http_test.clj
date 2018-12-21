(ns flugger.http_test
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [struct.core :as st]
            [manifold.deferred :as d :refer [chain]]
            [aleph.http :as client]
            [flugger.http :as http]
            [flugger.uuid :as uuid]))

(def port 12070)
(defn with-http [f]
  (http/start-server port)
  (Thread/sleep 300)
  (f))

(use-fixtures :once with-http)

(def url (format "http://127.0.0.1:%s/" port))

(defn- post [data & options]
  (chain (client/post url {:body (json/write-str data)
                           :throw-exceptions? false})
         parse-body))

(defn- parse-body [resp]
  (update resp :body #(-> (io/reader %)
                          (json/read :key-fn keyword))))

(defn- call [method params & {:keys [id]}]
  (post {:jsonrpc "2.0"
         :method method
         :id (or id (rand-int 1000))
         :params params}))

(def mock-api
  {:echo {:spec {:foo [st/required st/string]}
          :fn (fn [opts] opts)}})

(defn mock-resolver [m] (get mock-api (keyword m)))

(deftest rpc
  (with-redefs [flugger.http/api-resolver mock-resolver]
    (testing "Invalid messages should be aborted"
      (let [r @(post {:foo "bar"})]
        (is (= 500 (-> r :status)))
        (is (= -32700 (-> r :body :error :code)))))

    (testing "Normal flow"
      (let [id (rand-int 1000)
            r @(call "echo" {:foo "bar"} :id id)]
        (is (= 200 (:status r)))
        (is (= id (-> r :body :id)))
        (is (= "bar" (-> r :body :result :foo)))))))
