(ns populator.film-api.credits
  (:require [populator.film-api.people :as people]
            [populator.utils.general :refer :all]
            [populator.db.credits :as db-credits]
            [populator.db.films :as db-films]
            [populator.db :as db]))

(defn create-cast-member-data
  [film role-data]
  (let [film      (extract-map film)
        role-data (assoc (extract-map role-data) :is_cast true)]
    (when (and film role-data)
      (when-let [person (people/find-or-create-person {:tmdb_id (extract-number role-data :id)})]
        (let [person-tmdb-id (extract-number person :tmdb_id)]
          {:film      (extract-number film :id)
           :name      (extract-string person :name)
           :person    person-tmdb-id
           :character (extract-string role-data :character)})))))

(defn create-cast-member
  "Creates and returns a cast member."
  [film role-data]
  (let [film      (extract-map film)
        role-data (extract-map role-data)]
    (when-let [cast-member-data (create-cast-member-data film role-data)]
      (println "CREATING CAST MEMBER!:" (extract-string cast-member-data :name) "(" (extract-number film :tmdb_id) ")")
      (db-credits/create-cast-member cast-member-data))))

(defn find-cast-member
  "Finds a cast member in the database."
  [film role-data]
  (let [film-id        (extract-number film :id)
        role-data      (extract-map role-data)
        person-tmdb-id (extract-number role-data :id)]
    (when (and film-id person-tmdb-id)
      (db-credits/find-cast-member film-id person-tmdb-id role-data))))

(defn find-or-create-cast-member
  "Finds or creates a cast member in the database."
  [film role-data]
  (let [film      (extract-map film)
        role-data (extract-map role-data)]
    (if-let [cast-member (find-cast-member film role-data)]
      cast-member
      (create-cast-member film role-data))))

(defn find-cast-to-create
  "Finds cast members that need creating."
  [film roles-data]
  (let [film       (extract-map film)
        roles-data (not-empty (filter some? (extract-coll roles-data)))]
    (when (and film roles-data)
      (not-empty (filter some? (filter #(nil? (find-cast-member film %)) roles-data))))))

(defn find-or-create-cast
  "Finds or creates each of the cast members"
  [film roles-data]
  (let [film       (extract-map film)
        roles-data (not-empty (filter some? (extract-coll roles-data)))]
    (when (and film roles-data)
      (when-let [cast-to-create (find-cast-to-create film roles-data)]
        (when-let [cast-creation-data (doall (map #(create-cast-member-data film %) cast-to-create))]
          (println "CREATING CAST w/" (count cast-creation-data) "PEOPLE.")
          (db-credits/create-cast cast-creation-data))))))

(defn create-crew-member-data
  [film role-data]
  (let [film      (extract-map film)
        role-data (assoc (extract-map role-data) :is_cast false)]
    (when (and film role-data)
      (when-let [person (people/find-or-create-person {:tmdb_id (extract-number role-data :id)})]
        (let [person-tmdb-id (extract-number person :tmdb_id)]
          {:film       (extract-number film :id)
           :name       (extract-string person :name)
           :person     person-tmdb-id
           :department (extract-string role-data :department)
           :job        (extract-string role-data :job)})))))

(defn create-crew-member
  "Creates and returns a crew member."
  [film role-data]
  (let [film      (extract-map film)
        role-data (extract-map role-data)]
    (when-let [crew-member-data (create-crew-member-data film role-data)]
      (println "CREATING CREW MEMBER!:" (extract-string crew-member-data :name) "(" (extract-number film :tmdb_id) ")")
      (db-credits/create-crew-member crew-member-data))))

(defn find-crew-member
  "Finds a crew member in the database."
  [film role-data]
  (let [film-id        (extract-number film :id)
        role-data      (extract-map role-data)
        person-tmdb-id (extract-number role-data :id)]
    (when (and film-id person-tmdb-id)
      (db-credits/find-crew-member film-id person-tmdb-id role-data))))

(defn find-or-create-crew-member
  "Finds or creates a crew member in the database."
  [film role-data]
  (let [film      (extract-map film)
        role-data (extract-map role-data)]
    (if-let [crew-member (find-crew-member film role-data)]
      crew-member
      (create-crew-member film role-data))))

(defn find-or-create-crew
  [film roles-data]
  (let [film       (extract-map film)
        roles-data (not-empty (filter some? (extract-coll roles-data)))]
    (when (and film roles-data)
      (doall (map #(find-or-create-crew-member film %) roles-data)))))

(defn update-directors
  "Updates a film's directors, and returns the updated film object
  if it changed."
  [film-id director-ids]
  (when-let [film-id (extract-number film-id)]
    (when-let [director-ids (extract-coll director-ids)]
      (db-films/update-film-directors {:id        film-id
                                       :directors director-ids}))))
