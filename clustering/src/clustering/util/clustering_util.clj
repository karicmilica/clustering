(ns clustering.util.clustering-util
  (:require [clojure.math.combinatorics :as combo] 
            [clojure.data.json :as json]))


;normalization

(defn normalize [value min max]
  (/ (- value min) (- max min)))

(defn find-min [l]
  (apply min l))

(defn find-max [l]
  (apply max l))

(defn find-nutrition-min-and-max [attr l]
  (let [l1 (map #(attr (:nutrition %))l)]
    [(find-min l1) (find-max l1)]))

(defn sum [v]
  (reduce + v))

(defn sort-map [m]
  (into {} (sort (fn [x1 x2] 
                   (compare (sum  (first x2))
                            (sum  (first x1))))
                 m)))

(defn prepare-data [l]
  (let [sodium (find-nutrition-min-and-max :sodiumContent l)
        calories (find-nutrition-min-and-max :calories l)
        saturatedFat (find-nutrition-min-and-max :saturatedFatContent l)
        cholesterol (find-nutrition-min-and-max :cholesterolContent l)
        ;fiber (findNutritionMinAndMax :fiberContent l)
        sugar (find-nutrition-min-and-max :sugarContent l)
        ]
    (map (fn [{:keys [nutrition title _id] :as r} ] 
           {:name title
            :size 5000
            :data r
            :id _id
            :nVec [
                   ;(normalize (:calories nutrition) (first calories) (second calories))
                   ;(normalize (:fiberContent nutrition) (first fiber) (second fiber))
                   ;(normalize (:sodiumContent nutrition) (first sodium) (second sodium))
                   (normalize (:saturatedFatContent nutrition) (first saturatedFat) (second saturatedFat))
                   (normalize (:cholesterolContent nutrition) (first cholesterol) (second cholesterol))
                   (normalize (:sugarContent nutrition) (first sugar) (second sugar))
                  ]}) l)))

(defn euclidean [v1 v2]
  (/ 1 (+ 1 (Math/sqrt (reduce + (map #(Math/pow (- %1 %2) 2) v1 v2))))))

(defn pearson [v1 v2]
      (if (not= (count v1) (count v2)) 0
        (let [sum1  (sum v1)
            sum2  (sum v2)
            sum1-sq  (reduce (fn[s n] (+ s (Math/pow n 2))) 0 v1)
            sum2-sq  (reduce (fn[s n] (+ s (Math/pow n 2))) 0 v2)
            psum (reduce + (map #(* %1 %2) v1 v2))
            num (- psum (/ (* sum1 sum2) (count v1)))
            den (Math/sqrt (* 
                            (- sum1-sq (/ (Math/pow sum1 2) (count v1)))
                            (- sum2-sq (/ (Math/pow sum2 2) (count v1)))))]
        (if (= den 0)
          0
          (double (/ num den))))))

(defn cosine [v1 v2]
  (let [dotp (reduce + (map #(* %1 %2) v1 v2))
        sqrt1 (Math/sqrt (reduce + (map #(* % %) v1))) 
        sqrt2 (Math/sqrt (reduce + (map #(* % %) v2)))]
    (/ dotp (* sqrt1 sqrt2))))

(defn average-of-vectors [data]
  (let [num (count data)]
    (apply map (fn [& items]
                 (/ (apply + items) num))
           data)))

(defn average [s]
  (/ (reduce + s) (count s)))







