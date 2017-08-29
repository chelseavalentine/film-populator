(ns populator.utils.time
  (:require [populator.utils.numbers :as numbers]
            [populator.utils.general :refer :all]
            [clj-time.core :as time]
            [clj-time.coerce :as time-coerce]))

(defn parse-date-string-parts
  [date-string]
  (when-let [delimiter (cond
                         (nil? date-string) nil
                         (.contains date-string "/") "/"
                         (.contains date-string "-") "-"
                         (.contains date-string ".") "\\.")]
    (vec (.split date-string delimiter))))

(defn parse-date-string
  [date-string]
  (when-let [date-parts (parse-date-string-parts (extract-string date-string))]
    (when (= (count date-parts) 3)
      (let [year         (extract-number (first date-parts))
            month-string (numbers/trim-leading-zeros (second date-parts))
            day-string   (numbers/trim-leading-zeros (last date-parts))
            month        (extract-number month-string)
            day          (extract-number day-string)]
        (when (and year month day
                (> 13 month 0) (> 32 day 0))
          {:year year :month month :day day})))))

(defn create-datetime
  [date-string]
  (when-let [date-string (extract-string date-string)]
    (let [date-representation (parse-date-string date-string)
          year                (extract-number date-representation :year)
          month               (extract-number date-representation :month)
          day                 (extract-number date-representation :day)]
      (when (and year month day)
        (time/date-time year month day)))))

(defn create-sql-date
  [date-string]
  (when-let [date-string (extract-string date-string)]
    (when-let [datetime (create-datetime date-string)]
      (time-coerce/to-sql-date datetime))))

(defn get-year
  [date]
  (time/year (time-coerce/from-sql-date date)))

(defn is-future?
  [date-string]
  (when-let [date-string (extract-string date-string)]
    (time/after? (time/now) (create-datetime date-string))))
