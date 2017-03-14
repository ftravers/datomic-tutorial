(defproject datomic-tutorial "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [com.datomic/datomic-free "0.9.5344" :exclusions [joda-time org.slf4j/slf4j-nop]]]
  :main ^:skip-aot datomic-tutorial.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
