(ns clustering.db.db
  (:require [monger.core :as mg]
            [monger.collection :as mc])
   (:use [monger.conversion :only [from-db-object]]
         [monger.operators])
   (:import (org.bson.types ObjectId)))

(defn db-init []
  (mg/connect!)
  (mg/set-db! (mg/get-db "my-app1")))

(defn format-db []
  (mc/remove "ingredient")
  (mc/remove "recipes"))




(defn add-recipe [recipe]
  (mc/insert "recipes" recipe))




(defn String->Number [str]
  (let [n (read-string str)]
       (if (number? n) n nil)))

(defn find-recipe-by-id [id] (mc/find-map-by-id "recipes" id))

(defn find-by-meal [meal]
  (mc/find-maps "recipes" {:category meal}))

(defn find-recipes []
  (mc/find-maps "recipes"))

