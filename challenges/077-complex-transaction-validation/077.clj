(ns complex-transaction-validation)

(defn validate-transaction
  [transaction]
  )

(defn- tst []
  (assert (=
(validate-transaction
  {:transaction-id "TX123" :from-account-id "ACC001" :to-account-id "ACC002" :amount 5000 :currency :USD :country-code "US" :transaction-count-24h 2 :account-status :active :available-balance 10000 :account-age-days 100 :is-international false :requires-2fa false :has-2fa-verified false})
{:status :valid :transaction-id "TX123"}))

(assert (=
(validate-transaction
  {:transaction-id "TX124" :from-account-id "ACC001" :to-account-id "ACC001" :amount 60000 :currency :JPY :country-code "US" :transaction-count-24h 11 :account-status :suspended :available-balance 50000 :account-age-days 15 :is-international false :requires-2fa false :has-2fa-verified false})
{:status :invalid 
 :transaction-id "TX124" 
 :errors ["Account status must be active" 
          "Cannot transfer to same account" 
          "Amount exceeds single transaction limit of 50000" 
          "Currency JPY is not supported" 
          "New accounts (< 30 days) limited to 5000 per transaction" 
          "Transaction velocity limit exceeded (11 in 24h)"]}))

  "SUCCESS")

(tst)
