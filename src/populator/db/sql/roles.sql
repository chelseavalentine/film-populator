-- A :result value of :n below will return affected row count:
-- :name insert-role :? :n
-- :doc Inserts a role and returns the id of the newly-created role
INSERT INTO web_role (is_cast, department, job, character, work)
VALUES (:is_cast, :department, :job, :character, :work)
RETURNING id;

-- A ":result" value of ":1" specifies a single record (as a hashmap)
--  will be returned
-- :name get-role :? :1
-- :doc Retrieves a role by matching role object
SELECT *
FROM web_role
WHERE work = :work AND
      is_cast = :is_cast;

-- NOTE: This is untested
-- :name get-duplicate-roles :? :n
-- :doc Finds duplicate roles
SELECT MAX (id)
FROM web_role
GROUP BY work, character, department, job, is_cast
HAVING COUNT (*) > 1;

-- NOTE: This is untested
-- :name delete-duplicate-roles :? :n
-- :doc Deletes duplicate roles
DELETE FROM web_role
WHERE id IN (
  SELECT MAX (id)
  FROM web_role
  GROUP BY work, character, department, job, is_cast
  HAVING COUNT (*) > 1);
