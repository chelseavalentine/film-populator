(ns populator.db.credits
  (:require [populator.db.sql-adapter :as sql-fns]
            [populator.db :as db :refer [dbconn]]
            [populator.utils.general :refer :all]))

(defn validate-*-member-data
  ;; TODO: Create a test for this
  "Returns true or nil depending on whether the cast/crew
  member data is valid for database insertion."
  [member-data]
  (when [member-data (extract-map member-data)]
    (let [criteria [#(extract-number % :film)
                    #(extract-number % :person)]]
      (db/validate-data member-data criteria))))

(defn find-cast-member
  "Finds a cast member using the film ID and person's TMDB ID."
  [film-id person-tmdb-id role-data]
  (let [film-id        (extract-number film-id)
        person-tmdb-id (extract-number person-tmdb-id)
        role-data      (extract-map role-data)]
    (when (and film-id person-tmdb-id role-data)
      (db/exec
        #(sql-fns/get-cast-member dbconn (assoc role-data :film film-id
                                                          :person person-tmdb-id))))))

(defn clean-character
  "Ensures that the cast member's character is short enough for the database."
  [cast-member-data]
  (if-let [character (extract-string cast-member-data :character)]
    (if (> (count character) 255)
      (let [character-with-removed-parentheses (.replaceAll character "\\(.*?\\)" "")]
        (if (> (count character-with-removed-parentheses) 255)
          (let [truncated-character (str (.substring character 0 252) "...")]
            (assoc cast-member-data :character truncated-character))
          (assoc cast-member-data :character character-with-removed-parentheses)))
      cast-member-data)
    cast-member-data))

(defn create-cast-member
  "Creates a cast member record."
  [cast-member-data]
  (when-let [cast-member-data (extract-map cast-member-data)]
    (let [cleaned-cast-member-data (clean-character cast-member-data)]
      (when (validate-*-member-data cleaned-cast-member-data)
        (db/exec #(sql-fns/insert-cast-member dbconn cleaned-cast-member-data))))))

(defn create-cast
  "Creates cast member records in bulk."
  [cast-members-data]
  (when-let [cast-members-data (filter validate-*-member-data (extract-coll cast-members-data))]
    (let [cleaned-cast-data  (doall (map clean-character cast-members-data))
          ordering           [:name :character :person :film]
          cast-member-tuples (doall (map (fn [member-data]
                                           (db/exec #(sql-fns/convert-to-ordered-vector ordering member-data))) cleaned-cast-data))]
      (when cast-member-tuples
        (db/exec #(sql-fns/insert-cast-members dbconn {:cast cast-member-tuples}))))))


(defn find-crew-member
  "Finds a crew member using the film ID and the person's TMDB ID."
  [film-id person-tmdb-id role-data]
  (let [film-id        (extract-number film-id)
        person-tmdb-id (extract-number person-tmdb-id)
        role-data      (extract-map role-data)]
    (when (and film-id person-tmdb-id role-data)
      (db/exec
        #(sql-fns/get-crew-member dbconn (assoc role-data :film film-id
                                                          :person person-tmdb-id))))))

(defn create-crew-member
  "Creates a crew member record. Must include person TMDB ID and film ID."
  [crew-member-data]
  (when-let [crew-member-data (extract-map crew-member-data)]
    (when (validate-*-member-data crew-member-data)
      (db/exec #(sql-fns/insert-crew-member dbconn crew-member-data)))))
