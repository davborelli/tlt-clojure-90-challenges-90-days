(ns multi-source-aggregator)

;; (defn aggregate-user-data
;;   [user-profile user-preferences {:keys [login-count] :as user-activity}]
;;   (let [user-preferences (or user-preferences {:theme         "default"
;;                                                :language      "en"
;;                                                :notifications false})
;;         merged-data (merge user-profile user-preferences user-activity)
;;         activity-level (or
;;                         (when (>= login-count 100) "very-active")
;;                         (when (>= login-count 50)  "active")
;;                         (when (>= login-count 10)  "moderate")
;;                         (when (<  login-count 10)  "low"))]
;;     (assoc merged-data :activity-level (keyword activity-level))))

(defn aggregate-user-data
  [user-profile user-preferences {:keys [login-count] :as user-activity}]
  (let [preferences    (merge {:theme "default" :language "en" :notifications false}
                              user-preferences)
        activity-level (cond
                         (>= login-count 100) :very-active
                         (>= login-count 50)  :active
                         (>= login-count 10)  :moderate
                         :else                :low)]
    (merge user-profile preferences user-activity {:activity-level activity-level})))

(defn- tst []
  (assert (=
           (aggregate-user-data
            {:user-id "USER-1" :name "Alice" :email "alice@example.com" :join-date "2023-01-15"}
            {:theme "dark" :language "pt" :notifications true}
            {:last-login "2024-01-15" :login-count 150 :posts-count 45})
           {:user-id "USER-1"
            :name "Alice"
            :email "alice@example.com"
            :join-date "2023-01-15"
            :theme "dark"
            :language "pt"
            :notifications true
            :last-login "2024-01-15"
            :login-count 150
            :posts-count 45
            :activity-level :very-active}))

  (assert (=
           (aggregate-user-data
            {:user-id "USER-2" :name "Bob" :email "bob@example.com" :join-date "2024-01-01"}
            nil
            {:last-login "2024-01-10" :login-count 5 :posts-count 2})
           {:user-id "USER-2"
            :name "Bob"
            :email "bob@example.com"
            :join-date "2024-01-01"
            :theme "default"
            :language "en"
            :notifications false
            :last-login "2024-01-10"
            :login-count 5
            :posts-count 2
            :activity-level :low}))

  "SUCCESS")

(tst)
