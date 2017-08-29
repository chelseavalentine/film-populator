(ns populator.db
  (:require [environ.core :refer [env]]
            [slingshot.slingshot :as slingshot]
            [clojure.java.jdbc :as jdbc]
            [populator.utils.general :refer :all])
  (:import [org.postgresql.jdbc PgArray]))

(def dbconn
  {:classname   "org.postgresql.Driver"
   :subprotocol "postgresql"
   :subname     (str "//" (env :postgres-host) ":" (env :postgres-port) "/" (env :postgres-name))
   :user        (env :postgres-name)
   :password    (env :postgres-password)
   :sslmode     "require"})

(defn exec
  "Executes a database accessing function with retries."
  [f]
  (when (fn? f)
    (slingshot/try+
      (f)
      (catch Exception e
        (let [retry-time (* 100 (+ 20 (rand-int 280)))]
          (println (str "Trying to access the database again in " (double (/ retry-time 1000)) "s"))
          (println e)
          (println (.getNextException e))
          (Thread/sleep retry-time)
          (exec #(f)))))))

(defn create-string-array
  "Creates a PostgresSQL-compliant string array."
  [values]
  (if-not (= PgArray (type values))
    (when-let [strings (not-empty (map extract-string (extract-coll values)))]
      (when-not (some nil? strings)
        (exec
          (fn []
            (let [cn            (exec #(jdbc/get-connection dbconn))
                  strings-array (.createArrayOf cn "varchar" (into-array String strings))]
              (.close cn)
              strings-array)))))
    values))

(defn create-int-array
  "Creates a PostgresSQL-compliant int array."
  [values]
  (if-not (= PgArray (type values))
    (when-let [ints (not-empty (map extract-number (extract-coll values)))]
      (when-not (some nil? ints)
        (exec
          (fn []
            (let [cn         (exec #(jdbc/get-connection dbconn))
                  ints-array (.createArrayOf cn "int" (into-array Integer ints))]
              (.close cn)
              ints-array)))))
    values))

(defn validate-data
  "Returns whether the data meets the criteria specified."
  [data criteria]
  (when (and (map? data) (coll? criteria))
    (if (not-empty criteria)
      (let [test-results (map #(not (not (% data))) criteria)
            has-failures (true? (some false? test-results))]
        (not has-failures))
      true)))
