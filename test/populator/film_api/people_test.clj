(ns populator.film-api.people-test
  (:require [clojure.test :refer :all]
            [populator.film-api.people :as people]))

(deftest create-person-representation
  (testing "Creating a representation of a person"
    (testing "using nil for the TMDB ID"
      (is (nil? (people/create-person-data nil))))
    (testing "using a TMDB ID value that isn't a number"
      (is (nil? (people/create-person-data "12345a"))))))

(deftest create-person
  (testing "Creating a person object in the database"
    (testing "using nil for the TMDB ID"
      (is (nil? (people/create-person nil))))
    (testing "using a TMDB ID value that isn't a number"
      (is (nil? (people/create-person "12345z"))))))

(deftest find-or-create-person
  (testing "Finding or creating a person object"
    (testing "using nil for the query"
      (is (nil? (people/find-or-create-person nil))))
    (testing "using a query with a type other than map"
      (is (nil? (people/find-or-create-person 12345))))
    (testing "using a TMDB ID in the query,"
      (testing "which is nil"
        (is (nil? (people/find-or-create-person {:tmdb_id nil})))))
    (testing "using an unsupported property in the query"
      (is (nil? (people/find-or-create-person {:id 12345}))))))
