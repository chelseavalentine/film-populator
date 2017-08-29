-- A :result value of :n below will return affected row count:
-- :name insert-cast-member :? :n
-- :doc Inserts a cast member and returns its id
INSERT INTO web_castmember (name, character, person, film)
VALUES (:name, :character, :person, :film)
RETURNING *;

-- :name insert-cast-members :! :n
-- :doc Insert multiple cast members with :tuple* parameter type
INSERT INTO web_castmember (name, character, person, film)
VALUES :tuple*:cast;

-- A ":result" value of ":1" specifies a single record (as a hashmap)
--  will be returned
-- :name get-cast-member :? :1
-- :doc Retrieves a cast member by the person and film id
SELECT *
FROM web_castmember
WHERE person = :person AND
      film = :film;

-- :name get-duplicate-cast-members :? :n
-- :doc Finds duplicate cast members
SELECT MAX (id)
FROM web_castmember
GROUP BY person, film, character
HAVING COUNT (*) > 1;

-- :name delete-duplicate-cast-members :? :n
-- :doc Deletes duplicate cast members
DELETE FROM web_castmember
WHERE id IN (
  SELECT MAX (id)
  FROM web_castmember
  GROUP BY person, film, character
  HAVING COUNT (*) > 1);
