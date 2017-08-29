(ns populator.studio-populator
  (:require [populator.film-api.tmdb :as tmdb]
            [populator.utils.concurrent :as conc]))

(defn- create-studio-promise
  [studio-tmdb-id]
  (let [p (promise)]
    (deliver p (tmdb/get-studio-credits studio-tmdb-id))))

(defn- get-films-for-studios
  [studio-tmdb-ids]
  (let [film-ids     (atom [])
        studio-count (count studio-tmdb-ids)
        curr-studio  (atom 0)]
    (println "Going to start getting related films for" studio-count "studios.")
    (let [promises (doall (map create-studio-promise studio-tmdb-ids))]
      (doseq [curr-promise promises]
        (swap! film-ids concat (deref curr-promise))
        (println "STUDIO" @curr-studio "/" studio-count)
        (swap! curr-studio inc)))
    (println "Found" (count @film-ids) "films related to" studio-count "studios!")
    (set @film-ids)))

(defn get-studio-films
  [studio-tmdb-ids]
  (conc/do-concurrently #(get-films-for-studios studio-tmdb-ids)))
