(ns om-datomic.core-test
  (:require [expectations :refer :all]
            [om-datomic.core :refer :all]
            [datomic.api :as d]))

(defn create-empty-in-memory-db []
  (let [uri "datomic:mem://pet-owners-test-db"]
    (d/delete-database uri)
    (d/create-database uri)
    (let [conn (d/connect uri)
          schema (load-file "resources/data/schema.edn")]
      (d/transact conn schema)
      conn)))

(defn test-connection []
    (let [uri "datomic:free://192.168.99.100:4334/om_test"]
    (d/delete-database uri)
    (d/create-database uri)
    (let [conn (d/connect uri)
          schema (load-file "resources/data/schema.edn")]
      (d/transact conn schema)
      conn)))


;;adding a class should return something



(expect #{["Welcome to the ungle"]}
        (let [c (test-connection)]
        (dosync
          (create-class "Welcome to the ungle" 6001)
          (get-class-titles)
         )
          c))
