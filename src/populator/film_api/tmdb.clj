(ns populator.film-api.tmdb
  "Abstracts away dealing with the TMDB film API."
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [slingshot.slingshot :as slingshot]
            [environ.core :refer [env]]
            [populator.utils.general :refer :all])
  (:import (java.io InputStream
                    InputStreamReader
                    BufferedReader
                    FileNotFoundException)
           (java.net URL HttpURLConnection)))

(def api-url-base "https://api.themoviedb.org/3/")
(def api-key (if (zero? (rand-int 2)) (env :tmdb-api-key) (env :tmdb-api-key-2)))

(defn call-api
  "Makes an API call, and retries upon rate limiting, or service
  unavailability. Returns nil if requested resource wasn't found."
  [f]
  (when (fn? f)
    (slingshot/try+
      (f)
      (catch Exception exception-string
        (let [exception-properties (read-string (.getMessage exception-string))
              status               (extract-number exception-properties :status)
              retry-in             (extract-number exception-properties :retry-in)]
          (cond
            (some? retry-in) (do
                               (Thread/sleep retry-in)
                               (call-api #(f)))
            (= status 404) (println "Couldn't find a resource. It seems to have been deleted.")
            :else (do
                    (let [rand-retry-in (* 10 (+ 100 (rand-int 900)))]
                      (println (str status ": The API seems to be down. Trying again in" (double (/ rand-retry-in 1000)) "s."))
                      ;(if-not status (println exception-properties))
                      (Thread/sleep rand-retry-in)
                      (call-api #(f))))))))))

(defn make-http-request
  "Makes a HTTP request with error handling. Returns status code, and,
  if available, an amount of time to retry in."
  [url]
  (when-let [url (extract-string url)]
    (let [conn (.openConnection (URL. url))]
      (.setRequestMethod conn "GET")
      (.connect conn)
      (when-not (= (.getResponseCode conn) 200)
        (let [retry-after       (.getHeaderField conn "Retry-After")
              retry-in          (if-not (empty? retry-after) (* (read-string retry-after) 1000))
              exception-details {:status   (.getResponseCode conn)
                                 :retry-in retry-in}]
          (throw (Exception. (str exception-details)))))
      (with-open [stream (BufferedReader. (InputStreamReader. (.getInputStream conn)))]
        (.toString (reduce #(.append %1 %2) (StringBuffer.) (line-seq stream)))))))

(defn format-additional-params
  "Ensures that the additional params are well-formatted. If they
  can't be coerced, an empty string is returned."
  [params]
  (if-let [params (extract-string params)]
    (if (clojure.string/starts-with? params "&")
      params
      (str "&" params))
    ""))

(defn request-tmdb
  ([path]
   (request-tmdb path ""))
  ([path additional-params]
   (let [path              (extract-string path)
         additional-params (not-empty (format-additional-params additional-params))]
     (when path
       (let [base-params (str "?api_key=" api-key "&include_adult=false&include_video=false")
             request-url (str api-url-base path base-params additional-params)
             response    (call-api #(make-http-request request-url))]
         (when response
           (json/read-str response :key-fn keyword)))))))

(defn discover-year-films
  "Gets all a page of a year's films, and returns a vector of those
  films' IDs."
  ([year]
   (discover-year-films year 1))
  ([year page]
   (when (and (extract-positive-number year) (extract-positive-number page))
     (let [path     "discover/movie"
           params   (str "&year=" year "&page=" page)
           response (request-tmdb path params)]
       {:total_pages (extract-positive-number response :total_pages)
        :films       (extract-coll response :results)}))))

(defn- get-discover-year-result-ids
  [discover-year-promise]
  (let [discover-year-data @discover-year-promise
        films-data         (extract-coll discover-year-data :films)
        film-ids           (not-empty (filter some? (map #(extract-number % :id) films-data)))]
    film-ids))

(defn get-year-films
  "Gets all of the films created in a year and returns a vector of
   film IDs. Returns nil if no films, or invalid request."
  ([yr]
   (when-let [yr (extract-positive-number yr)]
     (println yr)
     (let [all-film-ids          (atom [])
           total-pages           (atom nil)
           discover-year-promise (deliver (promise) (discover-year-films yr 1))]
       ;; Get initial page to know how many pages there are
       (let [discover-year-data @discover-year-promise
             films-data         (extract-coll discover-year-data :films)
             film-ids           (not-empty (filter some? (map #(extract-number % :id) films-data)))]
         (swap! total-pages (constantly (extract-number discover-year-data :total_pages)))
         (swap! all-film-ids #(concat % film-ids))
         ;; Get the rest of the pages concurrently
         (when (> @total-pages 1)
           (let [pages    (range 2 (inc @total-pages))
                 promises (doall (map #(let [p (promise)] (deliver p (discover-year-films yr %))) pages))]
             (doseq [curr-promise promises]
               (swap! all-film-ids #(concat % (get-discover-year-result-ids curr-promise))))))
         (shutdown-agents)
         @all-film-ids))))
  ([start-year end-year]
   (let [start-year (extract-positive-number start-year)
         end-year   (extract-positive-number end-year)
         years      (not-empty (range start-year (inc end-year)))]
     (when years
       (let [film-ids (atom [])
             promises (doall (map (fn [yr] (let [p (promise)] (deliver p (get-year-films yr)))) years))]
         (doseq [curr-promise promises]
           (when-let [year-film-ids (not-empty @curr-promise)]
             (swap! film-ids #(concat % year-film-ids))))
         (shutdown-agents)
         @film-ids)))))

(defn get-film-info
  "Gets information for a film, which includes images, alternative
  titles, credits, and keywords."
  [tmdb-id]
  (let [tmdb-id           (extract-number tmdb-id)
        additional-params (str "&language=en-US"
                            "&append_to_response=images,alternative_titles,credits,keywords"
                            "&include_image_language=en,null")
        path              (str "movie/" tmdb-id)]
    (if tmdb-id (request-tmdb path additional-params))))

(defn get-studio-details
  "Gets a studio's details."
  [tmdb-id]
  (when-let [tmdb-id (extract-number tmdb-id)]
    (request-tmdb (str "company/" tmdb-id))))

(defn get-person-details
  "Gets a person's details."
  [tmdb-id]
  (when-let [tmdb-id (extract-number tmdb-id)]
    (request-tmdb (str "person/" tmdb-id))))

(defn get-person-credits-ids
  "Gets the film credits for a person."
  [person-tmdb-id]
  (println person-tmdb-id)
  (when-let [person-tmdb-id (extract-number person-tmdb-id)]
    (let [path          (str "person/" person-tmdb-id "/movie_credits")
          film-tmdb-ids (atom [])
          response      (not-empty (request-tmdb path))
          cast-films    (not-empty (extract-value response :cast))
          crew-films    (not-empty (extract-value response :crew))]
      (when cast-films
        (swap! film-tmdb-ids concat (doall (map #(% :id) cast-films))))
      (when crew-films
        (swap! film-tmdb-ids concat (doall (map #(% :id) crew-films))))
      (vec (set @film-tmdb-ids)))))

(defn get-studio-credits-page
  ([tmdb-id]
   (get-studio-credits-page tmdb-id 1))
  ([tmdb-id page]
   (let [tmdb-id (extract-positive-number tmdb-id)
         page    (extract-positive-number page)]
     (when (and tmdb-id page)
       (let [path     (str "company/" tmdb-id "/movies")
             params   (str "&page=" page)
             response (request-tmdb path params)]
         {:total_pages (extract-positive-number response :total_pages)
          :films       (extract-coll response :results)})))))

(defn- get-studio-credits-ids
  [studio-credits-promise]
  (let [studio-credits-data @studio-credits-promise
        films-data (extract-coll studio-credits-data :films)
        film-tmdb-ids (not-empty (filter some? (map #(extract-number % :id) films-data)))]
    film-tmdb-ids))

(defn get-studio-credits
  [tmdb-id]
  (when-let [tmdb-id (extract-positive-number tmdb-id)]
    (println tmdb-id)
    (let [all-film-ids (atom [])
          total-pages (atom nil)
          studio-credits-promise (deliver (promise) (get-studio-credits-page tmdb-id 1))]
      ;; Get the initial page so we know the total pages
      (let [studio-credits-data @studio-credits-promise
            films-data (extract-coll studio-credits-data :films)
            film-ids (get-studio-credits-ids studio-credits-promise)]
        (reset! total-pages (extract-number studio-credits-data :total_pages))
        (swap! all-film-ids concat film-ids)
        ;; Get the rest of the pages concurrently
        (when (> @total-pages 1)
          (let [pages (range 2 (inc @total-pages))
                promises (doall (map #(let [p (promise)] (deliver p (get-studio-credits-page tmdb-id %))) pages))]
            (doseq [curr-promise promises]
              (swap! all-film-ids concat (get-studio-credits-ids curr-promise)))
            (shutdown-agents)
            @all-film-ids))))))
