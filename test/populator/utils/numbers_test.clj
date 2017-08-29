(ns populator.utils.numbers_test
  (:require [clojure.test :refer :all]
            [populator.utils.numbers :as numbers]))

(deftest trim-leading-zeros
  (testing "Triming leading zeros"
    (testing "of a nil string"
      (is (nil? (numbers/trim-leading-zeros nil))))
    (testing "of a blank string"
      (is (empty? (numbers/trim-leading-zeros ""))))
    (testing "of a string containing no leading zeros"
      (is (= "10" (numbers/trim-leading-zeros "10"))))
    (testing "of a string containing one leading zero"
      (is (= "9" (numbers/trim-leading-zeros "09"))))
    (testing "of a string containing multiple leading zeros"
      (is (= "801" (numbers/trim-leading-zeros "00000801"))))))
