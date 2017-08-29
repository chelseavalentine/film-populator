-- A :result value of :n below will return affected row count:
-- :name insert-image :? :n
-- :doc Inserts an image and returns the new image's id
INSERT INTO web_image
(aspect_ratio, path, height, width)
VALUES
  (:aspect_ratio, :path, :height, :width)
RETURNING id;

-- :name insert-images :! :n
-- :doc Insert multiple images with :tuple* parameter type and returns their IDs
INSERT INTO web_image
(aspect_ratio, path, height, width)
VALUES :tuple*:images;

-- A ":result" value of ":1" specifies a single record (as a hashmap)
--  will be returned
-- :name get-image :? :1
-- :doc Retrieves an image by path (case-insensitive)
SELECT *
FROM web_image
WHERE upper (path) = upper (:path);

-- :name get-duplicate-images :? :n
-- :doc Finds duplicate images
SELECT
  path,
  COUNT (*)
FROM web_image
GROUP BY path
HAVING COUNT (*) > 1;

-- :name delete-duplicate-images :? :n
-- :doc Deletes duplicate images
DELETE FROM web_image
WHERE id IN (
  SELECT MAX (id)
  FROM web_image
  GROUP BY path
  HAVING COUNT (*) > 1);
