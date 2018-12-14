(ns flugger.db.entity-test
  (:require [clojure.test :refer :all]
            [flugger.db.entity :refer :all]
            [honeysql.helpers :as q]))

(deftest pg-format-test
  (testing "default pg properties injected correctly"
    (is (= "" (-> (q/))))))
