(ns unflatten-config 
  (:require
    [clojure.string :as str]))

(def config {:db "database" :api "api" :log "logging"})

(defn unflatten-config
  [flat-config]
  (reduce-kv (fn [acc k v]
               (let [[first-part & rest-parts] (str/split (name k) #"-")
                     first-level               (keyword (get config (keyword first-part) first-part))
                     second-level              (keyword (str/join "-" rest-parts))]
                 (assoc-in acc [first-level second-level] v)))
             {}
             flat-config))

(defn- tst []
  (assert (=
(unflatten-config 
  {:db-host "localhost" 
   :db-port 5432 
   :db-name "myapp" 
   :api-base-url "https://api.example.com" 
   :api-timeout 30 
   :api-retry-count 3 
   :log-level "info" 
   :log-file "/var/log/app.log"})
{:database {:host "localhost" :port 5432 :name "myapp"} 
 :api {:base-url "https://api.example.com" :timeout 30 :retry-count 3} 
 :logging {:level "info" :file "/var/log/app.log"}}))

(assert (=
(unflatten-config 
  {:db-host "db.example.com" 
   :db-port 3306 
   :db-name "production" 
   :api-base-url "https://prod-api.example.com" 
   :api-timeout 60 
   :api-retry-count 5 
   :log-level "warn" 
   :log-file "/var/log/prod.log"})
{:database {:host "db.example.com" :port 3306 :name "production"} 
 :api {:base-url "https://prod-api.example.com" :timeout 60 :retry-count 5} 
 :logging {:level "warn" :file "/var/log/prod.log"}}))

  "SUCCESS")

(tst)
