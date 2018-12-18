(ns flugger.model.channel_test
  (:require [clojure.test :refer :all]
            [manifold.deferred :as d]
            [flugger.model.service :as service]
            [flugger.model.user :as user]
            [flugger.model.channel :refer :all]
            [flugger.db.entity-test :refer [with-test-db]]))

(use-fixtures :once with-test-db)

xb(defn- send-message-bunch [ml cid sid uid]
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
        _ @(add-member cid uid)]

    (testing "Models creation"
      (is (= "test-service" (:name srv)))
      (is (= "test-user" (:name u)))
      (is (= "test-channel" (:title ch))))

    (testing "Send message"
      (is (= "test-message" (:message @(send-message cid sid uid "test-message")))))

    (testing "Send message to channel without membership"
      (is (thrown? Exception @(send-message cid sid "not-exists" "foo"))))

    (testing "Retriev messages"
      (let [msgs '("1" "2" "3" "4" "5")
            sm (doall (for [m msgs] @(send-message cid sid uid m)))
            rm @(get-messages cid :count 5)
            m2 @(get-messages cid :count 4 :start-from (:order_id (last sm)))]
        (is (= 5 (count sm)))
        (is (= 5 (count rm)))
        (is (= (reverse msgs) (map :message rm)))
        (is (= '("4" "3" "2" "1") (map :message m2)))))

    (testing "Members"
      (let [user-names ["u1" "u2" "u3"]
            ch1 @(create {:title "ch1" :service_id sid})
            c1id (:id ch1)
            ch2 @(create {:title "ch2" :service_id sid})
            c2id (:id ch2)
            users (doall (for [n user-names] @(user/create {:name n
                                                            :service_id sid})))
            _ (doseq [u users] @(add-member c1id (:id u)))
            _ @(add-user c2id (:id (first users)))
            ch1m @(get-members c1id)
            ch2m @(get-members c2id)]
        (is (= 3 (count users)))
        (is (= 3 (count ch1m)))
        (is (= 1 (count ch2m)))))))

(deftest users
  (let [srv @(service/create "other service")
        sid (:id srv)
        cnames ["ch1" "ch2" "ch3"]
        u @(user/create {:name "user1" :service_id sid})
        uid (:id u)
        chn (doall (for [n cnames] @(create {:title n :service_id sid})))
        _ (doseq [c chn] @(add-member (:id c) uid))]
    (testing "Get subscribed channels"
      (let [sch @(user/get-subscribed-channels uid)]
        (is (= 3 (count sch)))
        (is (= (map :id chn) (map :id sch)))))))
