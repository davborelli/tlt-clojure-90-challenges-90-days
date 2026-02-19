(ns json-conditional-fields)

(defn build-json-response
  [response-data]
  )

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
