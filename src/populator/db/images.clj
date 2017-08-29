(ns populator.db.images
  (:require [populator.db.sql-adapter :as sql-fns]
            [populator.db :as db :refer [dbconn]]
            [populator.utils.general :refer :all]))

(defn find-image
  "Finds an image record."
  [path]
  (when-let [path (extract-string path)]
    (db/exec #(sql-fns/get-image dbconn {:path path}))))

(defn validate-image-data
  ;; TODO: Create tests for this
  "Returns true or nil depending on whether the image data is valid
  for database insertion."
  [image-data]
  (when-let [image-data (extract-map image-data)]
    (let [criteria [#(extract-number % :aspect_ratio)
                    #(extract-string % :path)
                    #(extract-number % :height)
                    #(extract-number % :width)]]
      (db/validate-data image-data criteria))))

(defn create-image
  "Creates an image record."
  [image-data]
  (when-let [image-data (extract-map image-data)]
    (when (validate-image-data image-data)
      (db/exec #(sql-fns/insert-image dbconn image-data)))))

(defn find-imageset
  "Finds an imageset record."
  [query]
  (when-let [query (extract-map query)]
    (cond
      (some? (extract-number query :tmdb_id)) (db/exec #(sql-fns/get-imageset-by-tmdb-id dbconn query)))))

(defn create-imageset
  "Creates an imageset record."
  [imageset-data]
  (when-let [imageset-data (extract-map imageset-data)]
    (let [criteria                [#(extract-number % :tmdb_id)]
          backdrops               (db/create-int-array (extract-coll imageset-data :backdrops))
          posters                 (db/create-int-array (extract-coll imageset-data :posters))
          imageset-representation {:tmdb_id   (extract-number imageset-data :tmdb_id)
                                   :backdrops backdrops
                                   :posters   posters}]
      (when (db/validate-data imageset-representation criteria)
        (db/exec #(sql-fns/insert-imageset dbconn imageset-representation))))))
