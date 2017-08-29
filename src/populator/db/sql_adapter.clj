(ns populator.db.sql-adapter
  (:require [hugsql.core :as hugsql]
            [populator.utils.general :refer :all]))

(hugsql/def-db-fns "populator/db/sql/films.sql")
(hugsql/def-db-fns "populator/db/sql/cast-members.sql")
(hugsql/def-db-fns "populator/db/sql/crew-members.sql")

(hugsql/def-db-fns "populator/db/sql/studios.sql")
(hugsql/def-db-fns "populator/db/sql/studio-films.sql")

(hugsql/def-db-fns "populator/db/sql/people.sql")

(hugsql/def-db-fns "populator/db/sql/works.sql")
(hugsql/def-db-fns "populator/db/sql/roles.sql")

(hugsql/def-db-fns "populator/db/sql/images.sql")
(hugsql/def-db-fns "populator/db/sql/imagesets.sql")

(defn convert-to-ordered-vector
  "Converts data to an ordered vector."
  [ordering data]
  (let [ordering (not-empty (extract-coll ordering))
        data     (extract-map data)]
    (when (and ordering data)
      (map #(extract-value data %) ordering))))
