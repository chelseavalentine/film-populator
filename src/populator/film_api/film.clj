(ns populator.film-api.film
  (:require [populator.film-api.tmdb :as tmdb]
            [populator.film-api.images :as image-fns]
            [populator.film-api.studios :as studio-fns]
            [populator.film-api.studio-films :as studio-film-fns]
            [populator.film-api.people :as person-fns]
            [populator.film-api.credits :as credits-fns]
            [populator.utils.time :as time]
            [populator.utils.general :refer :all]
            [populator.db.films :as db-films]))

(defn setup-film-titles
  "Processes film titles and returns updated film data. Returns nil if the
  parameter values are of incorrect types."
  [response film-data]
  (let [response (extract-map response)
        data     (extract-map film-data)]
    (when (and response data)
      (let [updated-data   (atom data)
            film-title     (extract-string data :title)
            original-title (extract-string response :original_title)]
        (when (and original-title (.equals film-title original-title))
          (swap! updated-data #(assoc % :alt_titles [original-title])))
        (let [film-alt-titles     (atom (extract-coll updated-data :alt_titles))
              alt-titles-response (extract-coll response :alternative_titles)
              alt-titles          (not-empty (extract-property-items alt-titles-response :titles :title))]
          (when alt-titles
            (if-not @film-alt-titles
              (reset! film-alt-titles alt-titles)
              (swap! film-alt-titles #(concat % alt-titles)))
            (swap! updated-data #(assoc % :alt_titles @film-alt-titles))))
        @updated-data))))

(defn setup-film-release-dates
  "Processes film release dates data and returns updated film data. Returns
  nil if the parameter values are of incorrect types."
  [response data]
  (let [response (extract-map response)
        data     (extract-map data)]
    (when (and response data)
      (when-let [release-date-string (extract-string response :release_date)]
        (if-let [release-date (time/create-sql-date release-date-string)]
          (let [updated-film-data (assoc data :release_date release-date)
                year              (time/get-year release-date)]
            (if year
              (assoc updated-film-data :year year)
              updated-film-data))
          data)))))

(defn setup-film-keywords
  "Processes film keywords data and returns the updated film data.
  Returns nil if the parameter values are of incorrect types."
  [response film-data]
  (let [response  (extract-map response)
        film-data (extract-map film-data)]
    (when (and response film-data)
      (let [keywords-data (not-empty (extract-coll response :keywords))
            keywords      (not-empty (extract-property-items keywords-data :keywords :name))]
        (if keywords
          (assoc film-data :keywords keywords)
          film-data)))))

(defn is-released?
  [response]
  (when-let [response (extract-map response)]
    (let [status       (extract-string response :status)
          release-date (extract-string response :release_date)]
      (cond
        (some? status) (.equals status "Released")
        (some? release-date) (time/is-future? release-date)))))

(defn create-film-data
  "Creates and returns film data for a given TMDB ID."
  [response]
  (when-let [response (extract-map response)]
    (let [is-released (true? (is-released? response))
          data        (atom {:title         (extract-string response :title)
                             :overview      (extract-string response :overview)
                             :tagline       (extract-string response :tagline)
                             :runtime       (extract-positive-number response :runtime)
                             :released      is-released
                             :poster_path   (extract-string response :poster_path)
                             :backdrop_path (extract-string response :backdrop_path)
                             :budget        (extract-number response :budget)
                             :revenue       (extract-number response :revenue)
                             :genres        (extract-property-items response :genres :name)
                             :language      (extract-string response :original_language)
                             :languages     (extract-property-items response :spoken_languages :iso_639_1)
                             :countries     (extract-property-items response :production_countries :iso_3166_1)
                             :is_adult      (extract-boolean response :adult)
                             :tmdb_id       (extract-number response :id)
                             :imdb_id       (extract-string response :imdb_id)})]
      (swap! data #(setup-film-titles response %))
      (swap! data #(setup-film-release-dates response %))
      (swap! data #(setup-film-keywords response %))
      @data)))

(defn create-film-images
  "Process film images data and creates image records."
  [response film]
  (let [response (extract-map response)
        film     (extract-map film)]
    (when (and response film)
      (let [film-tmdb-id (extract-number film :tmdb_id)
            images-data  (not-empty (extract-coll response :images))
            imageset-id  (image-fns/find-or-create-imageset (assoc images-data :tmdb_id film-tmdb-id))]
        (when imageset-id
          (db-films/update-film-imageset {:id       (extract-number film :id)
                                          :imageset imageset-id}))
        film))))

(defn create-film-studios
  "Processes film studios and creates the studio records for a film."
  [response film]
  (let [response (extract-map response)
        film     (extract-map film)]
    (when (and response film)
      (if-let [studio-tmdb-ids (not-empty (filter some? (extract-property-items response :production_companies :id)))]
        (let [film-id        (extract-number film :id)
              studio-queries (doall (map (fn [tmdb-id] {:tmdb_id tmdb-id}) studio-tmdb-ids))
              studios        (studio-fns/find-or-create-studios studio-queries)
              studio-ids     (not-empty (filter some? (map #(extract-number % :id) studios)))]
          (if studio-ids
            (do
              (doall (map #(studio-film-fns/find-or-create-studio-film % film) studio-ids))
              (studio-fns/update-film-studios film-id studio-ids))
            film))
        film))))

(defn create-film-credits
  "Processes film credits and creates the people and cast member
  records for a film."
  [response film]
  (let [response (extract-map response)
        film     (extract-map film)]
    (when (and response film)
      (if-let [credits (extract-map response :credits)]
        (let [cast-member-data    (extract-coll credits :cast)
              crew-member-data    (extract-coll credits :crew)
              credits-people-data (concat cast-member-data crew-member-data)
              people-ids          (vec (set (doall (map #(extract-number % :id) credits-people-data))))
              people-queries      (map (fn [tmdb-id] {:tmdb_id tmdb-id}) people-ids)]
          (when people-ids
            (let [people       (person-fns/find-or-create-people people-queries)
                  cast-members (credits-fns/find-or-create-cast film cast-member-data)
                  crew-members (credits-fns/find-or-create-crew film crew-member-data)
                  directors    (filter #(.equals "Director" (extract-string % :job)) crew-members)
                  director-ids (not-empty (filter some? (map #(extract-number % :id) directors)))
                  film-id      (extract-number film :id)]
              (when director-ids
                (credits-fns/update-directors film-id director-ids)))))
        film))))

(defn create-film
  "Creates a film record in the database."
  [tmdb-id]
  (let [tmdb-id       (extract-number tmdb-id)
        existing-film (db-films/find-film {:tmdb_id tmdb-id})]
    ;; Only bother doing the whole process if the last step hasn't been completed yet
    (when-not (extract-value existing-film :directors)
      (when-let [response (tmdb/get-film-info (extract-number tmdb-id))]
        (println "CREATING FILM:" (extract-string response :title))
        (when-let [data (create-film-data response)]
          (let [film (or existing-film (db-films/create-film data))]
            (create-film-images response film)
            (create-film-studios response film)
            (create-film-credits response film)))))))
