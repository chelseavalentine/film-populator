-- A :result value of :n below will return affected row count:
-- :name insert-studio :? :n
-- :doc Inserts a studio and returns its id
INSERT INTO web_studio (name, description, hq, website, tmdb_id)
VALUES (:name, :description, :hq, :website, :tmdb_id)
RETURNING id;

-- A ":result" value of ":1" specifies a single record (as a hashmap)
--  will be returned
-- :name get-studio-by-tmdb-id :? :1
-- :doc Retrieves a studio by its tMDB id
SELECT *
FROM web_studio
WHERE tmdb_id = :tmdb_id;

-- :name get-studio-by-id :? :1
-- :doc Retrieves a studio by its ID
SELECT *
FROM web_studio
WHERE id = :id;

-- :name get-studio-tmdb-ids :? :*
-- :doc Retrieves all studios' tmdb ids from the database
SELECT tmdb_id
FROM web_studio;

-- :name get-studio-by-tmdb-id :? :1
-- :doc Retrieves a studio by its tmdb ID
SELECT *
FROM web_studio
WHERE tmdb_id = :tmdb_id;

-- :name get-duplicate-studios :? :n
-- :doc Finds duplicate studios
SELECT MAX (id)
FROM web_studio
GROUP BY tmdb_id
HAVING COUNT (*) > 1;

-- :name delete-duplicate-studios :? :n
-- :doc Deletes duplicate studios
DELETE FROM web_studio
WHERE id IN (
  SELECT MAX (id)
  FROM web_studio
  GROUP BY tmdb_id
  HAVING COUNT (*) > 1);
