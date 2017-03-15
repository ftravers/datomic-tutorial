<div id="table-of-contents">
<h2>Table of Contents</h2>
<div id="text-table-of-contents">
<ul>
<li><a href="#sec-1">1. A Datomic Tutorial</a>
<ul>
<li><a href="#sec-1-1">1.1. Data Conceptual Shape</a></li>
<li><a href="#sec-1-2">1.2. Keywords</a></li>
<li><a href="#sec-1-3">1.3. Basic Schema</a></li>
<li><a href="#sec-1-4">1.4. Testdata</a></li>
<li><a href="#sec-1-5">1.5. Blow away and recreate DB</a></li>
<li><a href="#sec-1-6">1.6. Better Testdata</a></li>
</ul>
</li>
<li><a href="#sec-2">2. Query the database</a>
<ul>
<li><a href="#sec-2-1">2.1. Concept</a></li>
<li><a href="#sec-2-2">2.2. Breaking down the datomic query</a></li>
<li><a href="#sec-2-3">2.3. Pull Syntax</a></li>
</ul>
</li>
<li><a href="#sec-3">3. Parent Child Data</a>
<ul>
<li><a href="#sec-3-1">3.1. Many Refs Schema</a></li>
<li><a href="#sec-3-2">3.2. Testdata</a></li>
<li><a href="#sec-3-3">3.3. Querying Parent Child Data</a></li>
<li><a href="#sec-3-4">3.4. Parent Child Pull Syntax</a></li>
</ul>
</li>
</ul>
</div>
</div>

# A Datomic Tutorial<a id="sec-1" name="sec-1"></a>

## Data Conceptual Shape<a id="sec-1-1" name="sec-1-1"></a>

Datomic allows clojure programmers to work with data in the structures
they are comfortable with, maps and vectors.  The way to think about
datomic is that it is one giant vector of maps.  Each map in this
vector is what they call an *Entity*.  So lets draw this out with an
example: 

    [{:db/id 1
      :car/make "toyota"
      :car/model "tacoma"
      :year 2014}
    
     {:db/id 2
      :car/make "BMW"
      :car/model "325xi"
      :year 2001}
    
     {:db/id 3
      :user/name "ftravers"
      :user/age 54
      :cars [{:db/id 1}
             {:db/id 2}]}]

So this datomic database has 3 entries/entities/maps.  A user,
`ftravers`, who owns 2 cars.  Every map has to have a `:db/id`.  This
is what uniquely identifies that entity to datomic.  Datomic
`:db/id`'s are actually very large integers, so the data above is
actually a bit fake, but I keep it simple to communicate the concept.

As we can see in the above example, the `:cars` field of the user
`ftravers` points (refers/links) to the cars he owns using the
`:db/id` field.

## Keywords<a id="sec-1-2" name="sec-1-2"></a>

The keywords that our maps use are not just hodge-podge, willy-nilly,
used.  Rather we have to specify ahead of time the set of fields that
maps are allowed to use.  This is called creating a schema.  

A schema in regular relational database (RDBMS) means specifying
tables and columns.  In datomic, we ONLY specify 'columns', or the
entire set of allowed keywords.  

Just like in an RDBMS, when we specify a column, we indicate the type
of data that will live in that field.  We might setup a column to be a
foriegn key (reference/link) to the primary key of another table.
Thats how you link rows together.

So lets take a break from concepts and see how to connect to a
database quickly.

    (def db-url "datomic:free://127.0.0.1:4334/omn-dev")
    (def db-conn (atom (d/connect db-url)))

## Basic Schema<a id="sec-1-3" name="sec-1-3"></a>

Now lets define a schema with just one field.

    (def schema [{:db/doc "A users email."
                  :db/id #db/id[:db.part/db]
                  :db/ident :user/email
                  :db/valueType :db.type/string
                  :db/cardinality :db.cardinality/one
                  :db.install/_attribute :db.part/db}])

This field is of type `string`.  The name of the field is:
`:user/email`.  It should hold just one value, `:db.cardinality/one`.
Now we can load this into the database by transacting it like so:

    (d/transact @db-conn schema)

## Testdata<a id="sec-1-4" name="sec-1-4"></a>

Now we can actually start to load up a bit of testdata into the db.

    (def test-data
      [{:db/id #db/id[:db.part/user -1]
        :user/email "fenton.travers@gmail.com"}])

