(ns populator.film-api.people
  (:require [clojure.core.async :as async]
            [populator.db.people :as db-people]
            [populator.utils.general :refer :all]
            [populator.utils.time :as time]
            [populator.film-api.tmdb :as tmdb]))

(def inprogress-people (atom (set [])))
(def pending-threads (atom []))
(def found-people (atom {}))
(def found-person-ids (atom (set [])))

(defn clean-website
  [site]
  (when-let [site (extract-string site)]
    (let [site (.toLowerCase site)]
      (if (and (.contains site "google") (> (count site) 200))
        (let [site-query (last (.split (last (.split site "url=")) "%2f%2f"))
              domain     (first (.split site-query "%2f"))]
          domain)
        (if (> (count site) 200)
          nil
          site)))))

(defn create-person-data
  "Gets a person's information and creates a map representing
  a person object."
  [tmdb-id]
  (when-let [tmdb-id (extract-number tmdb-id)]
    (when-let [response (not-empty (tmdb/get-person-details tmdb-id))]
      (let [birthdate-string (extract-string response :birthday)
            deathdate-string (extract-string response :deathdate)]
        {:name       (extract-string response :name)
         :alt_names  (extract-coll response :also_known_as)
         :bio        (extract-string response :biography)
         :birthdate  (time/create-sql-date birthdate-string)
         :deathdate  (time/create-sql-date deathdate-string)
         :gender     (extract-number response :gender)
         :hometown   (extract-string response :place_of_birth)
         :image_path (extract-string response :profile_path)
         :website    (clean-website (extract-string response :homepage))
         :imdb_id    (extract-string response :imdb_id)
         :tmdb_id    tmdb-id}))))

(defn create-person
  "Creates a person entity in the database."
  [tmdb-id]
  (when-let [tmdb-id (extract-number tmdb-id)]
    (let [data (create-person-data tmdb-id)]
      (println "CREATING PERSON:" (extract-string data :name))
      (db-people/create-person data))))

(defn find-person
  [query]
  (when-let [query (extract-map query)]
    ;; Check for the person in our local copy first
    (let [tmdb-id         (query :tmdb_id)
          id-key          (keyword (str tmdb-id))
          existing-person (get @found-people id-key)]
      (if existing-person
        (do
          (println "Saved time~")
          existing-person)
        (let [found-person (db-people/find-person query)]
          (println "Finding person..." tmdb-id)
          (swap! found-people assoc id-key found-person)
          (swap! found-person-ids conj tmdb-id)
          found-person)))))

(defn find-or-create-person
  "Finds or creates a person in the database."
  [query]
  (when-let [query (extract-map query)]
    (if-let [person (find-person query)]
      person
      (create-person (extract-number :tmdb_id query)))))

(defn find-people-to-create
  [queries]
  (println "Going to try to find" (count queries) "people...")
  (if-let [queries (not-empty (filter some? (extract-coll queries)))]
    (not-empty (filter some? (filter #(nil? (find-person %)) queries)))))

(defn create-people
  "Creates multiple person records, given multiple TMDB IDs."
  [person-tmdb-ids]
  (when-let [person-tmdb-ids (not-empty (extract-coll person-tmdb-ids))]
    (let [people   (atom [])
          promises (doall (map #(let [p (promise)] (deliver p (create-person-data %))) person-tmdb-ids))]
      (doseq [curr-promise promises]
        (when-let [person (not-empty @curr-promise)]
          (swap! people #(conj % person))))
      (db-people/create-people @people))))

(defn these-people-are-being-worked-on?
  [tmdb-ids ready-receptor]
  (when-let [tmdb-ids (extract-coll tmdb-ids)]
    (locking inprogress-people
      (if-let [intersection (not-empty (clojure.set/intersection @inprogress-people (set tmdb-ids)))]
        ;; Are they already created?
        (clojure.set/subset? intersection @found-person-ids)

        ;; No other threads are currently working on these people, let's add these people!
        (do
          (swap! inprogress-people clojure.set/union (set tmdb-ids))

          ;; Close our receptor since we won't be waiting on anyone to start creating, and
          ;; thus don't need it
          (async/close! ready-receptor)
          false)))))

(defn notify-threads
  "Notify all threads to check whether they can create their people now"
  []
  (locking pending-threads
    (doall (map #(async/>!! % "Go ahead!") @pending-threads))
    (reset! pending-threads [])))

(defn find-or-create-people
  "Finds or create multiple people in the database."
  [queries]
  (when-let [queries (not-empty (extract-coll queries))]
    (let [related-tmdb-ids (doall (map #(extract-number % :tmdb_id) queries))
          ready-receptor   (async/chan 1)]
      (while (these-people-are-being-worked-on? related-tmdb-ids ready-receptor)
        ;; Add self to the pending threads
        (swap! pending-threads conj ready-receptor)
        (println "Waiting on my people~~~ . . . ")

        ;; Block this thread until it receives the go-ahead to retry from a thread that just
        ;; finished dealing w/ some people entities
        (println "Nice, another thread gave me the:" (async/<!! ready-receptor)))

      ;; Received the go-ahead that no one else is working on these people, so let's
      ;; create these people

      ;; Preemptively add all related tmdb IDs to in-progress to remove chances
      ;; of someone else working on these people
      (swap! inprogress-people clojure.set/union (set related-tmdb-ids))

      (let [people-to-create          (find-people-to-create queries)
            person-tmdb-ids-to-create (doall (map #(extract-number % :tmdb_id) people-to-create))
            exisiting-people-tmdb-ids (clojure.set/difference (set related-tmdb-ids) (set person-tmdb-ids-to-create))]
        ;; Remove existing people from in-progress people so it doesn't hold others up
        (swap! inprogress-people clojure.set/difference (set exisiting-people-tmdb-ids))
        (notify-threads)

        (let [created-people (create-people person-tmdb-ids-to-create)]
          ;; Created all our people, so remove related to this film from in-progress
          (swap! inprogress-people clojure.set/difference (set related-tmdb-ids))
          (notify-threads)
          (println created-people)
          created-people)))))

