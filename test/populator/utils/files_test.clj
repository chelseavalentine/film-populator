(ns populator.utils.files-test
  (:require [clojure.test :refer :all]
            [populator.utils.files :as files]
            [clojure.java.io :as io])
  (:import [java.io FileNotFoundException]))

(def tmp-dir (System/getenv "TMPDIR"))

(deftest exists
  (testing "File exists"
    (testing "with a nil file path"
      (is (false? (files/exists nil))))
    (testing "with an empty file path"
      (is (false? (files/exists ""))))
    (testing "with a file that exists"
      (def file-path (str tmp-dir "my-test-exists-file.txt"))
      (spit file-path "hah")
      (is (true? (files/exists file-path)))
      (io/delete-file file-path))
    (testing "with a file that doesn't exist"
      (def file-path (str tmp-dir "lolololol.txt"))
      (when (files/exists file-path)
        (io/delete-file file-path))
      (is (false? (files/exists file-path))))))

(deftest doesnt-exist
  (testing "File doesn't exist"
    (testing "with a nil file path"
      (is (true? (files/doesnt-exist nil))))
    (testing "with an empty file path"
      (is (true? (files/doesnt-exist ""))))
    (testing "with a file that exists"
      (def file-path (str tmp-dir "my-test-exists-file.txt"))
      (spit file-path "hah")
      (is (false? (files/doesnt-exist file-path)))
      (io/delete-file file-path))
    (testing "with a file that doesn't exist"
      (def file-path (str tmp-dir "lolololol.txt"))
      (when (files/exists file-path)
        (io/delete-file file-path))
      (is (true? (files/doesnt-exist file-path))))))

(defn test-with-contents
  [data f]
  (def file-path (str tmp-dir "testing-with-contents-file.txt"))
  (files/write file-path data)
  (f file-path)
  (io/delete-file file-path))

(defn test-with-blank-file
  [f]
  (test-with-contents "" f))

(defn test-wrote-correctly
  [written-data]
  (test-with-contents
    written-data
    (fn [file-path] (is (= written-data (files/read-file file-path))))))

(defn clean-up-files
  ([path-base end]
   (clean-up-files path-base 0 end))
  ([path-base start end]
   (doseq
    [num (range start end)]
     (def file-path (str path-base num ".txt"))
     (io/delete-file file-path))))

(deftest write-and-reading-from-file
  (testing "Writing to a file"
    (testing "with a nil file path"
      (is (nil? (files/write nil "hi"))))
    (testing "with a blank file path"
      (is (nil? (files/write "" "hii")))))
  (testing "Reading from a file"
    (testing "with a nil for the file path"
      (is (nil? (files/read-file nil))))
    (testing "with a blank file path"
      (is (nil? (files/read-file ""))))
    (testing "that is empty"
      (test-wrote-correctly ""))
    (testing "containing"
      (testing "nil"
        (test-wrote-correctly nil))
      (testing "a boolean"
        (test-wrote-correctly true))
      (testing "a vector"
        (test-wrote-correctly ["1" "2" "3" "4" 10]))
      (testing "a map"
        (def written-data {"name"     "Chelsea",
                           "age"      20,
                           "birthday" (populator.utils.time/create-sql-date "1997/02/14")})
        (test-wrote-correctly written-data))
      (testing "a string"
        (test-wrote-correctly "hey there!\n\n\nlololol")))))

(deftest chunk-vector-into-files
  (def path-base (str tmp-dir "testing-chunking-a-vector-into-files-pieces-"))
  (def chunk-size 10)
  (testing "Chunking a vector into files"
    (testing "using a nil file path"
      (is (nil? (files/chunk-vector-into-files path-base nil chunk-size))))
    (testing "using a blank file path"
      (is (nil? (files/chunk-vector-into-files path-base "" chunk-size))))
    (testing "using a path to an existing file, "
      (def file-path (str tmp-dir "testing-chunking-a-vector-into-files-file.txt"))
      (testing "and a nil file base string"
        (test-with-blank-file (fn [path] (is (nil? (files/chunk-vector-into-files nil path chunk-size))))))
      (testing "and a blank file base string"
        (test-with-blank-file (fn [path] (is (nil? (files/chunk-vector-into-files "" path chunk-size))))))
      (testing "a valid file base string,"
        (testing "and a negative chunk size"
          (test-with-blank-file (fn [path] (is (nil? (files/chunk-vector-into-files path-base path -1))))))
        (testing "and zero for the chunk size"
          (test-with-blank-file (fn [path] (is (nil? (files/chunk-vector-into-files path-base path 0))))))
        (testing "and a nonzero chunk size"
          (testing "with an blank file"
            (test-with-blank-file (fn [path] (is (nil? (files/chunk-vector-into-files path-base path chunk-size))))))
          (testing "with a file whose contents aren't in a vector"
            (test-with-contents
              69
              (fn [path] (is (nil? (files/chunk-vector-into-files path-base file-path chunk-size))))))
          (testing "with a file whose contents are in a vector"
            (def data [1 2 3 4 5 1 2 3 10 10 12 30 19 69 420 666 1111])
            (def data-size (count data))
            (def num-chunks (Math/ceil (/ data-size chunk-size)))
            (test-with-contents
              data
              (fn [path]
                (is (= num-chunks (files/chunk-vector-into-files path-base path chunk-size)))))
            (clean-up-files path-base num-chunks)))))))

