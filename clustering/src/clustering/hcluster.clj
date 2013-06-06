(ns clustering.hcluster
  (:require [clojure.math.combinatorics :as combo] 
            [clustering.db.db :as db]
            [clojure.data.json :as json]
            [clustering.util.clustering-util :as utilc]))


(defn best-pairing [cls]
  (last (sort (fn [[cl11 cl12] [cl21 cl22]]
                (compare (utilc/euclidean (:nVec cl11) (:nVec cl12))
                         (utilc/euclidean(:nVec cl21) (:nVec cl22))))
              (combo/combinations cls 2))))


(defn get-children [n]
  (if-let [ch (:children n)]
    ch
    [n]))

(defn hcluster [cls]
  (if (< (count cls) 5)
    cls
    (let [closest-pair (best-pairing cls)
          [l r] closest-pair
          new-cls (remove #(some (set [%]) closest-pair) cls)]
      (hcluster 
        (conj new-cls
              {:children  (into [] 
                                (concat [] (get-children l) (get-children r)))
               :nVec (utilc/average-of-vectors [(:nVec l) (:nVec r)])})))))


(defn -main [& args]
  (db/db-init) 
  (spit "resources/public/data/clusters.json" (json/write-str 
                                            {"children" (into [] 
                                                               (hcluster (utilc/prepare-data (db/find-by-meal "breakfast"))))
                                             "name" "Popular breakfast recipes"})))
