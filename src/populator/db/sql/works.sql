-- A :result value of :n below will return affected row count:
-- :name insert-work :? :n
-- :doc Inserts a work and returns its id
INSERT INTO web_work (title, year, released, release_date, runtime, poster_path, backdrop_path,
                      genres, budget, revenue, is_adult, imdb_id, tmdb_id, film, person)
VALUES (:title, :year, :released, :release_date, :runtime, :poster_path,
                :backdrop_path, :genres, :budget, :revenue, :is_adult, :imdb_id, :tmdb_id,
        :film, :person)
RETURNING *;

-- A ":result" value of ":1" specifies a single record (as a hashmap)
--  will be returned
-- :name get-work :? :1
-- :doc Retrieves a work by a person ID and a film ID
SELECT *
FROM web_work
WHERE film = :film
      AND person = :person;

-- NOTE: Untested
-- :name get-duplicate-works :? :n
-- :doc Finds duplicate works
SELECT MAX (id)
FROM web_work
GROUP BY person, tmdb_id
HAVING COUNT (*) > 1;

-- NOTE: Untested
-- :name delete-duplicate-works :? :n
-- :doc Deletes duplicate works
DELETE FROM web_work
WHERE id IN (
  SELECT MAX (id)
  FROM web_work
  GROUP BY person, tmdb_id
  HAVING COUNT (*) > 1);
