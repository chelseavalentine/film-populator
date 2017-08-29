(ns populator.studio-populator
  (:require [populator.utils.files :as files]
            [populator.film-api.tmdb :as tmdb]
            [populator.film-api.film :as film-fns]
            [populator.db.studios :as studios]
            [populator.utils.concurrent :as conc]
            [clojure.core.async :as async]))

(def studio-tmdb-ids (atom []))

(defn create-studio-promise
  [i j chunk-size]
  (let [studio-index (+ (* i chunk-size) j)
        studio-tmdb-id (nth @studio-tmdb-ids studio-index)
        p (promise)]
    (deliver p (tmdb/get-studio-credits studio-tmdb-id))))

(defn get-films-for-studios
  []
  (when-let [studio-response (not-empty (vec (set (studios/get-studios-tmdb-ids))))]
    (println "Got the response.")
    (reset! studio-tmdb-ids (doall (map #(% :tmdb_id) studio-response)))
    (let [film-ids     (atom [])
          studio-count (count @studio-tmdb-ids)
          curr-studio  (atom 0)]
      (println "Mapped the studios' TMDB IDs.")
      (let [chunk-size 69
            num-chunks (Math/floor (/ studio-count chunk-size))]
        (println "Figured out the number of chunks to use.")
        (dotimes [i num-chunks]
          (println "\n\n------------------------- CHUNK" i "/" num-chunks "\n\n")
          (let [j-indices (range chunk-size)
                promises  (doall (map #(create-studio-promise i % chunk-size) j-indices))]
            (doseq [currPromise promises]
              (swap! film-ids concat (deref currPromise))
              (println "STUDIO " @curr-studio "/" studio-count)
              (swap! curr-studio inc)))
          (files/write (str "studios-film-ids-" i ".txt") (vec (set @film-ids)))
          (reset! film-ids [])))
      (println "Nice. Went through all of the studios, now let's write their film TMBD ids to a file.")
      (println "Done~~!")
      (shutdown-agents))))

(defn populate-studios
  []
  (conc/do-concurrently get-films-for-studios))
