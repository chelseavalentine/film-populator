-- A :result value of :n below will return affected row count:
-- :name insert-imageset :? :n
-- :doc Inserts an imageset and returns the new imageset's id
INSERT INTO web_imageset (tmdb_id, backdrops, posters)
VALUES (:tmdb_id, :backdrops, :posters)
RETURNING id;

-- A ":result" value of ":1" specifies a single record (as a hashmap)
--  will be returned
-- :name get-imageset-by-tmdb-id :? :1
-- :doc Retrieves an imageset by its tMDB id
SELECT *
FROM web_imageset
WHERE :tmdb_id = tmdb_id;

-- :name get-duplicate-imagesets :? :n
-- :doc Finds duplicate imagesets
SELECT
  tmdb_id,
  COUNT (*)
FROM web_imageset
GROUP BY tmdb_id
HAVING COUNT (*) > 1;

-- :name delete-duplicate-imagesets :? :n
-- :doc Deletes duplicate imagesets
DELETE FROM web_imageset
WHERE id IN (
  SELECT MAX (id)
  FROM web_imageset
  GROUP BY tmdb_id
  HAVING COUNT (*) > 1);
