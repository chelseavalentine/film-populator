(ns populator.film-api.studio-films-test
  (:require [clojure.test :refer :all]
            [populator.film-api.studio-films :as studio-films]))

(deftest create-studio-film
  (testing "Creating a studio film record in the database"
    (testing "using an invalid film object and an invalid studio ID"
      (is (nil? (studio-films/create-studio-film nil nil))))
    (let [studio-id 29091
          film      {:tmdb_id 1234
                     :id      789}]
      (testing "using a valid studio ID"
        (testing "and nil for the film object"
          (is (nil? (studio-films/create-studio-film studio-id nil))))
        (testing "and a datatype other than a map for the film"
          (is (nil? (studio-films/create-studio-film studio-id 290098)))))
      (testing "using a valid film object"
        (testing "and nil for the studio ID"
          (is (nil? (studio-films/create-studio-film nil film))))
        (testing "and a blank string for the studio ID"
          (is (nil? (studio-films/create-studio-film "" film))))
        (testing "and a datatype other than number for the studio ID"
          (is (nil? (studio-films/create-studio-film {:id "12345a"} film))))))))

(deftest find-or-create-studio-film
  (testing "Finding or creating a studio film record in the database"
    (testing "using an invalid film object and an invalid studio ID"
      (is (nil? (studio-films/find-or-create-studio-film nil nil))))
    (let [studio-id 12345
          film      {:tmdb_id 1234
                     :id      789}]
      (testing "using a valid studio ID"
        (testing "and nil for the film object"
          (is (nil? (studio-films/find-or-create-studio-film studio-id nil))))
        (testing "and a datatype other than a map for the film"
          (is (nil? (studio-films/find-or-create-studio-film studio-id 290098)))))
      (testing "using a valid film object"
        (testing "and nil for the studio ID"
          (is (nil? (studio-films/find-or-create-studio-film nil film))))
        (testing "and a blank string for the studio ID"
          (is (nil? (studio-films/find-or-create-studio-film "" film))))
        (testing "and a datatype other than a number for the studio ID"
          (is (nil? (studio-films/find-or-create-studio-film {:id "12345a"} film))))))))
