(ns populator.db.films-test
  (:require [clojure.test :refer :all]
            [populator.db.films :as db-films]))

(deftest create-film
  (testing "Creating a film record"
    (testing "using nil for the film data"
      (is (nil? (db-films/create-film nil))))
    (testing "using film data with required properties missing"
      (is (nil? (db-films/create-film {})))
      (is (nil? (db-films/create-film {:title    "ya"
                                       :released true}))))
    (testing "using film data with required properties, but wrong types"
      (is (nil? (db-films/create-film {:title    nil
                                       :released nil
                                       :is_adult nil
                                       :tmdb_id  nil})))
      (is (nil? (db-films/create-film {:title    "Wow."
                                       :released true
                                       :is_adult false
                                       :tmdb_id  "Hello!"})))
      (is (nil? (db-films/create-film {:title    12
                                       :released "Released"
                                       :is_adult "No"
                                       :tmdb_id  "Hello!"}))))))

(deftest find-film
  (testing "Finding a film record"
    (testing "using nil for the query"
      (is (nil? (db-films/find-film nil))))
    (testing "using a non-map, non-nil type for the query"
      (is (nil? (db-films/find-film 12345))))
    (testing "using an unsupported query"
      (is (nil? (db-films/find-film {:title "Happy days"}))))
    (testing "with a query"
      (testing "that uses an ID,"
        (testing "which is invalid"
          (is (nil? (db-films/find-film {:id nil})))
          (is (nil? (db-films/find-film {:id "1234abs"})))))
      (testing "that uses a TMDB ID,"
        (testing "which is invalid"
          (is (nil? (db-films/find-film {:tmdb nil})))
          (is (nil? (db-films/find-film {:tmdb "1234a"}))))))))
