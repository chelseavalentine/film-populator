(ns populator.db.works
  (:require [populator.db.sql-adapter :as sql-fns]
            [populator.db :as db :refer [dbconn]]
            [populator.utils.general :refer :all]))

(defn validate-work-data
  "Returns true or nil depending on whether the work data is valid
  for database insertion."
  [work-data]
  (when-let [work-data (extract-map work-data)]
    (let [criteria [#(extract-number % :film)
                    #(extract-string % :title)
                    #(extract-number % :person)
                    #(boolean? (extract-boolean % :released))
                    #(boolean? (extract-boolean % :is_adult))
                    #(extract-number % :tmdb_id)]]
      (db/validate-data work-data criteria))))

(defn find-work
  "Finding a work record in the database."
  [person-tmdb-id film-id]
  (let [person-tmdb-id (extract-number person-tmdb-id)
        film-id        (extract-number film-id)]
    (when (and person-tmdb-id film-id)
      (db/exec #(sql-fns/get-work dbconn {:person person-tmdb-id
                                          :film   film-id})))))

(defn create-work
  "Creating a work record in the database."
  [work-data]
  (when-let [work-data (extract-map work-data)]
    (when (validate-work-data work-data)
      (db/exec #(sql-fns/insert-work dbconn work-data)))))

(defn validate-role-data
  "Returns true or nil depending on whether the role data is valid
  for database insertion."
  [role-data]
  (when-let [role-data (extract-map role-data)]
    (let [criteria [#(extract-number % :work)
                    #(boolean? (extract-boolean % :is_cast))]]
      (db/validate-data role-data criteria))))

(defn find-role
  "Finding a role record in the database."
  [role-data]
  (when-let [role-data (extract-map role-data)]
    (let [criteria [#(extract-number % :work)
                    #(boolean? (extract-boolean % :is_cast))]]
      (when (db/validate-data role-data criteria)
        (db/exec #(sql-fns/get-role dbconn role-data))))))

(defn create-role
  "Creating a role record in the database."
  [role-data]
  (when-let [role-data (extract-map role-data)]
    (when (validate-role-data role-data)
      (db/exec #(sql-fns/insert-role dbconn role-data)))))
