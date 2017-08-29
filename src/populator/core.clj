(ns populator.core
  (:gen-class)
  (:require [populator.utils.files :as files]
            [populator.film-api.tmdb :as tmdb]
            [populator.film-api.film :as film-fns]
            [populator.utils.concurrent :as conc]))

(def all-film-ids (atom {}))

(defn generate-film-ids-file
  []
  (let [film-ids (tmdb/get-year-films 1800 2060)]
    (files/write "film-ids.txt" (vec (set film-ids)))))

(defn- read-in-films-file
  []
  (reset! all-film-ids (set (files/read-file "film-ids.txt")))
  (println "We have" (count @all-film-ids) "films to create."))

(defn create-film
  [tmdb-id]
  (let [related-films-ids (film-fns/create-film tmdb-id)
        films-excluding-this (swap! all-film-ids disj tmdb-id)]
    (files/write "film-ids.txt" (vec @all-film-ids))
    (println "We now have" (count @all-film-ids) "films to create.")))

(defn create-films
  [tmdb-ids]
  (let [futures  (doall (map (fn [tmdb-id] (future (time (create-film tmdb-id)))) tmdb-ids))
        timeouts (not-empty (doall (map deref futures)))]
    (println "Dereferenced all of the futures.")))

(defn create-chunk-films
  [filename-base chunk-num total-chunks]
  (let [filename      (str filename-base chunk-num ".txt")
        film-tmdb-ids (vec (files/read-file filename))]
    (println "\n------------------------> CHUNK" chunk-num "/" total-chunks "<------------------------\n")
    (create-films film-tmdb-ids)))

(defn create-films-in-chunk
  [filename-base start-chunk num-chunks]
  (conc/do-concurrently
    (fn []
      (let [file-nums          (range start-chunk num-chunks)
            continue-creating? (atom true)]
        (doseq [file-num file-nums]
          (when @continue-creating?
            (let [chunk-future (future (create-chunk-films filename-base file-num num-chunks))
                  timeout      (deref chunk-future 1100000 :timeout)]
              (when timeout
                (reset! continue-creating? false)
                (println "Timed out!")
                (create-films-in-chunk filename-base file-num num-chunks)))))))))

(defn- create-films-in-film-chunks
  [start-chunk num-chunks]
  (create-films-in-chunk "film-ids-" start-chunk num-chunks))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Going to get all of the films for each year.")
  (generate-film-ids-file)
  (println "Got all of the films for each year. Going to divide the films file into many chunks.")
  (let [num-chunks  (files/chunk-vector-into-files "film-ids-" "film-ids.txt" 1024)]
    (read-in-films-file)
    (create-films-in-film-chunks 0 num-chunks)))
