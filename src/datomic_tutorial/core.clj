(ns datomic-tutorial.core
  (:require [datomic.api :as d]))

(def db-url "datomic:free://127.0.0.1:4334/datomic-tutorial")

(def schema [{:db/doc "The username."
              :db/id #db/id[:db.part/db]
              :db/ident :user/name
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
              :db.install/_attribute :db.part/db}

             {:db/doc "List of cars a user owns"
              :db/id #db/id[:db.part/db]
              :db/ident :cars
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/many
              :db.install/_attribute :db.part/db}

             {:db/doc "Car make"
              :db/id #db/id[:db.part/db]
              :db/ident :car/make
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
              :db.install/_attribute :db.part/db}

             {:db/doc "Car model"
              :db/id #db/id[:db.part/db]
              :db/ident :car/model
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
              :db.install/_attribute :db.part/db}

             {:db/doc "Year"
              :db/id #db/id[:db.part/db]
              :db/ident :year
              :db/valueType :db.type/long
              :db/cardinality :db.cardinality/one
              :db.install/_attribute :db.part/db}

             {:db/doc "Person age"
              :db/id #db/id[:db.part/db]
              :db/ident :user/age
              :db/valueType :db.type/long
              :db/cardinality :db.cardinality/one
              :db.install/_attribute :db.part/db}])

(def test-data
  [{:db/id #db/id[:db.part/user -1]
    :car/make "toyota"
    :car/model "tacoma"
    :year 2014}

   {:db/id #db/id[:db.part/user -2]
    :car/make "BMW"
    :car/model "325xi"
    :year 2001}

   {:db/id #db/id[:db.part/user -3]
    :user/name "ftravers"
    :user/age 54
    :cars [{:db/id #db/id[:db.part/user -1]}
           {:db/id #db/id[:db.part/user -2]}]}])

(defn reload-dbs []
  (d/delete-database db-url)
  (d/create-database db-url)
  (d/transact (d/connect db-url) schema)
  (d/transact (d/connect db-url)  test-data))

(defn query1 [db]
  (d/q '[:find
         (pull ?e
               [:user/name
                :user/age
                {:cars [:car/make :car/model]}])
         :where
         [?e :user/name "ftravers"]]
       db))

(query1 (-> db-url d/connect d/db))


