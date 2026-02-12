(ns extract-and-transform)

(defn extract-transaction-summary
  [transaction]
  )

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
