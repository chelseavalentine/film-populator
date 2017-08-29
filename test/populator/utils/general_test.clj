(ns populator.utils.general-test
  (:require [clojure.test :refer :all]
            [populator.utils.general :as general]))

(deftest extract-value
  (testing "Extracting a value"
    (testing "from a nil hashmap"
      (is (nil? (general/extract-value nil "something"))))
    (testing "from a hashmap property that's nil"
      (is (nil? (general/extract-value "" nil))))
    (testing "from a hashmap property"
      (let [data {"string_val" "hello",
                  "array_val"  ["hi"],
                  "nil_val"    nil,
                  "bool_val"   true,
                  :num_val     29}]
        (testing "containing nil"
          (is (nil? (general/extract-value data "nil_val"))))
        (testing "containing an integer"
          (is (= 29 (general/extract-value data "num_val"))))
        (testing "containing a string"
          (is (= "hello" (general/extract-value data "string_val"))))
        (testing "containing a boolean"
          (is (= true (general/extract-value data "bool_val"))))
        (testing "containing an array"
          (is (= ["hi"] (general/extract-value data "array_val"))))
        (testing "that doesn't exist"
          (is (nil? (general/extract-value data "doesnt-exist"))))))
    (testing "from an empty string"
      (is (nil? (general/extract-value ""))))
    (testing "from a string only containing whitespace"
      (is (nil? (general/extract-value "    \n")))
      (is (nil? (general/extract-value " \n \t "))))
    (testing "from a string containing whitespace on the ends"
      (is (= "chocolate" (general/extract-value " \nchocolate \n")))
      (is (= "chocolate" (general/extract-value " chocolate ")))
      (is (= "chocolate" (general/extract-value "\n chocolate "))))))

(deftest extract-string
  (testing "Extracting a number"
    (testing "using nil"
      (is (nil? (general/extract-string nil))))
    (testing "using an empty string"
      (is (nil? (general/extract-string ""))))
    (testing "using a value type other than nil or string"
      (is (nil? (general/extract-string {:hello "world"}))))
    (testing "using a string"
      (let [test-string "hello"]
        (is (= test-string (general/extract-string test-string)))))))

(deftest extract-coll
  (testing "Extracting a collection"
    (testing "using nil"
      (is (nil? (general/extract-coll nil))))
    (testing "using an empty string"
      (is (nil? (general/extract-coll ""))))
    (let [collection [123 123 123 3]]
      (testing "using a string representation of a collection"
        (is (= collection (general/extract-coll "[123 123 123 3]")))
        (is (= collection (general/extract-coll "[123, 123, 123, 3]"))))
      (testing "using a collection"
        (is (= collection (general/extract-coll collection)))))))

(deftest extract-map
  ;; TODO: Protect against case "{:hello 123"
  ;; TODO: Protect against case "/cool_panda.jpg"
  ;; TODO: Protect against case "1234aloha"
  (testing "Extracting a map"
    (testing "using nil"
      (is (nil? (general/extract-map nil))))
    (testing "using an empty string"
      (is (nil? (general/extract-map ""))))
    (let [map {:hi "there"}]
      (testing "using a string representation of a map"
        (is (= map (general/extract-map "{:hi \"there\"}"))))
      (testing "using a collection"
        (is (= map (general/extract-map map)))))))

(deftest extract-boolean
  (testing "Extracting a boolean value"
    (testing "using nil"
      (is (nil? (general/extract-boolean nil))))
    (testing "using a value type other than nil or a boolean"
      (is (nil? (general/extract-boolean "bool"))))
    (testing "using a boolean"
      (is (true? (general/extract-boolean true)))
      (is (true? (general/extract-boolean "true")))
      (is (false? (general/extract-boolean false)))
      (is (false? (general/extract-boolean "false"))))))

(deftest extract-number
  (testing "Extracting a number"
    (testing "using nil"
      (is (nil? (general/extract-number nil))))
    (testing "using a number"
      (is (= 11 (general/extract-number 11))))
    (testing "using a string representation of a number"
      (is (= 11 (general/extract-number "11"))))
    (testing "using a string that starts with a number, and includes non-digits"
      (is (nil? (general/extract-number "1234abc"))))
    (testing "using a string that ends in a number, and includes non-digits"
      (is (nil? (general/extract-number "abc1234"))))
    (testing "using a value type other than nil, string, or number"
      (is (nil? (general/extract-number {:number 11}))))))

(deftest extract-positive-number
  (testing "Extracting a positive number"
    (testing "using nil"
      (is (nil? (general/extract-positive-number nil))))
    (testing "using zero"
      (is (nil? (general/extract-positive-number 0))))
    (testing "using a negative number"
      (is (nil? (general/extract-positive-number -10))))
    (testing "using a positive number"
      (is (= 3 (general/extract-positive-number 3))))
    (testing "using a string representation of a positive number"
      (is (= 9 (general/extract-positive-number "9"))))))

(deftest extract-property-items
  (testing "Extracting property items"
    (testing "with a nil hashmap"
      (is (nil? (general/extract-property-items nil "property" "sub-property"))))
    (let [data         {"string_val" "hello"
                        "array_val"  ["hi"],
                        "map_vector" [{"name" "joe"}
                                      {"name" "chelsea"}
                                      {"name" "carlette"}
                                      {"name" "justin"}],
                        "nil_val"    nil}
          nil-vector   [nil nil nil nil]
          names-vector ["joe" "chelsea" "carlette" "justin"]]
      (testing "with a property that is"
        (testing "nil"
          (is (nil? (general/extract-property-items data nil "name"))))
        (testing "blank"
          (is (nil? (general/extract-property-items data "" ":name"))))
        (testing "not a vector"
          (is (nil? (general/extract-property-items data "string_val" "length"))))
        (testing "a vector of something other than maps"
          (is (= [nil] (general/extract-property-items data "array_val" "length"))))
        (testing "a vector of maps and uses a sub-property that"
          (testing "is nil"
            (is (= nil-vector (vec (general/extract-property-items data "map_vector" nil)))))
          (testing "is blank"
            (is (= nil-vector (general/extract-property-items data "map_vector" ""))))
          (testing "doesn't exist"
            (is (= nil-vector (general/extract-property-items data "map_vector" "age"))))
          (testing "exists"
            (is (= names-vector (vec (general/extract-property-items data "map_vector" "name"))))))))))
