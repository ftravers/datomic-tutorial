<div id="table-of-contents">
<h2>Table of Contents</h2>
<div id="text-table-of-contents">
<ul>
<li><a href="#sec-1">1. A Datomic Tutorial</a>
<ul>
<li><a href="#sec-1-1">1.1. Data Conceptual Shape</a></li>
<li><a href="#sec-1-2">1.2. Map Fields</a></li>
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
<li><a href="#sec-2-3">2.3. Where</a></li>
<li><a href="#sec-2-4">2.4. Find</a></li>
<li><a href="#sec-2-5">2.5. Pull Syntax</a></li>
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
<li><a href="#sec-4">4. Deeper Understanding</a>
<ul>
<li><a href="#sec-4-1">4.1. Fields cross SQL Table boundaries</a></li>
</ul>
</li>
</ul>
</div>
</div>

# A Datomic Tutorial<a id="sec-1" name="sec-1"></a>

## Data Conceptual Shape<a id="sec-1-1" name="sec-1-1"></a>

The data in datomic is conceptually one big vector of maps.  Re-read
that sentence and cement it inside your head.  Conceiving of datomic
like this is fundamental to understanding how to operate with it.

Each map in this vector is one *entity*, in the datomic vocabulary.
This corresponds to a row in a Relational Database Management System
(RDBMS).  Below is a sample database of data:

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

So this datomic database has 3 entries (entities/maps/rows).  A user,
`ftravers`, who owns 2 cars.  Every map (entity) must have a `:db/id`.
This is what uniquely identifies that entity to datomic.  Datomic
`:db/id`'s are actually very large integers, so the data above is
actually a bit fake, but I keep it simple to communicate the concept.

As we can see in the above example, the `:cars` field of the user
`ftravers` points (refers/links) to the cars he owns using the
`:db/id` field.  The `:db/id` field allows one entity to refer to
another entity (or many other entities).

## Map Fields<a id="sec-1-2" name="sec-1-2"></a>

So datomic is a vector of maps.  A map has fields, or keys, and in
clojure keywords are used.  The three maps, in our example we had the
fields/keys:

    :db/id
    :car/make
    :car/model
    :year
    :user/name
    :user/age
    :cars

Datomic doesn't allow you to just go ahead and pick any old keyword as
a field (or key) to entity maps.  Rather we have to specify ahead of
time which keywords entities in datomic can use.  This is called
creating a schema.

In the SQL world, creating a schema means defining table names, column
names and its data type.

In datomic, we do away with the concept of a table.  You could say
datomic ONLY specifies 'columns'.  Here a column in SQL is equivalent
to a field in datomic.  When we specify a column in SQL we give it a
name, and we indicate what it will hold, a string, integer, etc&#x2026;  In
datomic we do a similar thing, we define fields stating what their
name is and what type of data they hold.  Datomic calls fields
**attributes**.

So this is a bit of a big deal.  In RDBMS our columns are stuck
together in a table.  In datomic we define a bunch of fields that don't
necessarily have anything to do with one another.  We can randomly use
any field we define for any record we want to store.  Remember datomic
is just a bunch of maps floating around.  We are only allowed to pick
fields that have been predefined, but other than that, we can create
maps with any combinations of those fields.

Okay enough concepts, lets see how to define a field.

## Basic Schema<a id="sec-1-3" name="sec-1-3"></a>

Here we create a field in datomic.  We'll start with creating just one
field.  This field will *hold* an email value.

    (def schema [{:db/doc "A users email."
                  :db/id #db/id[:db.part/db]
                  :db/ident :user/email
                  :db/valueType :db.type/string
                  :db/cardinality :db.cardinality/one
                  :db.install/_attribute :db.part/db}])

`:db/ident` is the name of the field.  So when we want to use this
field to store data, this is the keyword you use.

`:db/valueType` is the type of data that this field will hold.  Here
we use the `string` datatype to store an email string.

`:db/cardinality` can be either `one` or `many`.  Basically should
this field hold a single item or a list of items.

Those are the important fields to understand conceptually. `:db/doc`
is a documentation string, remember everything in datomic needs it's
own `:db/id`, and `:db.install/_attribute` instructs datomic to treat
this data as schema field creation data.

Before we can start adding schema to a database, we need to create the
database!

    (def db-url "datomic:free://127.0.0.1:4334/omn-dev")
    (d/create-database db-url)
    (def db-conn (atom (d/connect db-url)))

