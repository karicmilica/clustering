(ns clustering.scraper.util-extraction
  (:require [clojure.data.json :as json]
            [net.cgrand.enlive-html :as html]))


(defn String->Number [str]
  (let [n (read-string str)]
    (if (number? n) n nil)))

(defn ^String substring?
  [substring ^String s]
  (.contains s substring))


(defn get-links [links] 
  (map #(:href (:attrs %)) links))

(defn pause [a] (alter-meta! a assoc ::paused true))

(defn paused? [a] (::paused (meta a)))

(defn num-filter [exp] (apply str (filter #(#{\0,\1,\2,\3,\4,\5,\6,\7,\8,\9} %) exp)))






(defn defineIngredientCategory [s l] 
  (map #(:_id %) (filter #(substring? (:name %) s) l)))


(defn get-data [jsonText]
  (if (nil? (:graph jsonText))
    (first (:list (:md:item jsonText)))
    (first (:list (:md:item (first (remove #(nil? (:md:item %)) (:graph jsonText))))))))

(defn prepare-json [body]     
  (json/read-str
    (clojure.string/replace body "@" "") :key-fn keyword))


(defn extract-recipe [data] 
  (assoc {} 
         :title  (:name data)
         :image (:image data)
         :summary (:description data)
         :recipeYield (:recipeYield data)
         :instructions (:list (:recipeInstructions data))
         :ingredients  (:ingredients data)
         :nutrition (let [m (dissoc (:nutrition data) :type)]
                      (apply hash-map (interleave 
                                        (keys m)
                                        (map #(String->Number %) (vals m)))))  
         :prepTime (:prepTime data)
         :cookTime (:cookTime data)
         :totalTime (:totalTime data)))

(defn process-data [data]
  (extract-recipe (get-data (prepare-json data))))

(defn extract-meal [url meals]
  (first (filter #(substring? % url) meals)))


