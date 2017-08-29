(ns populator.film-api.studios
  (:require [populator.db.studios :as db-studios]
            [clojure.core.async :as async]
            [populator.db.films :as db-films]
            [populator.db :as db]
            [populator.film-api.tmdb :as tmdb]
            [populator.utils.general :refer :all]))

(def inprog-studios (atom {}))

(defn create-studio-data
  "Creates a map representation of a studio record."
  [tmdb-id]
  (when-let [tmdb-id (extract-number tmdb-id)]
    (when-let [data (tmdb/get-studio-details tmdb-id)]
      {:name        (extract-string data :name)
       :tmdb_id     (extract-number tmdb-id)
       :description (extract-string data :description)
       :hq          (extract-string data :headquarters)
       :website     (extract-string data :homepage)})))

(defn create-studio
  "Creates a studio record in the database and returns its a map with
  its ID."
  [tmdb-id]
  (when-let [tmdb-id (extract-number tmdb-id)]
    (when-let [data (create-studio-data tmdb-id)]
      (println "CREATING STUDIO:" (extract-string data :name))
      (db-studios/create-studio data))))

(defn find-studio
  "Finds a studio record."
  [query]
  (when-let [query (extract-map query)]
    (db-studios/find-studio query)))

(defn find-or-create-studio
  "Finds or creates a studio record, and returns a map that at least
  has its ID."
  [query]
  (when-let [query (extract-map query)]
    (if-let [studio (find-studio query)]
      studio
      (when-let [tmdb-id (extract-number query :tmdb_id)]
        (let [id-key   (keyword (str tmdb-id))
              receptor (atom nil)]
          ;; Is this studio in the process of being created?
          (locking inprog-studios
            (if-let [awaitors-details (get @inprog-studios id-key)]
              ;; Yes, join the queue of threads awaiting a copy once it's created
              (let [receptors (get awaitors-details :receptors)]
                (reset! receptor (async/chan 1))
                (swap! inprog-studios assoc id-key (conj receptors receptor)))

              ;; No, go ahead and record that you're starting the process (update the atom)
              (swap! inprog-studios assoc id-key [])))
          (if (some? @receptor)
            ;; Block this thread until it receives the studio, and return that
            (let [studio (async/<!! @receptor)]
              (async/close! @receptor)
              studio)

            ;; Create the studio object, give it to the awaiting threads, and return the studio
            (let [studio             (create-studio tmdb-id)
                  awaiting-receptors (atom nil)]
              (locking inprog-studios
                (let [found-receptors (get @inprog-studios id-key)]
                  ;; Remove this studio from inprogress-studios
                  (reset! awaiting-receptors found-receptors)
                  (swap! inprog-studios dissoc id-key)))

              ;; Send a copy of the newly-created studio to all of the waiting threads
              (if (not-empty @awaiting-receptors)
                (doall (map #(async/>!! % studio) @awaiting-receptors)))
              studio)))))))

(defn find-or-create-studios
  "Finds or creates mutliple studios."
  [queries]
  (when-let [queries (not-empty (filter some? (extract-coll queries)))]
    (doall (map find-or-create-studio queries))))

(defn update-film-studios
  "Updates a film record's studios."
  [film-id studio-ids]
  (let [film-id    (extract-number film-id)
        studio-ids (db/create-int-array (not-empty (filter number? (extract-coll studio-ids))))]
    (when (and film-id studio-ids)
      (db-films/update-film-studios {:id      film-id
                                     :studios studio-ids}))))
