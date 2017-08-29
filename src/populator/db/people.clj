(ns populator.db.people
  (:require [populator.db.sql-adapter :as sql-fns]
            [populator.db :as db :refer [dbconn]]
            [populator.utils.general :refer :all]))

(defn validate-person-data
  "Returns true or nil depending on whether the person data is valid
  for database insertion."
  [person-data]
  (when-let [person-data (extract-map person-data)]
    (let [criteria [#(extract-string % :name)
                    #(extract-number % :tmdb_id)]]
      (db/validate-data person-data criteria))))

(defn create-person
  "Creates a person record in the database."
  [data]
  (when-let [data (extract-map data)]
    (let [alt-names    (db/create-string-array (extract-coll data :alt_names))
          updated-data (assoc data :alt_names alt-names)]
      (when (validate-person-data data)
        (db/exec #(sql-fns/insert-person dbconn updated-data))))))

(defn create-people
  "Creates person records."
  [people-data]
  (when-let [people-data (not-empty (filter validate-person-data (extract-coll people-data)))]
    (let [formatted-people-data (map #(assoc % :alt_names
                                               (db/create-string-array (extract-value % :alt_names))) people-data)
          ordering              [:name :alt_names :bio :birthdate :deathdate :gender :hometown :image_path
                                 :website :imdb_id :tmdb_id]
          person-tuples         (doall (map (fn [formatted-person-data]
                                              (db/exec #(sql-fns/convert-to-ordered-vector ordering formatted-person-data))) formatted-people-data))]
      (when person-tuples
        (db/exec #(sql-fns/insert-people dbconn {:people person-tuples}))))))

(defn find-person
  "Finds a person record in the database."
  [query]
  (when (map? query)
    (cond
      (some? (extract-number query :tmdb_id)) (db/exec #(sql-fns/get-person-by-tmdb-id dbconn query)))))

(defn get-people-tmdb-ids
  "Gets all of the people's TMDB IDs."
  []
  (db/exec #(sql-fns/get-people-tmdb-ids dbconn)))
