(ns clustering.kmeans
  (:require [clojure.math.combinatorics :as combo] 
            [clustering.db.db :as db]
            [clojure.data.json :as json]
            [clustering.util.clustering-util :as utilc]))


;k means ++
(defn cumulative-sum [l]
  (reduce 
    (fn [v, x] (conj v (+ (last v) x))) 
    [(first l)] 
    (rest l)))

(defn deduct-vectors [v1 v2]
  (map #(- %1 %2) v1 v2))

(defn square-inner-product [v1 v2]
  (Math/pow (reduce + (map #(Math/pow % 2) (deduct-vectors v1 v2))) 2))


(defn min-square-inner-product [c x0]
  (apply min (map #(square-inner-product % x0) c)))

(defn init-next [x c]
  (let [d2 (map (partial min-square-inner-product c) x)
        sum-d2 (reduce + d2)
        probs (map #(/ % sum-d2) d2)
        cumprobs (cumulative-sum probs)
        r (rand)
        i (first (first (remove #(< (second %) r) (map-indexed vector cumprobs))))
        next (nth x i)]
    (conj c next)))

(defn initialize [x k]
  (nth (iterate (partial init-next x) [(first x)]) (- k 1)))


;k-means

(defn sort-map [m]
  (into {} (sort (fn [x1 x2] 
                   (compare (utilc/sum  (first x2))
                            (utilc/sum  (first x1))))
                 m)))


(defn closest [centriod-keys value]
  (last (sort (fn [x1 x2] 
                (compare (utilc/euclidean x1 (:nVec value))
                         (utilc/euclidean x2 (:nVec value))))
              centriod-keys)))

(defn next-centroids [values centroids]
  (let [clusters (group-by (partial closest (keys centroids)) values)]
    (reduce (fn [x1 x2] (assoc x1 (into [] (utilc/average-of-vectors (map #(:nVec %) (second x2)))) 
                               (second x2))) {} clusters)))


(defn k-means 
  ([data init]
    (if (coll? init)
      (loop [centroids init] 
        (let [next (sort-map (next-centroids data centroids))]
          (if (= centroids next)
            centroids
            (recur next)))) 
      (let [initial-centroids (apply assoc {} 
                                 (interleave 
                                   (initialize (map #(:nVec %) data) init) (repeat init [])))]
        ;(println initial-centroids)
        (k-means data initial-centroids)))))

;(defn closest [centriod-keys value]
  ;(last (sort (fn [x1 x2] 
                ;(compare (utilc/cosine x1 (:nVec value))
                         ;(utilc/cosine x2 (:nVec value))))
              ;centriod-keys)))

(defn -main [& args]
  (db/db-init) 
  (spit "resources/public/data/clusters.json" (json/write-str 
                   {"name" "Popular breakfast recipes"
                    "children" (into [] (map (fn [x] {:name (first x) :children (second x)})
                                          (k-means (utilc/prepare-data (db/find-by-meal "breakfast"))
                                                   {[0.15772870662460567 0.2224224224224224 0.14372163388804843] [],
                                                    [0.12302839116719243 0.21921921921921922 0.4538577912254161] [], 
                                                    [0.4889589905362776 0.8708708708708709 0.025718608169440244] [], 
                                                    [0.034700315457413256 0.04704704704704705 0.02118003025718608] []})))})))
