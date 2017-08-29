(ns populator.db.images-test
  (:require [clojure.test :refer :all]
            [populator.db.images :as db-images]))

(deftest find-image
  (testing "Finding an image record"
    (testing "using nil for the path"
      (is (nil? (db-images/find-image nil))))
    (testing "using an empty string for the path"
      (is (nil? (db-images/find-image ""))))))

(deftest create-image
  (testing "Creating an image record"
    (testing "using nil for the data"
      (is (nil? (db-images/create-image nil))))
    (testing "using a non-map for the data"
      (is (nil? (db-images/create-image "hey")))
      (is (nil? (db-images/create-image 1234))))
    (testing "without all of the required properties"
      (is (nil? (db-images/create-image {})))
      (is (nil? (db-images/create-image {:path "hey"})))
      (is (nil? (db-images/create-image {:path         "hey"
                                         :aspect_ratio 0.12
                                         :height       120}))))
    (testing "with the required properties, but with the incorrect types"
      (is (nil? (db-images/create-image {:path         213
                                         :aspect_ratio "big"
                                         :height       "420p"
                                         :width        {}})))
      (is (nil? (db-images/create-image {:path         nil
                                         :aspect_ratio nil
                                         :height       "420px"
                                         :width        nil}))))))

(deftest find-imageset
  (testing "Finding an imageset record"
    (testing "using an invalid query"
      (is (nil? (db-images/find-imageset nil)))
      (is (nil? (db-images/find-imageset 1234))))
    (testing "using an unsupported query"
      (is (nil? (db-images/find-imageset {:backdrops [123 345]})))
      (is (nil? (db-images/find-imageset {:id 123}))))))

(deftest create-imageset
  (testing "Creating an imageset record"
    (testing "using nil for the data"
      (is (nil? (db-images/create-imageset nil))))
    (testing "using a non-map, non-nil value for the data"
      (is (nil? (db-images/create-imageset 1234))))
    (testing "using an invalid TMDB ID"
      (is (nil? (db-images/create-imageset {:tmdb_id "great"})))
      (is (nil? (db-images/create-imageset {:tmdb_id nil}))))))
