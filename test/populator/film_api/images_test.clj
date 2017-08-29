(ns populator.film-api.images-test
  (:require [clojure.test :refer :all]
            [populator.film-api.images :as images]))

;; TODO: find-images, find-images-to-create, find-or-create-images

(deftest create-image
  (testing "Creating an image in the database"
    (testing "using nil for the image data"
      (is (nil? (images/create-image nil))))
    (testing "using a non-map for the image data parameter"
      ;; TODO: Support this case w/ extract map
      ;(is (nil? (images/create-image "/cool_panda.jpg")))
      )))

(deftest find-or-create-image
  (testing "Finding or creating an image object"
    (testing "using nil for the image data"
      (is (nil? (images/find-or-create-image nil))))
    (testing "using a non-map for the image data parameter"
      (is (nil? (images/find-or-create-image 290098))))))

(deftest create-images
  (testing "Creating multiple images"
    (testing "using nil for the images' data"
      (is (= [] (images/find-or-create-images nil))))
    (testing "without using a sequence or a vector for the images' data"
      (is (= [] (images/find-or-create-images 12345))))
    (testing "using an empty vector for the images' data"
      (is (= [] (images/find-or-create-images []))))
    (testing "using a vector full of nil values for the images' data"
      (is (= [] (images/find-or-create-images [nil nil nil nil]))))))

(deftest create-imageset
  (testing "Creating an imageset"
    (testing "using nil for the imageset data"
      (is (nil? (images/create-imageset nil))))
    (testing "without using a map for the imageset data"
      (is (nil? (images/create-imageset "an imageset says a trillion words"))))))

(deftest find-or-create-imageset
  (testing "Finding or creating an imageset"
    (testing "using nil for the imageset data"
      (is (nil? (images/find-or-create-imageset nil))))
    (testing "without using a map for the imageset data"
      (is (nil? (images/find-or-create-imageset "an imageset says a trillion words"))))
    (testing "using imageset data containing an invalid TMDB ID"
      (is (nil? (images/find-or-create-imageset {:tmdb_id "hello there :)"})))
      (is (nil? (images/find-or-create-imageset {:tmdb_id nil}))))))
