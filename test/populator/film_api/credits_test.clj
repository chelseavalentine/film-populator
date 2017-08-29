(ns populator.film-api.credits-test
  (:require [clojure.test :refer :all]
            [populator.film-api.credits :as credits]
            [populator.film-api.people :as people]))

(deftest create-cast-member
  (testing "Creating a cast/crew member"
    (let [film      {}
          role-data {:id 10}]
      (testing "using nil for the film parameter"
        (is (nil? (credits/create-cast-member nil role-data)))
        (is (nil? (credits/find-or-create-cast-member nil role-data)))
        (is (nil? (credits/create-crew-member nil role-data)))
        (is (nil? (credits/find-or-create-crew-member nil role-data))))
      (testing "using a non-map value for the film parameter"
        (is (nil? (credits/create-cast-member "hello" role-data)))
        (is (nil? (credits/find-or-create-cast-member "hello" role-data)))
        (is (nil? (credits/create-crew-member "hello" role-data)))
        (is (nil? (credits/find-or-create-crew-member "hello" role-data))))
      (testing "using nil for the role data"
        (is (nil? (credits/create-cast-member film nil)))
        (is (nil? (credits/find-or-create-cast-member film nil)))
        (is (nil? (credits/create-crew-member film nil)))
        (is (nil? (credits/find-or-create-crew-member film nil))))
      (testing "using a non-map value for the role data parameter"
        (is (nil? (credits/create-cast-member film "goodbye")))
        (is (nil? (credits/find-or-create-cast-member film "goodbye")))
        (is (nil? (credits/create-crew-member film "goodbye")))
        (is (nil? (credits/find-or-create-crew-member film "goodbye"))))
      (testing "using an invalid person ID value in the role data"
        (is (nil? (credits/create-cast-member film {:id nil})))
        (is (nil? (credits/find-or-create-cast-member film {:id nil})))
        (is (nil? (credits/create-cast-member film {:id "aaaa!"})))
        (is (nil? (credits/find-or-create-cast-member film {:id "aaaa!"})))
        (is (nil? (credits/create-crew-member film {:id nil})))
        (is (nil? (credits/find-or-create-crew-member film {:id nil})))
        (is (nil? (credits/create-crew-member film {:id "aaaa!"})))
        (is (nil? (credits/find-or-create-crew-member film {:id "aaaa!"})))))))

;; TODO: find-cast-member, find-crew-member, find-cast-to-create, find-crew-to-create
;; TODO: find-or-create-cast, find-or-create-crew

(deftest update-directors
  (testing "Updating a film's directors"
    (let [film-id   420
          directors [120 220]]
      (testing "using nil for the film ID"
        (is (nil? (credits/update-directors nil directors))))
      (testing "using a blank string for the film ID"
        (is (nil? (credits/update-directors "" directors))))
      (testing "using a film ID that isn't a number"
        (is (nil? (credits/update-directors "!2123" directors))))
      (testing "using a valid film ID,"
        (testing "and using nil for the directors"
          (is (nil? (credits/update-directors film-id nil))))
        (testing "and using an empty vector of directors"
          (is (nil? (credits/update-directors film-id []))))
        (testing "and using a vector of elements that aren't of number type"
          (is (nil? (credits/update-directors film-id ["!2" "@" "#"]))))
        (testing "and using a vector that only contains nil elements"
          (is (nil? (credits/update-directors film-id [nil nil]))))))))
