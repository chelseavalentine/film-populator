(ns populator.db.credits-test
  (:require [clojure.test :refer :all]
            [populator.db.credits :as db-credits]))

(deftest find-*-member
  (testing "Finding a *-member"
    (let [film-id   1234
          person-id 5678]
      (testing "using a valid film ID"
        (testing "and a non-number for the person ID"
          (is (nil? (db-credits/find-cast-member film-id nil)))
          (is (nil? (db-credits/find-crew-member film-id nil)))
          (is (nil? (db-credits/find-cast-member film-id {:id 1})))
          (is (nil? (db-credits/find-crew-member film-id {:id 1})))
          (is (nil? (db-credits/find-cast-member film-id "hey there")))
          (is (nil? (db-credits/find-crew-member film-id "hey there :)")))
          (is (nil? (db-credits/find-cast-member film-id "123a")))
          (is (nil? (db-credits/find-crew-member film-id "123b")))))
      (testing "using a valid person ID"
        (testing "and a non-number for the film ID"
          (is (nil? (db-credits/find-cast-member nil person-id)))
          (is (nil? (db-credits/find-crew-member nil person-id)))
          (is (nil? (db-credits/find-cast-member {:id 1} person-id)))
          (is (nil? (db-credits/find-crew-member {:id 1} person-id)))
          (is (nil? (db-credits/find-cast-member "yes" person-id)))
          (is (nil? (db-credits/find-crew-member "yes" person-id)))
          (is (nil? (db-credits/find-cast-member "123a" person-id)))
          (is (nil? (db-credits/find-crew-member "123b" person-id))))))))

(deftest create-*-member
  (testing "Creating a *-member"
    (let [film-id   1234
          person-id "5678"]
      (testing "using an invalid datatype for the data"
        (is (nil? (db-credits/create-cast-member nil)))
        (is (nil? (db-credits/create-crew-member nil)))
        (is (nil? (db-credits/create-cast-member 123)))
        (is (nil? (db-credits/create-crew-member 123)))
        )
      (testing "without containing valid required properties"
        (is (nil? (db-credits/create-cast-member {:film   nil
                                                  :person nil})))
        (is (nil? (db-credits/create-crew-member {:film   nil
                                                  :person nil})))
        (is (nil? (db-credits/create-cast-member {:film   "The Handmaiden"
                                                  :person "Kim Min-hee"})))
        (is (nil? (db-credits/create-crew-member {:film   "The Handmaiden"
                                                  :person "Kim Min-hee"}))))
      (testing "without containing any required properties"
        (is (nil? (db-credits/create-cast-member {})))
        (is (nil? (db-credits/create-crew-member {}))))
      (testing "without containing some required properties"
        (is (nil? (db-credits/create-cast-member {:film 123})))
        (is (nil? (db-credits/create-crew-member {:film 123})))
        (is (nil? (db-credits/create-cast-member {:person 123})))
        (is (nil? (db-credits/create-crew-member {:person 123})))))))
