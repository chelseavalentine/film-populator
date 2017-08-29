(ns populator.utils.files
  (:require [clojure.java.io :as io]
            [slingshot.slingshot :as slingshot]
            [populator.utils.general :refer :all]))

(defn exists
  [file-path]
  (slingshot/try+
    (.exists (io/as-file file-path))
    (catch Object _
      false)))

(defn doesnt-exist
  [file-path]
  (not (exists file-path)))

(defn write
  [file-path data]
  (when (not-empty file-path)
    (spit file-path (with-out-str (pr data)))))

(defn read-file
  [file-path]
  (when (not-empty file-path)
    (read-string (slurp file-path))))

(defn chunk-vector-into-files
  [path-base file-path chunk-size]
  (when-let [file-path (extract-string file-path)]
    (let [raw-data  (extract-coll (read-file file-path))
          path-base (extract-string path-base)]
      (when (and raw-data path-base)
        (let [formatted-data (vec raw-data)
              num-items      (count formatted-data)]
          (when-not (zero? num-items)
            (let [num-chunks (Math/ceil (/ num-items chunk-size))]
              (dotimes [i num-chunks]
                (let [start (* i chunk-size)
                      end   (min (* (inc i) chunk-size) num-items)]
                  (write (str path-base i ".txt") (subvec formatted-data start end))))
              num-chunks)))))))

(defn partition-vector-by-num-files
  ;; TODO: Refactor
  [path-base file-path num-files]
  (def data (read-file file-path))
  (when-not (or (nil? data) (empty? path-base))
    (def items (when (coll? data) (vec (set data))))
    (def num-items (count items))
    (when-not (zero? num-items)
      (def chunk-size (Math/ceil (/ num-items num-files)))
      (dotimes [i num-files]
        (def start (* i chunk-size))
        (def end (min (* (inc i) chunk-size) num-items))
        (if (< start num-items)
          (write (str path-base i ".txt") (subvec items start end))
          (write (str path-base i ".txt") []))))))

(defn combine-files
  ([path-base end]
   (combine-files path-base 0 end))
  ([path-base start end]
   (when-let [path-base (not-empty path-base)]
     (let [items (atom [])]
       (doseq
        [num (range start end)]
         (let [filename (str path-base num ".txt")]
           (swap! items concat (vec (read-file filename)))))
       @items))))
