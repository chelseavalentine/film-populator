(ns populator.db-test
  (:require [clojure.test :refer :all]
            [populator.db :as db]))

(deftest exec
  (testing "Database function execution"
    (testing "not using a function for the function parameter"
      (is (nil? (db/exec nil)))
      (is (nil? (db/exec ":)")))
      (is (nil? (db/exec 420))))
    (testing "using a valid function"
      (is (= "valid!" (db/exec (fn [] "valid!")))))))

(deftest create-string-array
  (testing "Creating a PostgresSQL string array"
    (testing "using nil"
      (is (nil? (db/create-string-array nil))))
    (testing "using an empty vector"
      (is (nil? (db/create-string-array []))))
    (testing "using a vector of non-strings"
      (is (nil? (db/create-string-array [123 4 5 6789])))
      (is (nil? (db/create-string-array [nil nil nil nil nil]))))
    (testing "using a vector of empty strings"
      (is (nil? (db/create-string-array [" " " " "" "\n" "\t  \n"]))))))

(deftest create-int-array
  (testing "Creating a PostgresSQL int array"
    (testing "using nil"
      (is (nil? (db/create-int-array nil))))
    (testing "using an empty vector"
      (is (nil? (db/create-int-array []))))
    (testing "using a vector of non-ints"
      (is (nil? (db/create-int-array ["hi" "world"])))
      (is (nil? (db/create-int-array [nil nil nil]))))))

(deftest validate-data
  (testing "Validating data"
    (let [data {:bye 420}
          met-criteria [#(contains? % :bye)]
          unmet-criteria [#(contains? % :bye)
                          #(contains? % :hi)]]
      (testing "using valid data"
        (testing "and nil for the criteria"
          (is (nil? (db/validate-data data nil))))
        (testing "and an empty vector for the criteria"
          (is (true? (db/validate-data data [])))))
      (testing "using valid criteria"
        (testing "and nil for the data"
          (is (nil? (db/validate-data nil met-criteria))))
        (testing "and a non-map type for the data"
          (is (nil? (db/validate-data 420 met-criteria))))
        (testing "that succeeds, and a valid map"
          (is (true? (db/validate-data data met-criteria))))
        (testing "that fails, and a valid map"
          (is (false? (db/validate-data data unmet-criteria))))))))
