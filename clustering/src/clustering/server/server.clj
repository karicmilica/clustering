(ns clustering.server.server
  (:require [clustering.db.db :as db]
            [noir.server :as server]
            [noir.session :as session]
            [noir.response :as resp]
            [noir.core :refer [defpage defpartial]]
            [net.cgrand.enlive-html :as html])
  (:import (org.bson.types ObjectId)))



(defn start-server []
  (server/start 8080)
  (db/db-init)
  (Thread/sleep 2000))



(html/deftemplate recipes-template "clustering/server/clusters.html" [title]
   [:title] (html/content title))

(defpage "/recipes" {}
  (recipes-template "Breakfast"))


(defpage "/id/:id" {:keys [id]}
  (println (str "gettttttttttt " id))
  (resp/json (dissoc (db/find-recipe-by-id id) :_id)))





  

(defn -main [& args]
  (start-server))