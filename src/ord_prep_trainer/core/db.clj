(ns ord-prep-trainer.core.db
  (:require [clojure.data.json :as json]
            [clojure.string :as str]
            [clojure.pprint :as pp]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as mq]
            [monger.operators :refer :all]
            [monger.conversion :as mv]
            [ord-prep-trainer.core.util :as util])
  (:import org.bson.types.ObjectId))

;;;;; GLOBAL

;; localhost, default port
(def conn (mg/connect))
(def db (mg/get-db conn "ord-prep"))

;;;;; DATA

(defn vectorize [item]
  (if (vector? item)
    item
    (vector item)))

(defn insert-data-into-db [json-file]
  (let [data-raw (slurp json-file)
        data-json (json/read-str data-raw)
        books (get data-json "books")
        coll "data"]

    ;; create collection if nonexistent
    (if-not (mc/exists? db coll)
      (mc/create db coll nil)
      ;; remove all documents
      (mc/remove db coll))

    ;; insert new documents
    (doseq [book books]
      (let [book-name (get book "book-name")
            sections (->> (get book "sections")
                          (map (fn [section]
                                 (map-indexed (fn [idx itm]
                                                (hash-map
                                                  :_id (ObjectId.)
                                                  :book-name book-name
                                                  :section-name (get section "section-name")
                                                  :part (inc idx)
                                                  :content itm))
                                   (vectorize (get section "content")))))
                          (apply concat))]
        (dorun (map #(mc/insert db coll %) sections))))))

(defn query-data-db [query]
  (let [coll "data"
        results (->> (mc/find-maps db coll query)
                     ;; remove MongoDB-specific :id
                     (map #(dissoc % :_id))
                     ;; replace newlines with <br/>
                     (map #(assoc % :content (str/replace (:content %) #"\n" "<br/>")))
                     ;; sort by canonical order
                     (sort-by #((into {} (map-indexed (fn [i e] [e i]) util/books-in-order)) (:book-name %))))]
    results))

(defn query-data-db-with-or-conditions [query]
  (query-data-db {$or query}))

(defn query-data-db-by-sections-and-books [sections-query books-query]
  (query-data-db {$and [{$or sections-query} {$or books-query}]}))

;;;;; STARS

(defn get-stars-for-user [user-id]
  (->> (mc/find-maps db "stars" {:user user-id} ["stars"])
       (first)
       (:stars)))

(defn update-stars-for-user [user-id stars]
  (mc/update db "stars" {:user user-id} {$set {:stars stars}} {:upsert true})
  "OK")
