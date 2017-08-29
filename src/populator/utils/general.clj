(ns populator.utils.general
  (:require [clojure.walk :as walk]))

(defn extract-value
  "Extracts a value from its input. Returns nil if input is a blank string or nil."
  ([input]
   (cond
     (nil? input) nil
     (string? input) (not-empty (clojure.string/trim input))
     :else input))
  ([data property]
   (when (and (map? data) (extract-value property))
     (let [formatted-map (walk/keywordize-keys data)
           input         (formatted-map (keyword property))]
       (extract-value input)))))

(defn extract-string
  "Extracts a string from its input."
  ([input]
   (when (string? input) (extract-value input)))
  ([data property]
   (extract-string (extract-value data property))))

(defn extract-coll
  "Extracts a collection from its input."
  ([input]
   (cond
     (coll? input) input
     (string? input) (when-let [input-string (extract-string input)]
                       (when-let [string-value (read-string input-string)]
                         (when (coll? string-value) string-value)))))
  ([data property]
   (extract-coll (extract-value data property))))

(defn extract-map
  "Extracts a map from its input."
  ([input]
   (cond
     (map? input) input
     (string? input) (when-let [input-string (extract-string input)]
                       (when-let [string-value (read-string input-string)]
                         (when (map? string-value) string-value)))))
  ([data property]
   (extract-map (extract-value data property))))

(defn extract-boolean
  "Extracts a boolean from its input."
  ([input]
   (cond
     (boolean? input) input
     (string? input) (when-let [input-string (extract-string input)]
                       (let [string-value (read-string input-string)]
                         (when (boolean? string-value) string-value)))))
  ([data property]
   (extract-boolean (extract-value data property))))

(defn extract-number
  "Extracts a number from its input."
  ([input]
   (cond
     (number? input) input
     (string? input) (when-let [input-string (extract-string input)]
                       (if (and (= (clojure.string/upper-case input-string)
                                  (clojure.string/lower-case input-string)))
                         (when-let [string-value (read-string input-string)]
                           (when (number? string-value) string-value))))))
  ([data property]
   (extract-number (extract-value data property))))

(defn extract-positive-number
  "Extracts a positive number from its input."
  ([input]
   (when-let [input-number (extract-number input)]
     (when (> input-number 0) input-number)))
  ([data property]
   (extract-positive-number (extract-value data property))))

(defn extract-property-items
  "Gets the specified attribute of a subarray value."
  [data property sub-property]
  (when (map? data)
    (let [items (extract-value data property)]
      (when (coll? items)
        (map #(extract-value % sub-property) items)))))

(defn safe-println [& more]
  (.write *out* (str (clojure.string/join " " more) "\n")))
