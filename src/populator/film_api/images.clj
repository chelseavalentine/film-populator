(ns populator.film-api.images
  (:require [populator.db.images :as db-images]
            [populator.utils.general :refer :all]
            [populator.film-api.tmdb :as tmdb]))

(defn create-image
  "Creates an image in the database."
  [raw-data]
  (when-let [raw-data (extract-map raw-data)]
    (let [image-data {:aspect_ratio (extract-number raw-data :aspect_ratio)
                      :path         (extract-string raw-data :file_path)
                      :height       (extract-number raw-data :height)
                      :width        (extract-number raw-data :width)}]
      (db-images/create-image image-data))))

(defn find-image
  "Finds an image in the database"
  [data]
  (when-let [data (extract-map data)]
    (let [path  (extract-string data :file_path)
          image (db-images/find-image path)]
      (println "IMG:" path)
      image)))

(defn find-or-create-image
  "Finds or creates an image in the database."
  [data]
  (when-let [data (extract-map data)]
    (if-let [image (find-image data)]
      image
      (create-image data))))

(defn find-or-create-images
  "Finds or creates and returns multiple images."
  [images-data]
  (if-let [images-data (not-empty (filter some? (extract-coll images-data)))]
    (map find-or-create-image images-data)
    []))

(defn create-imageset
  "Creates and returns an imageset record in the database."
  [data]
  (when-let [raw-data (extract-map data)]
    (let [backdrop-images (find-or-create-images (extract-coll raw-data :backdrops))
          poster-images   (find-or-create-images (extract-coll raw-data :posters))
          backdrops       (doall (map #(extract-number % :id) (filter some? backdrop-images)))
          posters         (doall (map #(extract-number % :id) (filter some? poster-images)))
          imageset-data   {:tmdb_id   (extract-number raw-data :tmdb_id)
                           :backdrops backdrops
                           :posters   posters}]
      (db-images/create-imageset imageset-data))))

(defn find-or-create-imageset
  "Finds or create imageset and returns its ID."
  [data]
  (when-let [data (extract-map data)]
    (when-let [tmdb-id (extract-number data :tmdb_id)]
      (if-let [imageset (db-images/find-imageset {:tmdb_id tmdb-id})]
        (extract-number imageset :id)
        (extract-number (create-imageset data) :id)))))
