(ns populator.film-api.works-test
  (:require [clojure.test :refer :all]
            [populator.film-api.works :as works]))

(deftest create-work-representation
  (testing "Creating a work representation object"
    (testing "using nil for the film object"
      (is (nil? (works/create-work-data nil))))
    (testing "using a film object that isn't a map"
      (is (nil? (works/create-work-data ["title" 30, "hello" false])))
      (is (nil? (works/create-work-data "")))
      (is (nil? (works/create-work-data 123456789))))
    (testing "using a film object that is a map"
      (let [film-object {:film     1
                         :id       10
                         :title    "My special title"
                         :tmdb_id  123456789
                         :is_adult false}
            work        {:film          10
                         :title         "My special title"
                         :roles         []
                         :year          nil
                         :released      nil
                         :release_date  nil
                         :runtime       nil
                         :poster_path   nil
                         :backdrop_path nil
                         :genres        nil
                         :budget        nil
                         :revenue       nil
                         :is_adult      false
                         :tmdb_id       123456789
                         :imdb_id       nil}]
        (is (= work (works/create-work-data film-object)))))))

(deftest create-work
  (testing "Creating a work object in the database"
    (testing "using nil for the film object"
      (is (nil? (works/create-work nil))))
    (testing "using a non-map for the film object"
      (is (nil? (works/create-work 12345))))))

(deftest create-role-representation
  (testing "Creating a role representation object"
    (let [is-cast             false
          role-data           {:credit_id    "52fe4250c3a36847f8014a11"
                               :department   "Production"
                               :job          "Producer"
                               :id           1254
                               :name         "Art Linson"
                               :profile_path nil}
          role-representation {:is_cast    false
                               :character  nil
                               :department "Production"
                               :job        "Producer"}]
      (testing "using nil for the role data"
        (is (nil? (works/create-role-data nil is-cast))))
      (testing "using a role data value that isn't a map or nil"
        (is (nil? (works/create-role-data "hi there" is-cast))))
      (testing "using nil for is_cast"
        (is (nil? (works/create-role-data role-data nil))))
      (testing "using a value that isn't a boolean for is_cast"
        (is (nil? (works/create-role-data role-data "truez"))))
      (testing "using valid role data and a boolean for is_cast"
        (is (= role-representation (works/create-role-data role-data is-cast)))))))

(deftest find-or-create-work
  (testing "Finding or creating a work object"
    (let [person-id 52
          film      {:tmdb_id 290098}]
      (testing "using nil for the person ID"
        (is (nil? (works/find-or-create-work nil film))))
      (testing "using a value type other than string for the person ID"
        (is (nil? (works/find-or-create-work "a12345" film)))
        (is (nil? (works/find-or-create-work {:id person-id} film))))
      (testing "using a blank string for the person ID"
        (is (nil? (works/find-or-create-work "" film))))
      (testing "using nil for the film object"
        (is (nil? (works/find-or-create-work person-id nil))))
      (testing "using a non-map for the film object"
        (is (nil? (works/find-or-create-work person-id 290098))))
      (testing "using nil for the film's TMDB ID"
        (is (nil? (works/find-or-create-work person-id {:tmdb_id nil}))))
      (testing "using a value that isn't a number for the film's TMDB ID"
        (is (nil? (works/find-or-create-work person-id "heh")))
        (is (nil? (works/find-or-create-work person-id {:tmdb_id "heh"})))))))

(deftest add-role-to-work
  (testing "Adding a role to a person"
    (let [person-id 52
          film      {:tmdb_id 290098}
          role      {:is_cast    false
                     :character  nil
                     :department "Production"
                     :job        "Producer"}]
      (testing "using nil for the person ID"
        (is (nil? (works/find-or-create-role nil film role))))
      (testing "using a blank string for the person ID"
        (is (nil? (works/find-or-create-role "" film role))))
      (testing "using a value type other than a number for the person ID"
        (is (nil? (works/find-or-create-role "12345heh " film role)))
        (is (nil? (works/find-or-create-role {:id 12345} film role))))
      (testing "using nil for the film object"
        (is (nil? (works/find-or-create-role person-id nil role))))
      (testing "using a non-map for the film object"
        (is (nil? (works/find-or-create-role person-id "hello" role))))
      (testing "using nil for the film's TMDB id"
        (is (nil? (works/find-or-create-role person-id {:tmdb_id nil} role))))
      (testing "using a value that isn't a number for the film's TMDB ID"
        (is (nil? (works/find-or-create-role person-id "hehe" role)))
        (is (nil? (works/find-or-create-role person-id {:tmdb_id "hehe"} role))))
      (with-redefs-fn {#'works/find-or-create-work (fn [person-id film] {:id "abc123"})}
        (fn[]
          (testing "using nil for the role"
            (is (nil? (works/find-or-create-role person-id film nil))))
          (testing "using a value type other than a map for the role"
            (is (nil? (works/find-or-create-role person-id film "Producer")))))))))
