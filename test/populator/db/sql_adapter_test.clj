(ns populator.db.sql-adapter-test
  (:require [clojure.test :refer :all]
            [populator.db.sql-adapter :as sql-fns]))

(deftest convert-to-ordered-vector
  (testing "Converting data to an ordered vector"
    (testing "using a valid ordering argument"
      (let [ordering []]
        (testing "and an invalid data argument"
          (is (nil? (sql-fns/convert-to-ordered-vector ordering nil)))
          (is (nil? (sql-fns/convert-to-ordered-vector ordering 123))))
        (testing "and a valid data argument")))
    (testing "using a valid data argument"
      (let [data {:name "Chelsea"}]
        (testing "and an invalid ordering argument"
          (is (nil? (sql-fns/convert-to-ordered-vector nil data)))
          (is (nil? (sql-fns/convert-to-ordered-vector 123 data)))
          (is (nil? (sql-fns/convert-to-ordered-vector :name data))))
        (testing "and a valid ordering argument")))))
