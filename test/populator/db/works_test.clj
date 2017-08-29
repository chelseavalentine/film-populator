(ns populator.db.works-test
  (:require [clojure.test :refer :all]
            [populator.db.works :as db-works]))

(deftest find-work
  (testing "Finding a work record"
    (testing "using nil for both IDs"
      (is (nil? (db-works/find-work nil nil))))
    (let [person-id 12
          film-id   34]
      (testing "using a valid person ID"
        (testing "and invalid values for the film ID"
          (is (nil? (db-works/find-work person-id nil)))
          (is (nil? (db-works/find-work person-id "123a")))
          (is (nil? (db-works/find-work person-id {:film 34})))))
      (testing "using a valid film ID"
        (testing "and invalid values for the person ID"
          (is (nil? (db-works/find-work nil film-id)))
          (is (nil? (db-works/find-work "123a" film-id)))
          (is (nil? (db-works/find-work {:person 123} film-id))))))))

(deftest create-work
  (testing "Creating a work record"
    (testing "using nil for the data"
      (is (nil? (db-works/create-work nil))))
    (testing "using a non-map for the data"
      (is (nil? (db-works/create-work "hey")))
      (is (nil? (db-works/create-work 1234))))
    (testing "without all of the required properties"
      (is (nil? (db-works/create-work {})))
      (is (nil? (db-works/create-work {:film 123})))
      (is (nil? (db-works/create-work {:film   123
                                       :title  "Good luck"
                                       :person 2}))))
    (testing "with the required properties, but with the incorrect types"
      (is (nil? (db-works/create-work {:film     "hi"
                                       :title    2
                                       :person   {}
                                       :released "you know it"
                                       :is_adult 0
                                       :tmdb_id  "byte"})))
      (is (nil? (db-works/create-work {:film     nil
                                       :title    nil
                                       :person   nil
                                       :released nil
                                       :is_adult nil
                                       :tmdb_id  nil}))))))

(deftest find-role
  (testing "Finding a role record"
    (testing "using invalid values for the role data"
      (is (nil? (db-works/find-role nil)))
      (is (nil? (db-works/find-role 123)))
      (is (nil? (db-works/find-role ["Producer" 1 2 "yes"]))))
    (testing "using a valid value for the role data"
      (testing "without all of the required properties"
        (is (nil? (db-works/find-role {})))
        (is (nil? (db-works/find-role {:work 10})))
        (is (nil? (db-works/find-role {:is_cast true}))))
      (testing "with the required properties, but with incorrect types"
        (is (nil? (db-works/find-role {:work    nil
                                       :is_cast nil})))
        (is (nil? (db-works/find-role {:work    123
                                       :is_cast nil})))
        (is (nil? (db-works/find-role {:work    nil
                                       :is_cast 123})))
        (is (nil? (db-works/find-role {:work    "yes"
                                       :is_cast "maybe"})))))))

(deftest create-role
  (testing "Creating a role record"
    (testing "using nil for the data"
      (is (nil? (db-works/create-role nil))))
    (testing "using a non-map for the data"
      (is (nil? (db-works/create-role "hey")))
      (is (nil? (db-works/create-role 1234))))
    (testing "without all of the required properties"
      (is (nil? (db-works/create-role {})))
      (is (nil? (db-works/create-role {:work 12})))
      (is (nil? (db-works/create-role {:is_cast true}))))
    (testing "with the required properties, but with the incorrect types"
      (is (nil? (db-works/create-role {:work    nil
                                       :is_cast nil})))
      (is (nil? (db-works/create-role {:work    123
                                       :is_cast nil})))
      (is (nil? (db-works/create-role {:work    nil
                                       :is_cast 123})))
      (is (nil? (db-works/create-role {:work    "hell yeah"
                                       :is_cast "maybe"}))))))