Whenever we add data into datomic we need to create and give the
entity a `:db/id`.

    #db/id[:db.part/user -1]

is the way we do this.  The -1 could be any negative number, and is
like our fake temporary id.  Datomic will, upon inserting this record
(entity/map), create the real permanent datomic id, `:db/id`.

Lets transact this data into the DB:

    (d/transact @db-conn test-data)

## Blow away and recreate DB<a id="sec-1-5" name="sec-1-5"></a>

When experimenting with datomic, I like to blow the database away, so
I know I'm starting with a clean slate each time.

    (d/delete-database db-url)
    (d/create-database db-url)
    (reset! db-conn (d/connect db-url))
    (d/transact @db-conn schema)
    (d/transact @db-conn test-data)

Here I blow it away, recreate a blank DB, recreate the connection,
transact the schema and testdata.

Working code can be found under the 

GIT TAG: basic-schema-insert

## Better Testdata<a id="sec-1-6" name="sec-1-6"></a>

Okay a DB with only one record in it is pretty boring.  Also a db with
only one column (field), that can't be compared, email, is very
boring.  Lets create a DB with two entities (records/maps) in it.
Lets have those entities have both email and age fields.

The schema

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

So we've added another field, age, that is type: `:db.type/long`.  Now
lets add some actual data:

    (def test-data
      [{:db/id #db/id[:db.part/user -1]
        :user/email "sally.jones@gmail.com"
        :user/age 34}
    
       {:db/id #db/id[:db.part/user -2]
        :user/email "franklin.rosevelt@gmail.com"
        :user/age 14}])

GIT TAG: better-testdata

Notice we need to specify a unique number for each entity in our
batch, so franklin's temp `:db/id` is -2, while sally's is -1.

**REMEMBER** to transact this schema and testdata into your cleaned up DB!

# Query the database<a id="sec-2" name="sec-2"></a>

## Concept<a id="sec-2-1" name="sec-2-1"></a>

Now we have seen how to add data to datomic, the interesting part is
the querying of the data.  A query might be: "Give me the users who
are over 21", if you are making an app to see who is legal to drink
in the United States, for example.

In regular RDBMS we compare rows of tables based on the values in a
given column.  A similar SQL query might look like:

    SELECT user-email FROM users WHERE user-age > 21

In datomic we dont have tables, just a giant vector of maps.  So we
dont have a `FROM` clause.  In our case we are inspecting the
`:user/age` field, so ANY entity (map), which has that field will be
included in our query.  This is a very important idea which we will
revist later to re-inforce.

## Breaking down the datomic query<a id="sec-2-2" name="sec-2-2"></a>

A query takes datalog for its first argument and a database to execute
that datalog on as the second argument.  Lets look at some datalog
first:

    [:find ?e
     :where [?e :user/email]]

Datalog is the query language to extract entities from datomic.  We
have two parts to the datalog, the `:find` part and the `:where` part.
The query (`:where`) part selects (narrows down) the records
(entities).  This is truely the querying part.  So this corresponds to
the `WHERE` clause in SQL. The `:find` part, is basically what to show
from the found records.  So this naturally corresponds to the `SELECT`
part of sql.  Lets focus on the `:where` part first.

Where clauses take one or more vector clauses that are of the form:

    [entity-id field-name field-value]

In our example the `?e` basically means we aren't specifying a
specific entity id, so just fill this in with whatever entity ids you
find.  Next we specify an actual field name, `:user/email`.  So this
is like a constant, whereas `?e` is like a variable.  This means
restrict the entities to ones who actually have the field:
`:user/email`.  Finally we dont specify a field-value, so it can be
any value.

This query reads like: "Get us all the entities that have the field:
`:user/email`.  

In datomic speak, they call field-names, **attributes**, and
field-values, simply **values**.  So you will see them refer to query
clauses like so:q

    [entity attribute value]

Now say we wanted just the entities whose email exactly equaled
`sally.jones@gmail.com`, our query would look like:

    [?e :user/email "sally.jones@gmail.com"]

Here is a  complete query, for all entities that have the
`:user/email` field.  Which in our case will be both entities.

    (defn query1 []
      (d/q '[:find ?e
             :where [?e :user/email]]
           (d/db @db-conn)))

GIT TAG: simple-first-query

Now when you run this query, you get a weird beast back:

    datomic-tutorial.core> (query1)
    #{[17592186045418] [17592186045419]}

