(ns populator.film-api.film-test
  (:require [clojure.test :refer :all]
            [populator.film-api.film :as film]))

(deftest test-film-setup-fns
  (testing "Film functions requiring two maps"
    (testing "using an invalid film response and invalid data"
      (is (nil? (film/setup-film-titles nil nil)))
      (is (nil? (film/setup-film-release-dates nil nil)))
      (is (nil? (film/setup-film-keywords nil nil)))
      (is (nil? (film/create-film-images nil nil)))
      (is (nil? (film/create-film-studios nil nil)))
      (is (nil? (film/create-film-credits nil nil)))
      (is (nil? (film/setup-film-titles 123 123)))
      (is (nil? (film/setup-film-release-dates 123 123)))
      (is (nil? (film/setup-film-keywords 123 123)))
      (is (nil? (film/create-film-images 123 123)))
      (is (nil? (film/create-film-studios 123 123)))
      (is (nil? (film/create-film-credits 123 123))))
    (is (nil? (film/create-film-images 123 123)))
    (testing "using a valid film response"
      (let [film-response {}]
        (testing "and invalid film data"
          (is (nil? (film/setup-film-titles film-response nil)))
          (is (nil? (film/setup-film-release-dates film-response nil)))
          (is (nil? (film/setup-film-keywords film-response nil)))
          (is (nil? (film/create-film-images film-response nil)))
          (is (nil? (film/create-film-studios film-response nil)))
          (is (nil? (film/create-film-credits film-response nil)))
          (is (nil? (film/setup-film-titles film-response 290098)))
          (is (nil? (film/setup-film-release-dates film-response 290098)))
          (is (nil? (film/setup-film-keywords film-response 290098)))
          (is (nil? (film/create-film-images film-response 290098)))
          (is (nil? (film/create-film-studios film-response 290098)))
          (is (nil? (film/create-film-credits film-response 290098))))))
    (testing "using valid film data"
      (let [film-data {}]
        (testing "and an invalid film response"
          (is (nil? (film/setup-film-titles nil film-data)))
          (is (nil? (film/setup-film-release-dates nil film-data)))
          (is (nil? (film/setup-film-keywords nil film-data)))
          (is (nil? (film/create-film-images nil film-data)))
          (is (nil? (film/create-film-studios nil film-data)))
          (is (nil? (film/create-film-credits nil film-data)))
          (is (nil? (film/setup-film-titles 123 film-data)))
          (is (nil? (film/setup-film-release-dates 123 film-data)))
          (is (nil? (film/setup-film-keywords 123 film-data)))
          (is (nil? (film/create-film-images 123 film-data)))
          (is (nil? (film/create-film-studios 123 film-data)))
          (is (nil? (film/create-film-credits 123 film-data))))))))

(deftest create-film-data
  (testing "Creating a film data"
    (testing "using an invalid film response"
      (is (nil? (film/create-film-data nil)))
      (is (nil? (film/create-film-data "ahhh")))
      (is (nil? (film/create-film-data 123))))))

(deftest create-film-images
  (testing "Creating film images"
    (testing "using a valid response"
      (let [film-response {:tmdb_id 123
                           :images  []}]))
    (testing "using a valid film"
      (let [film {}]
        (testing "using a response without a valid TMDB ID or images property"
          (is (= film (film/create-film-images {} film)))
          (is (= film (film/create-film-images {:tmdb_id "yes"} film)))
          (is (= film (film/create-film-images {:images "yes"} film)))
          (is (= film (film/create-film-images {:tmdb_id "12a"
                                                :images  "yes"} film))))
        (testing "using a response with a valid TMDB ID"
          (testing "and a no images property"
            (is (= film (film/create-film-images {:tmdb_id 123} film))))
          (testing "and an empty images property"
            (is (= film (film/create-film-images {:tmdb_id 123
                                                  :images  []} film)))))
        (testing "using a response with a non-empty images property")))))

(deftest create-film-studios
  (testing "Creating film studios"
    (testing "using a valid response"
      (let [film-response {:tmdb_id 123
                           :studios []}]))
    (testing "using a valid film"
      (let [film {}]
        (testing "using a response without a valid TMDB ID or studios property"
          (is (= film (film/create-film-studios {} film)))
          (is (= film (film/create-film-studios {:tmdb_id "yes"} film)))
          (is (= film (film/create-film-studios {:studios "yes"} film)))
          (is (= film (film/create-film-studios {:tmdb_id "12a"
                                                 :studios "yes"} film))))
        (testing "using a response with a valid TMDB ID"
          (testing "and a no studios property"
            (is (= film (film/create-film-studios {:tmdb_id 123} film))))
          (testing "and an empty studios property"
            (is (= film (film/create-film-studios {:tmdb_id 123
                                                   :studios []} film)))))
        (testing "using a response with a non-empty studios property")))))

(deftest create-film-credits
  (testing "Creating film credits"
    (testing "using a valid response"
      (let [film-response {:tmdb_id 123
                           :credits {}}]))
    (testing "using a valid film"
      (let [film {}]
        (testing "using a response without a valid TMDB ID or credits property"
          (is (= film (film/create-film-credits {} film)))
          (is (= film (film/create-film-credits {:tmdb_id "yes"} film)))
          (is (= film (film/create-film-credits {:credits "yes"} film)))
          (is (= film (film/create-film-credits {:tmdb_id "12a"
                                                 :credits "yes"} film))))
        (testing "using a response with a valid TMDB ID"
          (testing "and an invalid credits property"
            (is (= film (film/create-film-credits {:tmdb_id 123} film)))))
        (testing "using a response with a non-empty credits property")))))

(deftest create-film
  (testing "Creating a film"
    (testing "Using an invalid TMDB ID datatype"
      (is (nil? (film/create-film nil)))
      (is (nil? (film/create-film "Hello")))
      (is (nil? (film/create-film {:tmdb_id nil}))))))

(deftest find-or-create-film
  (testing "Finding or creating a film"
    (testing "using an invalid query datatype"
      (is (nil? (film/find-or-create-film nil)))
      (is (nil? (film/find-or-create-film "hey")))
      (is (nil? (film/find-or-create-film 1))))
    (testing "using an empty query"
      (is (nil? (film/find-or-create-film {}))))
    (testing "using an unsupported query"
      (is (nil? (film/find-or-create-film {:title "Hello"}))))
    (testing "using a support query with the wrong datatypes"
      (is (nil? (film/find-or-create-film {:tmdb_id nil})))
      (is (nil? (film/find-or-create-film {:tmdb_id "Okay"}))))))
