(ns populator.film-api.works
  (:require [populator.db.works :as db-works]
            [populator.utils.general :refer :all]))

(defn create-work-data
  "Creates a map representing a work object."
  [film]
  (when-let [film (extract-map film)]
    {:film          (extract-number film :id)
     :title         (extract-string film :title)
     :roles         []
     :year          (extract-number film :year)
     :released      (extract-boolean film :released)
     :release_date  (extract-value film :release_date)
     :runtime       (extract-number film :runtime)
     :poster_path   (extract-string film :poster_path)
     :backdrop_path (extract-string film :backdrop_path)
     :genres        (extract-coll film :genres)
     :budget        (extract-number film :budget)
     :revenue       (extract-number film :revenue)
     :is_adult      (extract-boolean film :is_adult)
     :tmdb_id       (extract-number film :tmdb_id)
     :imdb_id       (extract-string film :imdb_id)}))

(defn create-work
  "Creates a work object in the database."
  [person-id film]
  (when (map? film)
    (let [data (assoc (create-work-data film) :person person-id)]
      (db-works/create-work data))))

(defn create-role-data
  "Creates a map representing a role object."
  [data]
  (when-let [data (extract-map data)]
    {:is_cast    (extract-boolean data :is_cast)
     :character  (extract-string data :character)
     :department (extract-string data :department)
     :job        (extract-string data :job)
     :work       (extract-number data :work)}))

(defn find-or-create-work
  "Finds or creates a work object."
  [person-tmdb-id film]
  (let [person-tmdb-id (extract-number person-tmdb-id)
        film           (extract-map film)]
    (when (and person-tmdb-id film)
      (when-let [film-tmdb-id (extract-number film :tmdb_id)]
        (if-let [work (db-works/find-work person-tmdb-id film-tmdb-id)]
          work
          (create-work person-tmdb-id film))))))

(defn find-or-create-role
  "Finds a role, or creates a role and returns the created role."
  ([work-role-data]
   (when-let [work-role-data (extract-map work-role-data)]
     (if-let [found-role (db-works/find-role work-role-data)]
       found-role
       (db-works/create-role (create-role-data work-role-data)))))
  ([work role-data]
   (let [work      (extract-map work)
         role-data (extract-map role-data)]
     (when (and work role-data)
       (let [work-id   (extract-number work :id)
             work-role (assoc role-data :work work-id)]
         (find-or-create-role work-role)))))
  ([person-tmdb-id film role-data]
   (when-let [role-data (extract-map role-data)]
     (when-let [work (find-or-create-work person-tmdb-id film)]
       (find-or-create-role work role-data)))))