So this is a set of vectors with one `:db/id` in each vector.  This
isn't the most intuitive or user friendly representation, so lets
improve upon this.

## Pull Syntax<a id="sec-2-3" name="sec-2-3"></a>

Instead of the line:

    :find ?e

we can convert that into pull syntax like so:

    :find (pull ?e [:user/email :user/age])

and our output will now look like:

    datomic-tutorial.core> (query1)
    [[#:user{:email "sally.jones@gmail.com", :age 34}]
     [#:user{:email "franklin.rosevelt@gmail.com", :age 14}]]

Okay, that looks a lot nicer!

Now we still need to modify this query to only return people who are
21 and over.  Franklin, you aren't allowed to drink!

To get this we set our `:where` clauses like so:

    [?e :user/age ?age]
    [(>= ?age 21)]

So this reads: "give me all the entities who have the field
`:user/age` and store the age into the variable `?age`".  The second
clause reads: "run the `>=` function on the variable ?age and the
number 21, and if this returns `true`, keep this entity, otherwise
discard it.

So here is the full new query:

    (defn query1 []
      (d/q '[:find (pull ?e [:user/email :user/age])
             :where
             [?e :user/age ?age]
             [(>= ?age 21)]]
           (d/db @db-conn)))

The where clauses are \*AND\*ed together, so both must be true in order
for the entity to be included in the search results.

And now we get the desired result:

    datomic-tutorial.core> (query1)
    [[#:user{:email "sally.jones@gmail.com", :age 34}]]

GIT TAG: query-pull-filter

# Parent Child Data<a id="sec-3" name="sec-3"></a>

Often we have data that owns other data.  For example our first
example looked like this:

    [{:db/id 1
      :car/make "toyota"
      :car/model "tacoma"
      :year 2014}
    
     {:db/id 2
      :car/make "BMW"
      :car/model "325xi"
      :year 2001}
    
     {:db/id 3
      :user/name "ftravers"
      :user/age 54
      :cars [{:db/id 1}
             {:db/id 2}]}]

So how do we model this?  First we start with the schema.  We'll need
the fields: `:car/make`, `:car/model`, `:year`, `:user/name`, `:user/age`,
and `:cars`. 

`:car/make`, `:car/model`, and `:user/name` are all of type `string` and
cardinality one.  That is dont have a list of makes, models, or names,
they are single valued. For `:year` and `:user/age` we can use
integers.  `:cars` is the new one.  It's cardinality will be type
`many` also the type that it will hold will be of type `reference`,
since they refer to other entities.  Lets look only at the schema for
`:cars`, the others you should be able to piece together from previous
schema examples, or just look at the 

GIT TAG: parent-child-modeling

## Many Refs Schema<a id="sec-3-1" name="sec-3-1"></a>

For `:cars`, the schema will look like:

    {:db/doc "List of cars a user owns"
        :db/id #db/id[:db.part/db]
        :db/ident :cars
        :db/valueType :db.type/ref
        :db/cardinality :db.cardinality/many
        :db.install/_attribute :db.part/db}

You can see the values for `cardinality` and `valueType` that we've
used. 

## Testdata<a id="sec-3-2" name="sec-3-2"></a>

Here is some testdata:

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

GIT TAG: parent-child-modeling

So now we've stuffed some parent child data into the db, lets see how
to get it out in a nice way.

## Querying Parent Child Data<a id="sec-3-3" name="sec-3-3"></a>

So first of all we'll just find the record we care about, a where
clause that looks like:

    [?e :user/name "ftravers"]

is sufficient to find that entity.  Now lets do some magic with the
pull syntax to get the data how we want it.

## Parent Child Pull Syntax<a id="sec-3-4" name="sec-3-4"></a>

We have already learned how to dispay entity fields.

    (pull ?e [:user/name :user/age])

will spit out data like:

    datomic-tutorial.core> (query1)
    [[#:user{:name "ftravers", :age 54}]]

but what we really want is something that looks like:

    datomic-tutorial.core> (query1)
    [[{:user/name "ftravers",
       :user/age 54,
       :cars
       [#:car{:make "toyota", :model "tacoma"}
        #:car{:make "BMW", :model "325xi"}]}]]

To get the above we change the query to look like:

    (pull ?e
          [:user/name
           :user/age
           {:cars [:car/make :car/model]}])

So for the children, you start a new map, whose key is the parent
keyword that points to the child.  Then you start a vector and list
the properties of the child you wish to grab.
