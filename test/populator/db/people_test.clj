(ns populator.db.people-test
  (:require [clojure.test :refer :all]
            [populator.db.people :as db-people]))

(deftest create-person
  (testing "Creating a person record"
    (testing "using nil for the data"
      (is (nil? (db-people/create-person nil))))
    (testing "using a non-nil, non-map value for the data"
      (is (nil? (db-people/create-person 12345))))
    (testing "using a map for the data"
      (testing "without all of the required properties"
        (is (nil? (db-people/create-person {})))
        (is (nil? (db-people/create-person {:tmdb_id 123})))
        (is (nil? (db-people/create-person {:name "Chelsea"}))))
      (testing "with the required properties, but with the incorrect types"
        (is (nil? (db-people/create-person {:name nil
                                            :tmdb_id nil})))
        (is (nil? (db-people/create-person {:name 1235
                                            :tmdb_id "yes"})))
        (is (nil? (db-people/create-person {:name nil
                                            :tmdb_id 2134})))
        (is (nil? (db-people/create-person {:name "Chelsea"
                                            :tmdb_id "123ab"})))))))

(deftest find-person
  (testing "Findinga person record"
    (testing "using an invalid query"
      (is (nil? (db-people/find-person nil)))
      (is (nil? (db-people/find-person 12345))))
    (testing "using an empty query"
      (is (nil? (db-people/find-person {}))))
    (testing "using an unsupported query"
      (is (nil? (db-people/find-person {:name "Chelsea"}))))
    (testing "using a TMDB ID query,"
      (testing "which is invalid"
        (is (nil? (db-people/find-person {:tmdb_id "hello"})))))))
