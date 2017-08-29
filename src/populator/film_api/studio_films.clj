(ns populator.film-api.studio-films
  (:require [populator.db.studios :as db-studios]
            [populator.utils.general :refer :all]
            [populator.film-api.tmdb :as tmdb]))

(defn create-studio-film
  "Creates a studio's film."
  [studio-id film]
  (let [studio-id (extract-number studio-id)
        film      (extract-map film)]
    (when (and studio-id film)
      (let [studio-film-data {:film          (extract-number film :id)
                              :title         (extract-string film :title)
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
                              :imdb_id       (extract-string film :imdb_id)
                              :studio        studio-id}]
        (db-studios/create-studio-film studio-film-data)))))

(defn find-or-create-studio-film
  "Finds or creates a studio's film."
  [studio-id film]
  (when-let [studio-id (extract-number studio-id)]
    (when-let [film (extract-map film)]
      (let [film-id     (extract-number film :id)
            studio-film (db-studios/find-studio-film studio-id film-id)]
        (cond
          (some? studio-film) studio-film
          (some? film-id) (create-studio-film studio-id film))))))