(deftest partition-vector-by-num-files
  (def path-base (str tmp-dir "testing-partitioning-vector-by-number-of-files-"))
  (def num-files 10)
  (testing "Partitioning a vector by number of files"
    (testing "using a nil file path"
      (is (nil? (files/partition-vector-by-num-files path-base nil num-files))))
    (testing "using a blank file path"
      (is (nil? (files/partition-vector-by-num-files path-base "" num-files))))
    (testing "using a path to an existing file,"
      (def file-path (str tmp-dir "testing-partitioning-a-vector-into-files-file.txt"))
      (testing "and a nil file base string"
        (test-with-blank-file (fn [path] (nil? (files/partition-vector-by-num-files nil path num-files)))))
      (testing "and a blank file base string"
        (test-with-blank-file (fn [path] (nil? (files/partition-vector-by-num-files "" path num-files)))))
      (testing "a valid file base string,"
        (testing "and a negative number of files"
          (test-with-blank-file (fn [path] (nil? (files/partition-vector-by-num-files path-base path -1)))))
        (testing "and zero for the number of files"
          (test-with-blank-file (fn [path] (nil? (files/partition-vector-by-num-files path-base path 0)))))
        (testing "and a nonzero number of files"
          (testing "with a blank file"
            (test-with-blank-file (fn [path] (nil? (files/partition-vector-by-num-files path-base path num-files)))))
          (testing "with a non-blank file"
            (testing "whose contents aren't in a vector"
              (test-with-contents
                420
                (fn [path] (is (nil? (files/partition-vector-by-num-files path-base path num-files))))))
            (testing "whose contents are an empty vector"
              (test-with-contents
                []
                (fn [path] (is (nil? (files/partition-vector-by-num-files path-base path num-files))))))
            (testing "whose contents are a non-empty vector"
              (def data [1 2 3 4 5 1 2 3 10 10 12 30 19 69 420 666 1111])
              (test-with-contents
                data
                (fn
                  [path]
                  (files/partition-vector-by-num-files path-base path num-files)
                  (doseq [num (range num-files)]
                    (def created-partition-path (str path-base num ".txt"))
                    (is (true? (files/exists created-partition-path))))))
              (clean-up-files path-base num-files))))))))

(defn write-to-files
  [path-base start end data]
  (doseq
   [num (range start end)]
    (def file-path (str path-base num ".txt"))
    (files/write file-path data)))

(deftest combine-files
  (testing "Combining the contents of chunked files into one file"
    (testing "for a nil file base string"
      (is (nil? (files/combine-files nil 10))))
    (testing "for a blank file base string"
      (is (nil? (files/combine-files "" 10))))
    (testing "for a non-empty file base string"
      (def path-base (str tmp-dir "testing-combining-files-"))
      (def start 5)
      (def end 15)
      (testing "using an empty range"
        (is (empty? (files/combine-files path-base 0 0))))
      (testing "using 1+ out-of-bounds file numbers"
        (write-to-files path-base start end "hi")
        (is (thrown? FileNotFoundException (files/combine-files path-base start (inc end))))
        (clean-up-files path-base start end))
      (testing "using a valid file range"
        (def data [1 2 3 4 5])
        (write-to-files path-base start end data)
        (def combined-data (flatten (replicate (- end start) data)))
        (is (= combined-data (files/combine-files path-base start end)))
        (clean-up-files path-base start end))
      (testing "with empty files"
        (def empty-string "")
        (write-to-files path-base start end empty-string)
        (is (empty? (files/combine-files path-base start end)))
        (clean-up-files path-base start end)))))
