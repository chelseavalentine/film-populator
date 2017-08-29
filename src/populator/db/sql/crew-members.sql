-- A :result value of :n below will return affected row count:
-- :name insert-crew-member :? :n
-- :doc Inserts a crew member and returns its id
INSERT INTO web_crewmember (name, job, department, person, film)
VALUES (:name, :job, :department, :person, :film)
RETURNING *;

-- A ":result" value of ":1" specifies a single record (as a hashmap)
--  will be returned
-- :name get-crew-member :? :1
-- :doc Retrieves a crew member by the person and film id
SELECT *
FROM web_crewmember
WHERE person = :person AND
      film = :film AND
      department = :department AND
      job = :job;

-- :name get-duplicate-crew-members :? :n
-- :doc Finds duplicate crew members
SELECT MAX (id)
FROM web_crewmember
GROUP BY person, film, department, job
HAVING COUNT (*) > 1;

-- :name delete-duplicate-crew-members :? :n
-- :doc Deletes duplicate crew members
DELETE FROM web_crewmember
WHERE id IN (
  SELECT MAX (id)
  FROM web_crewmember
  GROUP BY person, film, department, job
  HAVING COUNT (*) > 1);
