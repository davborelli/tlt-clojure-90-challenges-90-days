(ns extract-and-transform)

(defn extract-transaction-summary
  [transaction]
  (let [transaction-id      (:id transaction)
        customer-name       (get-in transaction [:customer :name])
        customer-tier       (get-in transaction [:customer :tier])
        payment-amount      (get-in transaction [:payment :amount])
        payment-currency    (get-in transaction [:payment :currency])
        is-premium-customer (if (or (= customer-tier :gold) (= customer-tier :platinum)) true false)
        is-high-value       (if (> payment-amount 1000) true false)]
    {:transaction-id      transaction-id
     :customer-name       customer-name
     :customer-tier       customer-tier
     :payment-amount      payment-amount
     :payment-currency    payment-currency
     :is-premium-customer is-premium-customer
     :is-high-value       is-high-value}))

(defn- tst []
  (assert (=
(extract-transaction-summary 
  {:id "TXN-001" 
   :customer {:name "Alice" :tier :gold} 
   :payment {:method :credit-card :amount 1500.00 :currency "USD"} 
   :metadata {:timestamp "2024-01-15" :ip "192.168.1.1"}})
{:transaction-id "TXN-001" 
 :customer-name "Alice" 
 :customer-tier :gold 
 :payment-amount 1500.0 
 :payment-currency "USD" 
 :is-premium-customer true 
 :is-high-value true}))

(assert (=
(extract-transaction-summary 
  {:id "TXN-002" 
   :customer {:name "Bob" :tier :silver} 
   :payment {:method :debit-card :amount 500.00 :currency "USD"} 
   :metadata {:timestamp "2024-01-16" :ip "192.168.1.2"}})
{:transaction-id "TXN-002" 
 :customer-name "Bob" 
 :customer-tier :silver 
 :payment-amount 500.0 
 :payment-currency "USD" 
 :is-premium-customer false 
 :is-high-value false}))

  "SUCCESS")

(tst)
