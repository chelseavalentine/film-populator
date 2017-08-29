(ns populator.person-populator
  (:require [populator.utils.files :as files]
            [populator.film-api.tmdb :as tmdb]
            [populator.film-api.film :as film-fns]
            [populator.db.people :as people]
            [populator.utils.concurrent :as conc]))

(def person-tmdb-ids (atom []))

(defn create-person-promise
  [i j chunk-size]
  (let [person-index   (+ (* i chunk-size) j)
        person-tmdb-id (nth @person-tmdb-ids person-index)
        p              (promise)]
    (deliver p (tmdb/get-person-credits-ids person-tmdb-id))))

(defn get-films-for-people
  []
  (when-let [people-response (not-empty (vec (set (people/get-people-tmdb-ids))))]
    (println "Got the response.")
    (reset! person-tmdb-ids (doall (map #(% :tmdb_id) people-response)))
    (let [film-ids     (atom [])
          person-count (count @person-tmdb-ids)
          curr-person  (atom 0)]
      (println "Mapped the person TMDB IDs.")
      (let [chunk-size 421
            num-chunks (Math/ceil (/ person-count chunk-size))]
        (println "Figured out the number of chunks to use.")
        (doseq [i (range num-chunks)]
          (println "\n\n------------------------- CHUNK" i "/" num-chunks "\n\n")
          (let [j-indices (range chunk-size)
                promises  (doall (map #(create-person-promise i % chunk-size) j-indices))]
            (doseq [currPromise promises]
              (swap! film-ids concat (deref currPromise))
              (println "PERSON " @curr-person "/" person-count)
              (swap! curr-person inc)))
          (files/write (str "person-film-ids-" i ".txt") (vec (set @film-ids)))
          (reset! film-ids [])))
      (println "Done~~!")
      (shutdown-agents))))

(defn populate-people
  []
  (conc/do-concurrently get-films-for-people))
