(ns populator.person-populator
  (:require [populator.film-api.tmdb :as tmdb]
            [populator.utils.concurrent :as conc]))

(defn- create-person-promise
  [person-tmdb-id]
  (let [p (promise)]
    (deliver p (tmdb/get-person-credits-ids person-tmdb-id))))

(defn- get-films-for-people
  [person-tmdb-ids]
  (let [film-ids     (atom [])
        person-count (count @person-tmdb-ids)
        curr-person  (atom 0)]
    (println "Going to start getting all related films for" person-count "people.")
    (let [promises (doall (map create-person-promise person-tmdb-ids))]
      (doseq [curr-promise promise]
        (swap! film-ids concat (deref curr-promise))
        (println "PERSON" @curr-person "/" person-count)
        (swap! curr-person inc)))
    (println "Found" (count @film-ids) "films related to" person-count "people!")
    (set @film-ids)))

(defn get-people-films
  [person-tmdb-ids]
  (conc/do-concurrently #(get-films-for-people person-tmdb-ids)))
