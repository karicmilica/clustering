(ns clustering.scraper.recipe_extractor
  (:require 
           [net.cgrand.enlive-html :as html]
           [monger.collection :as mc]
           [clustering.scraper.util-extraction :as utile]
           [clustering.db.db :as db])
  (:use [clojure.java.io :only [reader]])
  (:import (org.bson.types ObjectId))
  (:import (java.util.concurrent LinkedBlockingQueue BlockingQueue)))




(def meals '("breakfast" "lunch"))
(def url-queue2 (LinkedBlockingQueue.))
 
(def crawled-urls (atom #{}))
(def flag2 (atom false))
(def agents-search (set (repeatedly 5 #(agent {::t ::getUrlSearch}))))
(def agents (set (repeatedly 10 #(agent {::t ::getUrlRecipe :queue url-queue2}))))
(def urls (atom '()))
(def ingredient-categories (atom '()))

(declare dequeue!) 

;(defn readUrls []
  ;(apply list (map #(clojure.string/trim %) (line-seq (reader "urlsfoodcom.txt")))))
        

(defn readUrls []
       (let [l (map #(clojure.string/trim %) (line-seq (reader "urlsfoodcom.txt")))]
         (apply list(concat l (apply list (for [x l
           y (range 2 11)] 
       (str x "?pn=" y)))))))



(declare get-url-search)
(declare process-search)
(declare handle-results-search)
(declare get-url)
(declare process)
(declare handle-results)

(defmulti dispatch ::t)


(defmethod dispatch ::getUrlSearch [transition]
  (fn [ag] (send-off ag get-url-search))) 

(defmethod dispatch ::handleResultsSearch [transition]
  (fn [ag] (send-off ag handle-results-search)))

(defmethod dispatch ::processSearch [transition]
  (fn [ag] (send ag process-search)))

(defmethod dispatch ::getUrlRecipe [transition]
  (fn [ag] (send-off ag get-url))) 

(defmethod dispatch ::handleResultsRecipe [transition]
  (fn [ag] (send-off ag handle-results)))

(defmethod dispatch ::processRecipe [transition]
  (fn [ag] (send ag process)))

(defn run-agents [ags]
  (fn [a] (when (ags a)
            (send a (fn [state]
                      (when-not (utile/paused? *agent*)
                        ((dispatch state) *agent*))
                      state)))))


(declare get-url-search)

(defn set-flag-true! [arg] (compare-and-set! arg false true))

(defn stop [state flag] 
  (set-flag-true! flag)	
  state)

(defn dequeue! [queue ag]
  (if (empty? @queue)
    (utile/pause ag)
    (loop []
      (let [q     @queue
            value (peek q)
            nq    (pop q)]
        (if (compare-and-set! queue q nq)
          value
          (recur))))))






(defn run-recipe-link-extraction
  ([] (doseq [a agents-search] (run-recipe-link-extraction a)))
  ([a] ((run-agents agents-search) a)))

; start 2
(defn handle-results-search [{:keys [links-recipes category]}]
  (try
    (doseq [url links-recipes]
      (.put url-queue2 {:url url :category category}))
    {::t ::getUrlSearch}
    (finally (run-recipe-link-extraction *agent*))))

(defn process-search [{:keys [content url]}]
  (try
    (let [html-content (html/html-resource (java.io.StringReader. content))
          articles (html/select html-content [:div.rz-tl-e :a])]
      {::t ::handleResultsSearch
       :links-recipes (utile/get-links articles)
       :category (utile/extract-meal url meals)})
    (finally (run-recipe-link-extraction *agent*))))

(defn get-url-search [state]
  (let [url (dequeue! urls *agent*)]
    (try
      {:url url
       :content (slurp url)
       ::t ::processSearch}
    (catch Exception e
      state)
    (finally (run-recipe-link-extraction *agent*)))))




(defn prepare-link [link] 
  (str "http://www.w3.org/2012/pyMicrodata/extract?format=json&uri=" link))



(defn run-recipes-extraction
  ([] (doseq [a agents] (run-recipes-extraction a)))
  ([a] ((run-agents agents) a)))

(defn store-results [url recipe]
  (swap! crawled-urls conj url)
  (db/add-recipe recipe))


(defn handle-results [{:keys [recipe url]}]
  (try
    (if-not (@crawled-urls url) 
      (store-results url recipe))
    {::t ::getUrlRecipe
     :queue url-queue2}
    (finally (run-recipes-extraction *agent*))))


(defn process [{:keys [content pageContent url urlstr category]}]
  (try
    (let [html-content (html/html-resource (java.io.StringReader. pageContent))
          id (utile/num-filter urlstr)
          recipe (assoc (utile/process-data content) :category category)]
      {:url url
       ::t ::handleResultsRecipe
       :recipe (assoc recipe :_id id :url urlstr)})
    (finally (run-recipes-extraction *agent*))))


(defn  get-url [{:keys [^BlockingQueue queue] :as state}]
  (if (= @flag2 true) 
    (utile/pause *agent*) 
    (let [el (.take queue)
          urlstr (:url el)
          category (:category el)]
      (try
        (if (= "END" urlstr)
          (stop state flag2 )
          (let [url (clojure.java.io/as-url urlstr)] 
            (if (@crawled-urls url)
              {::t ::getUrlRecipe
               :queue queue}
              {:url url
               :urlstr urlstr
               :content (slurp (prepare-link url))
               :pageContent (slurp url)
               :category category
               ::t ::processRecipe})))
        (catch Exception e
          state)
        (finally (run-recipes-extraction *agent*))))))




(defn prepare-db []
  (db/format-db)) 

(defn run-scraper []
  (db/db-init)
  (prepare-db)
  (compare-and-set! urls '() (readUrls))
  (Thread/sleep 2000)
  (println (count @urls)) 
  (run-recipe-link-extraction) 
  (run-recipes-extraction) 
  (Thread/sleep 20000)
  (.put url-queue2 {:url "END"})
  (Thread/sleep 200000)
  ;(doseq [a agents-search] (println (agent-error a)))
  ;(doseq [a agents] (println (agent-error a)))
  (doseq [a agents] (utile/pause a))
  (println "2" (.size url-queue2))
  (println "recipes: " (count @crawled-urls))
  (println "finished"))


(defn -main [& args]
  (run-scraper))