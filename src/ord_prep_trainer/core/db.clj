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

(defn read-data-json-file []
  (let [data-raw (slurp "data/combined.json")
        data-json (json/read-str data-raw :key-fn keyword)
        books (:books data-json)
        flattened-books (map (fn [book]
                               (let [book-name (:book-name book)
                                     sections (:sections book)
                                     new-sections (map #(assoc % :book-name book-name) sections)]
                                 new-sections)) books)
        merged-books (apply concat flattened-books)]
    merged-books))

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
            sections (get book "sections")
            new-sections (-> (map #(assoc % :book-name book-name) sections)
                             (assoc :_id (ObjectId.)))]
        (dorun (map #(mc/insert db coll %) new-sections))))))

(defn query-data-db [query]
  (let [coll "data"
        results (->> (mc/find-maps db coll query)
                     (map #(dissoc % :_id))                                                                       ; remove MongoDB-specific :id
                     (map (fn [item]                                                                              ; replace newlines with <br/>
                            (let [content (:content item)]
                              (if (vector? content)
                                (assoc item :content (str/join "<br/>" content))
                                (assoc item :content (str/replace content #"\n" "<br/>"))))))
                     (sort-by #((into {} (map-indexed (fn [i e] [e i]) util/books-in-order)) (:book-name %))))]   ; sort by canonical order
    ;; (println query)
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
