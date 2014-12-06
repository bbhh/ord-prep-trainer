(ns ord-prep-trainer.core.api
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [ord-prep-trainer.core.util :as util]
            [ord-prep-trainer.core.db :as db]))
 
(defn get-base-uri [request]
  "Generate a base uri from a ring request. For example 'http://localhost:5000/api'."
  (let [scheme (name (:scheme request))
        context (:context request)
        hostname (get (:headers request) "host")]
    (str scheme "://" hostname context)))

(defn json-response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type" "application/json; charset=utf-8"}
   :body    (json/generate-string data)})
 
(defroutes api-routes
  (context "/api" []
    (GET "/" request (json-response ":)"))

    (GET "/results" request
      (let [params (:params request)
            sections-str (:sections params)
            sections (str/split sections-str #":")
            books-str (:books params)
            books (str/split books-str #":")
            sections-query (->> sections
                                (map #(hash-map :section-name %)))
            books-query (->> books
                             (map #(hash-map :book-name %)))]
        (json-response (db/query-data-db-by-sections-and-books sections-query books-query))))

    (GET "/stars" request
      (let [params (:params request)
            user-id (:user params)]
        (json-response (db/get-stars-for-user user-id))))

    (PUT "/stars" request
      (let [params (:params request)
            user-id (:user params)
            stars-str (:stars params)
            stars (str/split stars-str #",")]
        (json-response (db/update-stars-for-user user-id stars))))

    (GET "/book-names" request
      (json-response util/books-in-order))

    (ANY "*" []
      (route/not-found (slurp (io/resource "404.html"))))))
 
(def rest-api
  (handler/api api-routes))
