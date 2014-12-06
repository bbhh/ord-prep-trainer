(ns ord-prep-trainer.core.boot
  (:require [ord-prep-trainer.core.api :refer [rest-api]]
            [ord-prep-trainer.core.site :refer [site]]
            [compojure.core :refer [routes]]))

;; combine site and rest-api
(def site-and-api (routes rest-api site))
