(ns json-conditional-fields 
  (:require
    [clojure.string :as str]))

(defn kebab->cammel
  [word]
  (let [[first-word & rest-words] (str/split word #"-")]
    (apply str first-word (map str/capitalize rest-words))))

(defn assoc-some [m k v] (if (some? v) (assoc m k v) m))

;; (defn build-json-response
;;   [response-data]
;;   (reduce-kv (fn [acc k v]
;;                (assoc-some acc (keyword (kebab->cammel (name k))) (if (= k :status)
;;                                                                     (name v)
;;                                                                     v)))
;;              {}
;;              response-data))

(defn build-json-response
  [{:keys [status
           user-id
           success-message
           error-message
           data
           metadata]}]
  (-> {:status (name status)
       :userId user-id}
      (assoc-some :successMessage success-message)
      (assoc-some :errorMessage   error-message)
      (assoc-some :data           data)
      (assoc-some :metadata       metadata)))

(build-json-response
 {:status :pending :user-id "U789" :success-message nil :error-message nil :data nil :metadata nil})

(defn- tst []
  (assert (=
(build-json-response
  {:status :success :user-id "U123" :success-message "Operation completed" :error-message nil :data {:result "OK"} :metadata nil})
{:status "success" :userId "U123" :successMessage "Operation completed" :data {:result "OK"}}))

(assert (=
(build-json-response
  {:status :error :user-id "U456" :success-message nil :error-message "Invalid input" :data nil :metadata {:timestamp "2024-01-15"}})
{:status "error" :userId "U456" :errorMessage "Invalid input" :metadata {:timestamp "2024-01-15"}}))

(assert (=
(build-json-response
  {:status :pending :user-id "U789" :success-message nil :error-message nil :data nil :metadata nil})
{:status "pending" :userId "U789"}))

  "SUCCESS")

(tst)
