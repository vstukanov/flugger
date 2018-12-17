(ns flugger.model.channel_test
  (:require [clojure.test :refer :all]
            [manifold.deferred :as d]
            [flugger.model.service :as service]
            [flugger.model.user :as user]
            [flugger.model.channel :refer :all]
            [flugger.db.entity-test :refer [with-test-db]]))

(use-fixtures :once with-test-db)

(defn- do-seq
  ([fn] #(do-seq fn %))
  ([fn col] (map (comp deref fn) col)))

(defn- send-message-bunch [ml cid sid uid]
  (map (comp deref #(send-message cid sid uid %)) ml))

(deftest channels
  (let [srv @(service/create "test-service")
        sid (:id srv)
        u @(user/create {:name "test-user"
                        :service_id sid
                         :external_id "user-uniqu-name"})
        uid (:id u)
        ch @(create {:title "test-channel"
                     :service_id sid
                     :external_id "channel-unique-name"})
        cid (:id ch)
        m1 @(send-message cid sid uid "test-message")]

    (testing "Models creation"
      (is (= "test-service" (:name srv)))
      (is (= "test-user" (:name u)))
      (is (= "test-channel" (:title ch))))

    (testing "Send message"
      (is (= "test-message" (:message m1))))

    (testing "Retriev messages"
      (let [msgs '("1" "2" "3" "4" "5")
            sm (doall (for [m msgs] @(send-message cid sid uid m)))
            rm @(get-messages cid :count 5)
            m2 @(get-messages cid :count 4 :start-from (:order_id (last sm)))]
        (is (= 5 (count sm)))
        (is (= 5 (count rm)))
        (is (= (reverse msgs) (map :message rm)))
        (is (= '("4" "3" "2" "1") (map :message m2)))))))
