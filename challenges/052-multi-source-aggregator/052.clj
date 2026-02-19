(ns multi-source-aggregator)

(defn aggregate-user-data
  [user-profile user-preferences user-activity]
  )

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
