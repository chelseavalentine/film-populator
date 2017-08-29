(ns populator.film-api.studios-test
  (:require [clojure.test :refer :all]
            [populator.film-api.studios :as studios]))

;; TODO: find-studios, find-studios-to-create, find-or-create-studios
(deftest create-studio-representation
  (testing "Creating a map representation of a studio record"
    (testing "using nil for the studio's TMDB ID"
      (is (nil? (studios/create-studio-data nil))))
    (testing "using a datatype that isn't nil or a number for the studio's TMDB ID"
      ;; TODO: uncomment once fix extract-map
      ;(is (nil? (studios/create-studio-data "1234aloha")))
      (is (nil? (studios/create-studio-data {:tmdb_id 290098}))))))

(deftest create-studio
  (testing "Creating a studio record in the database"
    (testing "using nil for the studio's TMDB ID"
      (is (nil? (studios/create-studio nil))))
    (testing "using a datatype that isn't nil or a number for the studio's TMDB ID"
      ;(is (nil? (studios/create-studio "1234aloha")))
      (is (nil? (studios/create-studio {:tmdb_id 290098}))))))

(deftest find-or-create-studio
  (testing "Finding or creating a studio in the database"
    (testing "using nil for the studio's TMDB ID"
      (is (nil? (studios/find-or-create-studio nil))))
    (testing "using a datatype that isn't nil or a number for the studio's TMDB ID"
      ;(is (nil? (studios/find-or-create-studio "1234aloha")))
      (is (nil? (studios/find-or-create-studio {:tmdb_id "290098 nooo"}))))))

(deftest update-film-studios
  (testing "Updating a film's studios"
    (testing "using an invalid film ID and studio ID"
      (is (nil? (studios/update-film-studios nil nil))))
    (let [film-id    523
          studio-ids [123 23 56]]
      (testing "using a valid film ID"
        (testing "and nil for the studio IDs"
          (is (nil? (studios/update-film-studios film-id nil))))
        (testing "and an empty vector for the studio IDs"
          (is (nil? (studios/update-film-studios film-id []))))
        (testing "and a sequence full of nils for the studio IDs"
          (is (nil? (studios/update-film-studios film-id (seq [nil nil nil]))))))
      (testing "using valid studio-ids"
        (testing "and nil for the film ID"
          (is (nil? (studios/update-film-studios nil studio-ids))))
        (testing "and a blank string for the film ID"
          (is (nil? (studios/update-film-studios "" studio-ids))))
        (testing "and a datatype other than nil or a number for the film ID"
          (is (nil? (studios/update-film-studios {:id "elo234h4el"} studio-ids))))))))
