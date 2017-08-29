-- A :result value of :n below will return affected row count:
-- :name insert-person :? :n
-- :doc Inserts a person and returns the newly-created person
INSERT INTO web_person (name, alt_names, bio, birthdate, deathdate, gender, hometown, image_path, website, imdb_id, tmdb_id)
VALUES (:name, :alt_names, :bio, :birthdate, :deathdate, :gender, :hometown, :image_path,
               :website, :imdb_id, :tmdb_id)
RETURNING *;

-- :name insert-people :! :n
-- :doc Inserts people with :tuple* parameter type and returns their IDs
INSERT INTO web_person
(name, alt_names, bio, birthdate, deathdate, gender, hometown, image_path, website, imdb_id, tmdb_id)
VALUES :tuple*:people;

-- A ":result" value of ":1" specifies a single record (as a hashmap)
--  will be returned
-- :name get-person-by-tmdb-id :? :1
-- :doc Retrieves a person by their tmdb-id
SELECT *
FROM web_person
WHERE tmdb_id = :tmdb_id;

-- :name get-people-tmdb-ids :? :*
-- :doc Retrieves all people from the database
SELECT tmdb_id
FROM web_person;

-- :name get-duplicate-people :? :n
-- :doc Finds duplicate people
SELECT MAX (id)
FROM web_person
GROUP BY tmdb_id
HAVING COUNT (*) > 1;

-- :name delete-duplicate-people :? :n
-- :doc Deletes duplicate people
DELETE FROM web_person
WHERE id IN (
  SELECT MAX (id)
  FROM web_person
  GROUP BY tmdb_id
  HAVING COUNT (*) > 1);
