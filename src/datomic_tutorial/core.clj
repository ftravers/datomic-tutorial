(ns datomic-tutorial.core
  (:require [datomic.api :as d]))

(def db-conn (atom nil))

(def db-url "datomic:free://127.0.0.1:4334/datomic-tutorial")

(def schema [{:db/doc "A users email."
              :db/id #db/id[:db.part/db]
              :db/ident :user/email
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
              :db.install/_attribute :db.part/db}

             {:db/doc "A users age."
              :db/id #db/id[:db.part/db]
              :db/ident :user/age
              :db/valueType :db.type/long
              :db/cardinality :db.cardinality/one
              :db.install/_attribute :db.part/db}])

(def test-data
  [{:db/id #db/id[:db.part/user -1]
    :user/email "sally.jones@gmail.com"
    :user/age 34}

   {:db/id #db/id[:db.part/user -2]
    :user/email "franklin.rosevelt@gmail.com"
    :user/age 14}])

(defn reload-dbs []
  (d/delete-database db-url)
  (d/create-database db-url)
  (reset! db-conn (d/connect db-url))
  (d/transact @db-conn schema)
  (d/transact @db-conn test-data))