Now we can load this schema definition into the database by
transacting it like so:

    (d/transact @db-conn schema)

## Testdata<a id="sec-1-4" name="sec-1-4"></a>

Now that we've defined a field, lets make use of it by
creating/inserting an entity that makes use of the newly created
field.  Remember data inside datomic is just a map, so lets just
create that map:

    (def test-data
      [{:db/id #db/id[:db.part/user -1]
        :user/email "fenton.travers@gmail.com"}])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

So the `:user/email` part is understandable, but whats that other
field `:db/id` all about?  Remember whenever we add data into datomic
we need to create and give the entity a `:db/id`.  The part that looks
like: 

    #db/id[:db.part/user -1]

is basically asking datomic to replace this with a valid `:db/id`.
The -1 could be any negative number, and is like our fake temporary
id.  Datomic will, upon inserting this record (entity/map), create the
real permanent datomic id, `:db/id`.

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

Okay a DB with only one record (row/entity/map) in it is pretty
boring.  Also a db with only one string column (field) is next to
useless!  Lets create a DB with two entities (records/maps) in it.
Also lets create a second field, age, so we can query the database for
people 21 and older!

The schema:

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

**REMEMBER** to transact this schema and testdata into your cleaned up
DB!  Otherwise you'll get an error for trying to add the `:user/email`
field twice.

# Query the database<a id="sec-2" name="sec-2"></a>

## Concept<a id="sec-2-1" name="sec-2-1"></a>

Now we have seen how to add data to datomic, the interesting part is
the querying of the data.  A query might be: "Give me the users who
are over 21", if you are making an app to see who is legal to drink
in the United States, for example.

In regular RDBMS we compare rows of tables based on the values in a
given column.  A similar SQL query might look like:

    SELECT user-email FROM users WHERE user-age > 21

In datomic we don't have tables, just a bunch of maps.  So we don't have
a `FROM` clause.  In our case we are inspecting the `:user/age` field,
so ANY entity (map), which has that field will be included in our
query.  This is a very important idea which we will revisit later to
re-inforce.

This is a critical concept.  When two maps use the same field, that's
what sort of links them together.  We will write our queries based on
these fields, so when we query on a field that a map uses, it gets
swept up in that query.

## Breaking down the datomic query<a id="sec-2-2" name="sec-2-2"></a>

A query takes *datalog* for its first argument and a *database* to
execute that datalog on as the second argument.  Lets just look at
the datalog first:

    [:find ?e
     :where [?e :user/email]]

Datalog is the query language to extract entities from datomic.  We
have two parts to the datalog, the `:find` part and the `:where` part.

## Where<a id="sec-2-3" name="sec-2-3"></a>

The query (`:where`) part selects (narrows down) the records
(entities).  This is truly the querying part.  So this corresponds to
the `WHERE` clause in SQL. 

The `:find` part, is basically what to show from the found records.
So this naturally corresponds to the `SELECT` part of SQL.  Lets focus
on the `:where` part first.

Where clauses take one or more vector clauses that are of the form:

    [entity field-name field-value]

Working backwards in our example `[?e :user/email]`, doesn't specify a
field-value, so this means the field-value can be anything.

Next we say we want maps that use the field (attributed):
`:user/email`.

Finally, the `?e`, means each entity (maps) we find, store it in the
variable `?e`, because we are going to use it in another part of our
datalog.

In summary this query reads like: "Get us all the entities in the DB
that have the field: `:user/email`.

## Find<a id="sec-2-4" name="sec-2-4"></a>

Finally we have the `:find` part of the datalog.  The correlates
directly to the `SELECT` aspect of SQL, and it basically indicates
what parts of the found records to return.

We just say: `:find ?e`, so we'll get the entity id (`:db/id`),
returned.  We can convert an entity id, which is just an integer, into
a clojure map. 

Here is the full query, 

    (defn query1 []
      (d/q '[:find ?e
             :where
             [?e :user/email]]
           (d/db @db-conn)))

and the result of running it:

    datomic-tutorial.core> (query1)
    #{[17592186045418] [17592186045419]}

GIT TAG: simple-first-query

