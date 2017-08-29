(ns populator.utils.numbers
  (:require [populator.utils.general :refer [extract-string]]))

(defn- trim-leading-zero
  [string]
  (when-let [string (extract-string string)]
    (if (.equals (get string 0) \0)
      (trim-leading-zero (subs string 1))
      string)))

;; NOTE: numbers prefixed with 0 are treated as octal numbers
(defn trim-leading-zeros
  "Trims leading zeros in a string and returns the cleaned string."
  [string]
  (when-let [string (extract-string string)]
    (trim-leading-zero string)))
