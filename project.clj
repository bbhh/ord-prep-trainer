(defproject ord-prep-trainer "0.1.0-SNAPSHOT"
  :description "Ordination Preparation Trainer"
  :url "https://github.com/bbhh/ord-prep-trainer"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.json "0.2.5"]
                 [compojure "1.2.0"]
                 [cheshire "5.3.1"]
                 [ring/ring-defaults "0.1.2"]
                 [com.novemberain/monger "2.0.0"]]
  :plugins [[lein-ring "0.8.13"]
            [cider/cider-nrepl "0.8.1"]]
  :ring {:handler ord-prep-trainer.core.boot/site-and-api}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
