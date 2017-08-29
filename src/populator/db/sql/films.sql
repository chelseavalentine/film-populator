-- A :result value of :n below will return affected row count:
-- :name insert-film :? :n
-- :doc Inserts a film and returns the newly created object
INSERT INTO web_film (title, alt_titles, overview, tagline, year, released, release_date, runtime,
                      poster_path, backdrop_path, genres, keywords, language, languages, countries,
                      budget, revenue, is_adult, imdb_id, tmdb_id)
VALUES
  (:title, :alt_titles, :overview, :tagline, :year, :released, :release_date, :runtime, :poster_path, :backdrop_path,
           :genres, :keywords, :language, :languages, :countries, :budget, :revenue, :is_adult, :imdb_id, :tmdb_id)
RETURNING *;

-- A ":result" value of ":1" specifies a single record (as a hashmap)
--  will be returned
-- :name get-film-by-id :? :1
-- :doc Gets a film by its ID
SELECT *
FROM web_film
WHERE id = :id;

-- :name get-film-by-tmdb-id :? :1
-- :doc Gets a film by its tmdb ID
SELECT *
FROM web_film
WHERE tmdb_id = :tmdb_id;

-- :name update-film-imageset :? :n
-- :doc Updates a film's imageset and returns the updated film
UPDATE web_film
SET imageset = :imageset
WHERE id = :id
RETURNING *;

-- :name update-film-studios :? :n
-- :doc Updates a film's studios and returns the updated film
UPDATE web_film
SET studios = :studios
WHERE id = :id
RETURNING *;

-- :name update-film-directors :? :n
-- :doc Updates a film's directors and returns the updated film
UPDATE web_film
SET directors = :directors
WHERE id = :id
RETURNING *;

-- :name get-duplicate-films :? :n
-- :doc Finds duplicate films
SELECT MAX (id)
FROM web_film
GROUP BY tmdb_id
HAVING COUNT (*) > 1;

-- :name delete-duplicate-films :? :n
-- :doc Deletes duplicate films
DELETE FROM web_film
WHERE id IN (
  SELECT MAX (id)
  FROM web_film
  GROUP BY tmdb_id
  HAVING COUNT (*) > 1);

-- :name delete-backdrop-images-for-duplicate-films :? :n
-- :doc Deletes the duplicate films' backdrop images
DELETE FROM web_image
WHERE id IN (
  SELECT UNNEST (backdrops)
  FROM web_imageset
  WHERE id IN (
    SELECT imageset
    FROM web_film
    WHERE id IN (
      SELECT MAX (id)
      FROM web_film
      GROUP BY tmdb_id
      HAVING COUNT (*) > 1
    )
  )
);

-- :name delete-poster-images-for-duplicate-films :? :n
-- :doc Deletes the duplicate films' poster images
DELETE FROM web_image
WHERE id IN (
  SELECT UNNEST (posters)
  FROM web_imageset
  WHERE id IN (
    SELECT imageset
    FROM web_film
    WHERE id IN (
      SELECT MAX (id)
      FROM web_film
      GROUP BY tmdb_id
      HAVING COUNT (*) > 1
    )
  )
);

-- :name delete-imagesets-for-duplicate-films :? :n
-- :doc Deletes the duplicate films' imagesets
DELETE FROM web_imageset
WHERE id IN (
  SELECT imageset
  FROM web_film
  WHERE id IN (
    SELECT MAX (id)
    FROM web_film
    GROUP BY tmdb_id
    HAVING COUNT (*) > 1
  )
);
