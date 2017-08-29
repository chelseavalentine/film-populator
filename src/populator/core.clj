(ns populator.core
  (:gen-class)
  (:require [populator.utils.files :as files]
            [populator.film-api.tmdb :as tmdb]
            [populator.film-api.film :as film-fns]
            [populator.db.films :as films]
            [populator.utils.concurrent :as conc]
            [populator.person-populator :as pp]
            [populator.studio-populator :as sp]
            [clojure.core.async :as async]
            [populator.db.people :as people]))

(defn generate-film-ids-file
  []
  (let [film-ids (tmdb/get-year-films 1800 2060)]
    (files/write "film-ids.txt" (vec (set film-ids)))))

(defn create-films
  [tmdb-ids]
  (let [futures  (doall (map (fn [tmdb-id] (future (time (film-fns/create-film tmdb-id)))) tmdb-ids))
        timeouts (not-empty (doall (map deref futures)))]
    (println "Dereferenced all of the futures.")))

(defn create-chunk-films
  [filename-base chunk-num]
  (let [filename      (str filename-base chunk-num ".txt")
        film-tmdb-ids (vec (files/read-file filename))]
    (println "\n------------------------> CHUNK" chunk-num "<------------------------\n")
    (create-films film-tmdb-ids)))

(defn create-films-in-chunk
  [filename-base start-chunk num-chunks]
  (conc/do-concurrently
    (fn []
      (let [file-nums          (range start-chunk num-chunks)
            continue-creating? (atom true)]
        (doseq [file-num file-nums]
          (when @continue-creating?
            (let [chunk-future (future (create-chunk-films filename-base file-num))
                  timeout      (deref chunk-future 1100000 :timeout)]
              (when timeout
                (reset! continue-creating? false)
                (println "Timed out!")
                (create-films-in-chunk filename-base file-num num-chunks)))))))))

(defn- create-films-in-film-chunks
  [start-chunk num-chunks]
  (create-films-in-chunk "film-ids-" start-chunk num-chunks))

(defn- create-films-in-people-chunks
  [start-chunk num-chunks]
  (create-films-in-chunk "ppl-ids-" start-chunk num-chunks))

(defn- create-films-in-studio-chunks
  [start-chunk num-chunks]
  (create-films-in-chunk "sf-ids-" start-chunk num-chunks))

(defn- consolidate-films
  [filename-base start-chunk end-chunk result-base]
  (let [film-tmdb-ids (set (files/combine-files filename-base start-chunk end-chunk))]
    (print (count film-tmdb-ids))
    (files/write "narrowed-down-studios.txt" (vec film-tmdb-ids))))

(defn create-find-film-future
  [i j chunk-size film-tmdb-ids]
  (let [film-index (+ (* i chunk-size) j)]
    (if (> 57476 film-index)
      (let [film-tmdb-id (nth film-tmdb-ids film-index)]
        (future
          (when (nil? (films/find-film {:tmdb_id film-tmdb-id}))
            (println "Need to create" film-tmdb-id)
            film-tmdb-id)))
      (future (when (nil? nil) nil)))))


(defn- narrow-down-films
  []
  (println "Going to read in the files.")
  (conc/do-concurrently
    (fn []
      (let [film-tmdb-ids (files/read-file "narrowed-down-studios.txt")]
        (let [films-to-create (atom (set nil))
              film-count      (count film-tmdb-ids)
              curr-film       (atom 0)]
          (println "Nice. Found" film-count "films to create.")
          (let [chunk-size 128
                num-chunks (Math/ceil (/ film-count chunk-size))]
            (print "Going to use" num-chunks "chunks.")
            (doseq [i (range num-chunks)]
              (println "\n\n-------------------------STUDIO CHUNK" i "/" num-chunks "\n\n")
              (let [j-indices (range chunk-size)
                    futures   (doall (map #(create-find-film-future i % chunk-size film-tmdb-ids) j-indices))
                    results   (set (filter some? (doall (map deref futures))))]
                (println results)
                (swap! films-to-create clojure.set/union results))))
          (println "Nice! Got the films.")
          (files/write "studios-films-to-create-ids.txt" (vec @films-to-create)))))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ;(pp/populate-people)
  ;(sp/populate-studios)

  ;(narrow-down-films)
  (let [start-chunk (read-string (first args))
        num-chunks  (read-string (last args))]
    (create-films-in-people-chunks start-chunk num-chunks))
  ;(consolidate-films "studios-film-ids-" start-chunk num-chunks "result-")
  ;(println (files/chunk-vector-into-files "ppl-ids-" "to-create.txt" 1024))
  ;(create-films-in-people-chunks start-chunk num-chunks)
  ;)
  )
