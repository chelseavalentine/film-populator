(ns populator.film-api.tmdb-test
  (:require [clojure.test :refer :all]
            [populator.film-api.tmdb :as tmdb]))

(deftest call-api
  (testing "Calling an api"
    (testing "not using a function for the function parameter"
      (is (nil? (tmdb/call-api nil)))
      (is (nil? (tmdb/call-api "hello")))
      (is (nil? (tmdb/call-api 420))))
    (testing "using a valid function"
      (is (= "hey" (tmdb/call-api (fn [] "hey")))))
    (testing "for a non-existent resource")
    (testing "and getting rate-limited")))

(deftest make-http-request
  (testing "Making a HTTP request"
    (testing "using nil for the URL"
      (is (nil? (tmdb/make-http-request nil))))
    (testing "using an empty string for the URL"
      (is (nil? (tmdb/make-http-request ""))))))

(deftest format-additional-params
  (testing "Formatting the additional parameters"
    (testing "using nil for the params"
      (is (= "" (tmdb/format-additional-params nil))))
    (testing "using an empty string for the params"
      (is (= "" (tmdb/format-additional-params ""))))
    (testing "using a non-empty string that doesn't start with `&`"
      (is (= "&type=milk" (tmdb/format-additional-params "type=milk"))))
    (testing "using a non-empty string that already starts with `&`"
      (is (= "&type=orange" (tmdb/format-additional-params "&type=orange"))))))

(deftest request-tmdb
  (testing "Requesting TMDB"
    (testing "using nil for the url"
      (is (nil? (tmdb/request-tmdb nil))))
    (testing "using a blank string for the url"
      (is (nil? (tmdb/request-tmdb ""))))
    (testing "using a valid path"
      (let [path "mis-personas/420"]
        (testing "and nil for the query params"
          (is (nil? (tmdb/request-tmdb path nil))))
        (testing "and a non-string, non-nil value for the query params"
          (is (nil? (tmdb/request-tmdb path {:language "en-US"}))))))))

(deftest discover-year-films
  (testing "Discovering a year's films one page at a time"
    (testing "using nil for the year"
      (is (nil? (tmdb/discover-year-films nil))))
    (testing "using a non-number, non-nil value, for the year"
      (is (nil? (tmdb/discover-year-films {:year 2016}))))
    (testing "using a negative number for the year"
      (is (nil? (tmdb/discover-year-films -1))))
    (testing "using zero for the year"
      (is (nil? (tmdb/discover-year-films 0))))
    (testing "using a valid year"
      (let [year 2016]
        (testing "and nil for the page"
          (is (nil? (tmdb/discover-year-films year nil))))
        (testing "and a non-number, non-nil value for the page"
          (is (nil? (tmdb/discover-year-films year {:page 1}))))
        (testing "and a negative number for the page"
          (is (nil? (tmdb/discover-year-films year -2017))))
        (testing "and zero for the page"
          (is (nil? (tmdb/discover-year-films year 0))))))))

(deftest get-year-films
  (testing "Getting all of the film IDs for films created in a year"
    (testing "using nil for the year"
      (is (nil? (tmdb/get-year-films nil))))
    (testing "using a non-number, non-nil value for the year"
      (is (nil? (tmdb/get-year-films {:year 2017}))))
    (testing "using a negative number for the year"
      (is (nil? (tmdb/get-year-films -1997))))
    (testing "using zero for the year"
      (is (nil? (tmdb/get-year-films 0))))))

(deftest get-film-info
  (testing "Getting a film's information"
    (testing "using nil for the TMDB ID"
      (is (nil? (tmdb/get-film-info nil))))
    (testing "using a value that isn't a number, or nil, for the TMDB ID"
      (is (nil? (tmdb/get-film-info {:tmdb_id "yo"}))))))

(deftest get-studio-details
  (testing "Getting a studio's details"
    (testing "using nil for the TMDB id"
      (is (nil? (tmdb/get-studio-details nil))))
    (testing "using a value that isn't a number, or nil, for the TMDB ID"
      (is (nil? (tmdb/get-studio-details {:tmdb_id "yo"}))))))

(deftest get-person-details
  (testing "Getting a person's details"
    (testing "using nil"
      (is (nil? (tmdb/get-person-details nil))))
    (testing "using a value that isn't a number, or nil"
      (is (nil? (tmdb/get-person-details {:tmdb_id "yo"}))))))
