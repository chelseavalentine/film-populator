-- A :result value of :n below will return affected row count:
-- :name insert-studio-film :? :n
-- :doc Inserts a studio's film and returns its id
INSERT INTO web_studiofilm (title, year, released, release_date, runtime, poster_path, backdrop_path, genres, budget,
                            revenue, is_adult, imdb_id, tmdb_id, film, studio)
VALUES (:title, :year, :released, :release_date, :runtime, :poster_path, :backdrop_path, :genres, :budget, :revenue,
                :is_adult, :imdb_id, :tmdb_id, :film, :studio)
RETURNING id;

-- A ":result" value of ":1" specifies a single record (as a hashmap)
--  will be returned
-- :name get-studio-film :? :1
-- :doc Retrieves a studio film by the studio id and the film's tMDB id
SELECT *
FROM web_studiofilm
WHERE film = :film AND
      studio = :studio;

-- :name get-duplicate-studio-films :? :n
-- :doc Finds duplicate studio films
SELECT MAX (id)
FROM web_studiofilm
GROUP BY film, studio
HAVING COUNT (*) > 1;

-- :name delete-duplicate-studio-films :? :n
-- :doc Deletes duplicate studio films
DELETE FROM web_studiofilm
WHERE id IN (
  SELECT MAX (id)
  FROM web_studiofilm
  GROUP BY film, studio
  HAVING COUNT (*) > 1);
