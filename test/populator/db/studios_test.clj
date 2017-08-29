(ns populator.db.studios-test
  (:require [clojure.test :refer :all]
            [populator.db.studios :as db-studios]))

(deftest create-studio
  (testing "Creating a studio"
    (testing "using nil for the data"
      (is (nil? (db-studios/create-studio nil))))
    (testing "using a non-nil, non-map value for the data"
      (is (nil? (db-studios/create-studio 12345))))
    (testing "using a map for the data"
      (testing "without all of the required properties"
        (is (nil? (db-studios/create-studio {})))
        (is (nil? (db-studios/create-studio {:tmdb_id 123})))
        (is (nil? (db-studios/create-studio {:name "Best studio ever"}))))
      (testing "with the required properties, but with the incorrect types"
        (is (nil? (db-studios/create-studio {:name    nil
                                             :tmdb_id nil})))
        (is (nil? (db-studios/create-studio {:name    1235
                                             :tmdb_id "yes"})))
        (is (nil? (db-studios/create-studio {:name    nil
                                             :tmdb_id 2134})))
        (is (nil? (db-studios/create-studio {:name    "Best Studio Ever"
                                             :tmdb_id "123ab"})))))))

(deftest find-studio
  (testing "Finding a studio record"
    (testing "using an invalid query"
      (is (nil? (db-studios/find-studio nil)))
      (is (nil? (db-studios/find-studio 12345))))
    (testing "using an empty query"
      (is (nil? (db-studios/find-studio {}))))
    (testing "using an unsupported query"
      (is (nil? (db-studios/find-studio {:name "Best studio ever"}))))
    (testing "using a TMDB ID query,"
      (testing "which is invalid"
        (is (nil? (db-studios/find-studio {:tmdb_id nil})))
        (is (nil? (db-studios/find-studio {:tmdb_id "hello"})))))
    (testing "using an ID query"
      (testing "which is invalid"
        (is (nil? (db-studios/find-studio {:id nil})))
        (is (nil? (db-studios/find-studio {:id "hi"})))))))

(deftest create-studio-film
  (testing "Creating a studio film record"
    (testing "using nil for the data"
      (is (nil? (db-studios/create-studio-film nil))))
    (testing "using a non-nil, non-map value for the data"
      (is (nil? (db-studios/create-studio-film 12345))))
    (testing "using a map for the data"
      (testing "without all of the required properties"
        (is (nil? (db-studios/create-studio-film {})))
        (is (nil? (db-studios/create-studio-film {:tmdb_id 123})))
        (is (nil? (db-studios/create-studio-film {:studio 1})))
        (is (nil? (db-studios/create-studio-film {:studio   1
                                                  :tmdb_id  123
                                                  :film     2
                                                  :title    "Good times"
                                                  :released true}))))
      (testing "with the required properties, but with the incorrect types"
        (is (nil? (db-studios/create-studio-film {:studio   nil
                                                  :film     nil
                                                  :title    nil
                                                  :released nil
                                                  :is_adult nil
                                                  :tmdb_id  nil})))
        (is (nil? (db-studios/create-studio-film {:studio   "a"
                                                  :film     "b"
                                                  :title    "c"
                                                  :released "d"
                                                  :is_adult "e"
                                                  :tmdb_id  "f"})))
        (is (nil? (db-studios/create-studio-film {:studio   1
                                                  :film     2
                                                  :title    "Great"
                                                  :released nil
                                                  :is_adult false
                                                  :tmdb_id  3})))))))

(deftest find-studio-film
  (testing "Finding a studio film record"
    (testing "using nil for both IDs"
      (is (nil? (db-studios/find-studio-film nil nil))))
    (let [studio-id 12
          film-id   34]
      (testing "using a valid studio ID"
        (testing "and invalid values for the film ID"
          (is (nil? (db-studios/find-studio-film studio-id nil)))
          (is (nil? (db-studios/find-studio-film studio-id "123a")))
          (is (nil? (db-studios/find-studio-film studio-id {:film 34})))))
      (testing "using a valid film ID"
        (testing "and invalid values for the studio ID"
          (is (nil? (db-studios/find-studio-film nil film-id)))
          (is (nil? (db-studios/find-studio-film "123a" film-id)))
          (is (nil? (db-studios/find-studio-film {:studio 123} film-id))))))))
