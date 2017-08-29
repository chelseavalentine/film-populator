(defproject populator "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha15"]
                 [org.clojure/core.async "0.3.442"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/java.jdbc "0.7.0-alpha1"]
                 [environ "1.1.0"]
                 [slingshot "0.12.2"]
                 [clj-http "2.3.0"]
                 [org.postgresql/postgresql "9.4.1207"]
                 [com.layerware/hugsql "0.4.7"]
                 [clj-time "0.13.0"]]
  :main populator.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