Hmmm&#x2026;  Okay this is kind of far from what we put in:

    (def test-data
      [{:db/id #db/id[:db.part/user -1]
        :user/email "sally.jones@gmail.com"
        :user/age 34}
    
       {:db/id #db/id[:db.part/user -2]
        :user/email "franklin.rosevelt@gmail.com"
        :user/age 14}])

Those numbers are the entity id's (`:db/id`) of the two records (maps)
we transacted into the database.

We are going to convert these entity ids into familiar clojure maps
using two approaches.  The first approach is a bit more instinctive,
and the second approach is more enlightened.

Instinctively, I'd look for an API to convert a `:db/id` into the
actual entity that the id represents.  So datomic has a function:
`entity`, which is documented like so:

"Returns a dynamic map of the entity's attributes for the given id"

Okay that looks promising.  A bit more research on google reveals the
following works:

    datomic-tutorial.core> (map #(seq (d/entity (d/db @db-conn) (first %))) (query1))
    (([:user/email "sally.jones@gmail.com"] [:user/age 34])
     ([:user/email "franklin.rosevelt@gmail.com"] [:user/age 14]))

Okay, that is the instinctual approach to extract the data we are
looking for, now let me introduce a more enlightened approach, **pull**! 

## Pull Syntax<a id="sec-2-5" name="sec-2-5"></a>

Instead of the line:

    :find ?e

we can convert that into pull syntax like so:

    :find (pull ?e [:user/email :user/age])

and our output will now look like:

    datomic-tutorial.core> (query1)
    [[#:user{:email "sally.jones@gmail.com", :age 34}]
     [#:user{:email "franklin.rosevelt@gmail.com", :age 14}]]

Okay, that looks a lot nicer!

The way to understand pull syntax is that the first argument is the
entity that you want to apply the pull syntax to.  As a reminder lets
put that here again so its fresh in your mind:

    (def test-data
      [{:db/id #db/id[:db.part/user -1]
        :user/email "sally.jones@gmail.com"
        :user/age 34}
    
       {:db/id #db/id[:db.part/user -2]
        :user/email "franklin.rosevelt@gmail.com"
        :user/age 14}])

Now the second argument to the pull function is the pull pattern,
again: `[:user/email :user/age]`.  Here we declare the fields we want
returned to us.  Once again the result of the pull syntax:

    datomic-tutorial.core> (query1)
    [[#:user{:email "sally.jones@gmail.com", :age 34}]
     [#:user{:email "franklin.rosevelt@gmail.com", :age 14}]]

Much more user friendly!  Okay, now lets make a query that is more
interesting that just "get all entities who have the `:user/email`
field!

Lets modify this query to only return people who are 21 and over.
Franklin, you aren't allowed to drink!

To achieve this we use the following TWO where clauses:

    :where
    [?e :user/age ?age]
    [(>= ?age 21)]

The first thing to note about this query is that it contains two
clauses.  Where clauses are implicitly AND-ed together.  So both
criteria need to be true.

Lets breakdown the first part of the query: 

    [?e :user/age ?age]

Remember where clauses are in the format: [entity field-name
field-value].  As an aside, documentation about datomic refers to this
as: [entity attribute value].

So this where clause reads like: "Find all entities that have the
field (attribute) `:user/age`, and stick the entity into the variable
`?e` and stick the value of the attribute, the actual users age, into
the variable `?age`.

So for each entity that meets this criteria will have the entity
stored in the `?e` variable, and the age in the `?age` variable.  Now
we can make use of the age value in the second where clause:

    [(>= ?age 21)]

Okay this is a special, and super cool variant on normal where
clauses.  We can run **ANY** function here that returns a boolean
result.  Well we know the function `>=` is a boolean value returning
function, so it's legit.  Second, for each entity, the users age will
be stored in the variable `?age`, so we can simply pass that into the
function to get our bool result!  This just says, we want entities who
have an age >= 21.  Simple!

So here is the full new query:

    (defn query1 []
      (d/q '[:find (pull ?e [:user/email :user/age])
             :where
             [?e :user/age ?age]
             [(>= ?age 21)]]
           (d/db @db-conn)))

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

`:car/make`, `:car/model`, and `:user/name` are all of type `string`
and cardinality one.  For `:year` and `:user/age` we can use integers.
`:cars` is the new one.  

The field `:cars` has a cardinality of `many` also the type that it
will hold is of type reference, since we only want to refer to the
cars already defined in the DB.

Lets look only at the schema for `:cars`, the others you should be
able to piece together from previous schema examples, or just look at
the:

GIT TAG: parent-child-modeling

## Many Refs Schema<a id="sec-3-1" name="sec-3-1"></a>

For `:cars`, the schema will look like:

    {:db/doc "List of cars a user owns"
        :db/id #db/id[:db.part/db]
        :db/ident :cars
        :db/valueType :db.type/ref
        :db/cardinality :db.cardinality/many
        :db.install/_attribute :db.part/db}

Take special not of the values for `cardinality` and `valueType` that
we've used.  

A `valueType` of `ref` means we want this field to hold references to
other entities in the DB.  This is the critical difference between a
database and regular old clojure data structures that don't really
support references.

The second thing to note is the `cardinality` is `many`.  That means
this field will hold a list of values, not just a single value.

## Testdata<a id="sec-3-2" name="sec-3-2"></a>

Now lets make some testdata we can transaction into the DB:

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

So now we've stuffed some parent/child data into the DB, lets see how
to get it out in a nice way.

## Querying Parent Child Data<a id="sec-3-3" name="sec-3-3"></a>

First we'll find the record we care about with a where clause that
looks like:

    [?e :user/name "ftravers"]

This reads: "find all the entities that have the `:user/name`
attribute (field) who's value is: ~"ftravers"~.  Now lets do some magic
with the pull syntax to get the data out how we want it.

## Parent Child Pull Syntax<a id="sec-3-4" name="sec-3-4"></a>

We have already learned how to extract entity fields with a basic pull
pattern:

    (pull ?e [:user/name :user/age])

retrieves the `:user/name` and `:user/age` fields from the found
entity/entities.  Again the result looks like this:

    datomic-tutorial.core> (query1)
    [[#:user{:name "ftravers", :age 54}]]

but what we really want is something that looks like:

    datomic-tutorial.core> (query1)
    [[{:user/name "ftravers",
       :user/age 54,
       :cars
       [#:car{:make "toyota", :model "tacoma"}
        #:car{:make "BMW", :model "325xi"}]}]]

So we want more than just the simple fields that an entity has, but we
want to follow any references it has to other entities and get values
from those entities.

To get the above we change the query to look like:

    (pull ?e
          [:user/name
           :user/age
           {:cars [:car/make :car/model]}])

So to get the children, and print out their fields, you start a new
map, whose key is the parent field that points to the child.  In our
case `:cars`.  Then you start a vector and list the properties of the
child you wish to grab.

This is an extremely elegant way to extract arbitrary levels of data
from datomic.  Just imagine the mess this would look like with SQL.
Maybe here is a stab just for comparison.

    SELECT users.id users.name, users.age, cars.make, cars.model, cars.year
    FROM users cars
    WHERE users.id == cars.userid AND users.name == "ftravers"

And this would produce a result like:

    [[1 ftravers 54 "toyota" "tacoma" 2013]
     [1 ftravers 54 "BMW" "325xi" 2001]]

for comparison equivalent datalog:

    '[:find
      (pull ?e
            [:user/name
             :user/age
             {:cars [:car/make :car/model]}])
      :where
      [?e :user/name "ftravers"]]

and its result:

    [[{:user/name "ftravers",
       :user/age 54,
       :cars
       [#:car{:make "toyota", :model "tacoma"}
        #:car{:make "BMW", :model "325xi"}]}]]

# Deeper Understanding<a id="sec-4" name="sec-4"></a>

## Fields cross SQL Table boundaries<a id="sec-4-1" name="sec-4-1"></a>

So pretend we have two entities like:

    {:user/name "ftravers"
    :year 1945}
    
    {:car/make "BMW 325xi"
    :year 2001}

In datomic we can compare these two seemingly quite different objects
with each other because they share a field: `:year`.  So I could write
a query that returns **ALL THINGS** that are older than 35 years old.
As I write this, it is 2017, so a 35 year old thing would be born
(made) in approximately the year: 1982.  So the where clause would
look like:

    [?e :year ?year]
    [(<= ?year 1982)]

In RDBMS you normally are only ever comparing things that exist in the
same table.  So it'd be awkward to try a similar thing in an RDBMS.
Primarily because they wouldn't have a combined index for fields in
two separate tables.  So your performance would die.  In datomic each
field has it's own index, so a query like the above, would still be
performant.
