(ns datomic-tutorial.core
  (:require [datomic.api :as d]))

(def db-url "datomic:free://127.0.0.1:4334/datomic-tutorial")

(def db-conn (atom (d/connect db-url)))

(def schema
  [{:db/doc "A users email."
    :db/ident :user/email
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}])

(def test-data
  [{:user/email "fenton.travers@gmail.com"}])

(defn reload-dbs []
  (d/delete-database db-url)
  (d/create-database db-url)
  (reset! db-conn (d/connect db-url))
  (d/transact @db-conn schema)
  (d/transact @db-conn test-data))


