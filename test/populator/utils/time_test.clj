(ns populator.utils.time-test
  (:require [clj-time.core :as clj-time]
            [clj-time.coerce :as time-coerce]
            [populator.utils.time :as time]
            [clojure.test :refer :all]))

(deftest parse-date-string-parts
  (def expected-parts ["1997" "12" "11"])
  (testing "Parsing date string"
    (testing "using nil for the string"
      (is (nil? (time/parse-date-string-parts nil))))
    (testing "without delimiters"
      (is (nil? (time/parse-date-string-parts "19971211"))))
    (testing "with '/' delimiters"
      (is (= expected-parts (time/parse-date-string-parts "1997/12/11"))))
    (testing "with '-' delimiters"
      (is (= expected-parts (time/parse-date-string-parts "1997-12-11"))))
    (testing "with '.' delimiters"
      (is (= expected-parts (time/parse-date-string-parts "1997.12.11"))))))

(deftest parse-date-string
  (testing "Parsing a date string"
    (testing "with a nil date string"
      (is (nil? (time/parse-date-string nil))))
    (testing "with an empty date string"
      (is (nil? (time/parse-date-string ""))))
    (testing "with an invalid day"
      (is (nil? (time/parse-date-string "1997-12-34"))))
    (testing "with an invalid month"
      (is (nil? (time/parse-date-string "1997-16-10"))))
    (testing "containing numbers > 7 with leading zeros"
      (def expected-date {:year 1997 :month 8 :day 9})
      (is (= expected-date (time/parse-date-string "1997-08-09"))))
    (testing "only containing a month and day"
      (is (nil? (time/parse-date-string "12-03"))))
    (testing "only containing a year and month"
      (is (nil? (time/parse-date-string "1997-02"))))))

(deftest create-sql-date
  (testing "Creating a date object"
    (testing "with empty date string"
      (is (nil? (time/create-sql-date ""))))
    (testing "with nil date string"
      (is (nil? (time/create-sql-date nil))))
    (testing "with proper date"
      (def datetime (clj-time/date-time 1997 02 14))
      (def expected-date (time-coerce/to-sql-date datetime))
      (is (= expected-date (time/create-sql-date "1997-02-14"))))))
