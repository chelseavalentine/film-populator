(ns populator.db.studios
  (:require [populator.db.sql-adapter :as sql-fns]
            [populator.db :as db :refer [dbconn]]
            [populator.utils.general :refer :all]))

(defn validate-studio-data
  ;; TODO: Create tests for this
  "Returns true or nil depending on whether the studio data is valid
  for database insertion."
  [studio-data]
  (when-let [studio-data (extract-map studio-data)]
    (let [criteria [#(extract-string % :name)
                    #(extract-number % :tmdb_id)]]
      (db/validate-data studio-data criteria))))

(defn create-studio
  "Creates a studio record in the database."
  [studio-data]
  (when-let [studio-data (extract-map studio-data)]
    (when (validate-studio-data studio-data)
      (db/exec #(sql-fns/insert-studio dbconn studio-data)))))

(defn find-studio
  "Finds a studio record in the database."
  [query]
  (when-let [query (extract-map query)]
    (cond
      (some? (extract-number query :id)) (db/exec #(sql-fns/get-studio-by-id dbconn query))
      (some? (extract-number query :tmdb_id)) (db/exec #(sql-fns/get-studio-by-tmdb-id dbconn query)))))

(defn create-studio-film
  "Creates a studio film record in the database."
  [studio-film-data]
  (when-let [studio-film-data (extract-map studio-film-data)]
    (let [criteria [#(extract-number % :studio)
                    #(extract-number % :film)
                    #(extract-string % :title)
                    #(boolean? (extract-boolean % :released))
                    #(boolean? (extract-boolean % :is_adult))
                    #(extract-number % :tmdb_id)]]
      (when (db/validate-data studio-film-data criteria)
        (db/exec #(sql-fns/insert-studio-film dbconn studio-film-data))))))

(defn find-studio-film
  "Finds a studio film record in the database."
  [studio-id film-id]
  (let [studio-id (extract-number studio-id)
        film-id   (extract-number film-id)]
    (when (and studio-id film-id)
      (db/exec #(sql-fns/get-studio-film dbconn {:studio studio-id
                                                 :film   film-id})))))

(defn get-studios-tmdb-ids
  "Gets all of the studios's TMDB IDs."
  []
  (db/exec #(sql-fns/get-studio-tmdb-ids dbconn)))
