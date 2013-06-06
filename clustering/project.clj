(defproject clustering "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/data.json "0.2.1"]
                 [com.novemberain/monger "1.4.2"]
                 [clj-enlive-template "0.0.1"]
                 [noir "1.3.0"]
                 [enlive "1.1.1"]
                 [org.clojure/math.combinatorics "0.0.4"]]
  ;:main clustering.scraper.recipe_extractor
  ;:main clustering.kmeans
  :main clustering.server.server
  ;:main clustering.hcluster
  )
