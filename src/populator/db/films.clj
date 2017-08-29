(ns populator.db.films
  (:require [populator.db.sql-adapter :as sql-fns]
            [populator.db :as db :refer [dbconn]]
            [populator.utils.general :refer :all])
  (:import [org.postgresql.jdbc PgArray]
           [java.sql Date]))

(defn validate-film-data
  ;; TODO: create a test for this
  "Returns true or nil depending on whether the film data
  is valid for database insertion."
  [film-data]
  (when-let [film-data (extract-map film-data)]
    (let [criteria [#(extract-string % :title)
                    #(boolean? (extract-boolean % :released))
                    #(boolean? (extract-boolean % :is_adult))
                    #(extract-number % :tmdb_id)]]
      (db/validate-data film-data criteria))))

(defn create-film
  "Creates a film record."
  [film-data]
  (when-let [film-data (extract-map film-data)]
    (when (validate-film-data film-data)
      (let [alt-titles          (db/create-string-array (extract-coll film-data :alt_titles))
            genres              (db/create-string-array (extract-coll film-data :genres))
            keywords            (db/create-string-array (extract-coll film-data :keywords))
            languages           (db/create-string-array (extract-coll film-data :languages))
            countries           (db/create-string-array (extract-coll film-data :countries))
            film-representation (assoc film-data :alt_titles alt-titles
                                                 :genres genres
                                                 :keywords keywords
                                                 :languages languages
                                                 :countries countries)]
        (db/exec #(sql-fns/insert-film dbconn film-representation))))))

(defn find-film
  "Finds a film record."
  [query]
  (when (map? query)
    (let [id         (extract-number query :id)
          tmdb-id    (extract-number query :tmdb_id)
          request-in (rand-int 10000)]
      (Thread/sleep request-in)
      (cond
        (some? id) (db/exec #(sql-fns/get-film-by-id dbconn query))
        (some? tmdb-id) (db/exec #(sql-fns/get-film-by-tmdb-id dbconn query))))))

(defn update-film-imageset
  "Updates a film's imageset."
  [changes]
  (when (map? changes)
    (let [criteria [#(extract-number % :id)
                    #(extract-number % :imageset)]]
      (when (db/validate-data changes criteria)
        (db/exec #(sql-fns/update-film-imageset dbconn changes))))))

(defn update-film-studios
  "Updates a film's studios."
  [changes]
  (when (map? changes)
    (let [criteria [#(extract-number % :id)
                    #(db/create-int-array (extract-value % :studios))]]
      (when (db/validate-data changes criteria)
        (db/exec #(sql-fns/update-film-studios dbconn changes))))))

(defn update-film-directors
  "Updates a film's directors."
  [changes]
  (when-let [changes (extract-map changes)]
    (let [film-id   (extract-number changes :id)
          directors (db/create-int-array (extract-coll changes :directors))]
      (when (and film-id directors)
        (db/exec #(sql-fns/update-film-directors dbconn {:id        film-id
                                                         :directors directors}))))))
