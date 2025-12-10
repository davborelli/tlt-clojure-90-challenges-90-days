;; =============================================================================
;; 047 - EXTRACT AND TRANSFORM WITH DESTRUCTURING
;; Level: 10/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This solution demonstrates advanced destructuring to extract nested fields
;; from complex data structures. Instead of multiple get-in calls, we use
;; nested destructuring in the function parameters to declaratively specify
;; exactly which fields we need.
;;
;; The approach destructures the nested transaction map in one step, extracting
;; id from root, name and tier from :customer, and amount and currency from
;; :payment. We then calculate derived boolean fields based on business rules.
;;
;; This pattern is common in production adapters and transformers: destructuring
;; makes the code more readable by clearly showing which fields are used,
;; and eliminates the noise of repeated get-in calls.

(ns challenge-047.solution)

;; IMPLEMENTATION
;; --------------

(defn extract-transaction-summary
  "Extracts nested fields and calculates summary from transaction.

  Parameters:
  - transaction: Nested transaction map with :customer and :payment

  Returns: Flattened summary map with calculated boolean fields"
  [{:keys [id]                                    ;; Extract id from root
    {:keys [name tier]} :customer                ;; Extract from :customer
    {:keys [amount currency]} :payment}]         ;; Extract from :payment
  ;; Build flattened result with calculated fields
  {:transaction-id id
   :customer-name name
   :customer-tier tier
   :payment-amount amount
   :payment-currency currency
   ;; Calculate premium status: gold or platinum
   :is-premium-customer (boolean (#{:gold :platinum} tier))
   ;; Calculate high-value status: amount > 1000
   :is-high-value (> amount 1000)})

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Nested Destructuring
;;    Instead of:
;;      (let [id (:id transaction)
;;            name (get-in transaction [:customer :name])
;;            tier (get-in transaction [:customer :tier])
;;            amount (get-in transaction [:payment :amount])
;;            currency (get-in transaction [:payment :currency])]
;;        ...)
;;    We use nested destructuring:
;;      [{:keys [id]
;;        {:keys [name tier]} :customer
;;        {:keys [amount currency]} :payment}]
;;    This is more declarative and shows intent clearly.
;;
;; 2. Destructuring in Function Parameters
;;    Clojure allows destructuring directly in function parameter lists.
;;    This makes the function signature self-documenting - you can see
;;    exactly which fields the function uses without reading the body.
;;    It's more concise than destructuring in a let binding.
;;
;; 3. Set Membership for Enum Checks
;;    To check if a value is in a set of options:
;;      (#{:gold :platinum} tier)
;;    If tier is :gold or :platinum, returns the tier (truthy).
;;    If tier is :silver, returns nil (falsy).
;;    This is idiomatic Clojure for checking enum membership.
;;    Wrap in `boolean` to ensure true/false result.
;;
;; 4. Calculated/Derived Fields
;;    The summary includes fields not in the source data:
;;    - :is-premium-customer (derived from tier)
;;    - :is-high-value (derived from amount)
;;    Transformers often add such fields for business logic convenience.
;;    These are pure calculations with no side effects.
;;
;; 5. Ignoring Irrelevant Fields
;;    The source has :metadata which we don't need. Destructuring lets us
;;    extract only what's relevant, ignoring the rest. This is cleaner than
;;    manually filtering out unwanted fields.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo1.md
;;
;; Pattern used: Advanced nested destructuring for data extraction
;;
;; Real-world usage: Production code uses destructuring to extract data:
;;   (defn process-review
;;     [{:keys [id] {:keys [customer-id]} :customer ...}]
;;     ...)
;;
;; The reference shows how destructuring makes extraction code more readable
;; and declarative. It's especially valuable in adapters, transformers, and
;; any function that works with nested data structures.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Premium customer, high-value transaction
  (extract-transaction-summary
    {:id "TXN-001"
     :customer {:name "Alice" :tier :gold}
     :payment {:method :credit-card :amount 1500.00 :currency "USD"}
     :metadata {:timestamp "2024-01-15" :ip "192.168.1.1"}})
  ;; => {:transaction-id "TXN-001"
  ;;     :customer-name "Alice"
  ;;     :customer-tier :gold
  ;;     :payment-amount 1500.0
  ;;     :payment-currency "USD"
  ;;     :is-premium-customer true
  ;;     :is-high-value true}

  ;; Example 2: Regular customer, low-value transaction
  (extract-transaction-summary
    {:id "TXN-002"
     :customer {:name "Bob" :tier :silver}
     :payment {:method :debit-card :amount 500.00 :currency "USD"}
     :metadata {:timestamp "2024-01-16" :ip "192.168.1.2"}})
  ;; => {:transaction-id "TXN-002"
  ;;     :customer-name "Bob"
  ;;     :customer-tier :silver
  ;;     :payment-amount 500.0
  ;;     :payment-currency "USD"
  ;;     :is-premium-customer false
  ;;     :is-high-value false}

  ;; Example 3: Platinum customer (premium), low amount
  (extract-transaction-summary
    {:id "TXN-003"
     :customer {:name "Charlie" :tier :platinum}
     :payment {:method :wire-transfer :amount 800.00 :currency "EUR"}
     :metadata {:timestamp "2024-01-17" :ip "192.168.1.3"}})
  ;; => {:is-premium-customer true, :is-high-value false}

  ;; Example 4: Bronze tier, high amount
  (extract-transaction-summary
    {:id "TXN-004"
     :customer {:name "Diana" :tier :bronze}
     :payment {:method :credit-card :amount 5000.00 :currency "GBP"}
     :metadata {:timestamp "2024-01-18" :ip "192.168.1.4"}})
  ;; => {:is-premium-customer false, :is-high-value true}
)

;; TESTS
;; -----

(defn -test []
  ;; Test premium customer + high-value
  (let [result (extract-transaction-summary
                 {:id "TXN-001"
                  :customer {:name "Alice" :tier :gold}
                  :payment {:method :credit-card :amount 1500.00 :currency "USD"}
                  :metadata {:timestamp "2024-01-15" :ip "192.168.1.1"}})]
    (assert (= (:transaction-id result) "TXN-001")
            "Should extract transaction ID")
    (assert (= (:customer-name result) "Alice")
            "Should extract customer name")
    (assert (= (:customer-tier result) :gold)
            "Should extract customer tier")
    (assert (= (:payment-amount result) 1500.0)
            "Should extract payment amount")
    (assert (= (:payment-currency result) "USD")
            "Should extract payment currency")
    (assert (true? (:is-premium-customer result))
            "Should mark gold as premium")
    (assert (true? (:is-high-value result))
            "Should mark >1000 as high-value"))

  ;; Test regular customer + low-value
  (let [result (extract-transaction-summary
                 {:id "TXN-002"
                  :customer {:name "Bob" :tier :silver}
                  :payment {:method :debit-card :amount 500.00 :currency "USD"}
                  :metadata {:timestamp "2024-01-16" :ip "192.168.1.2"}})]
    (assert (false? (:is-premium-customer result))
            "Should mark silver as not premium")
    (assert (false? (:is-high-value result))
            "Should mark <=1000 as not high-value"))

  ;; Test platinum tier
  (let [result (extract-transaction-summary
                 {:id "TXN-003"
                  :customer {:name "Charlie" :tier :platinum}
                  :payment {:method :wire-transfer :amount 800.00 :currency "EUR"}
                  :metadata {:timestamp "2024-01-17" :ip "192.168.1.3"}})]
    (assert (true? (:is-premium-customer result))
            "Should mark platinum as premium")
    (assert (false? (:is-high-value result))
            "Should mark 800 as not high-value"))

  ;; Test bronze tier with high amount
  (let [result (extract-transaction-summary
                 {:id "TXN-004"
                  :customer {:name "Diana" :tier :bronze}
                  :payment {:method :credit-card :amount 5000.00 :currency "GBP"}
                  :metadata {:timestamp "2024-01-18" :ip "192.168.1.4"}})]
    (assert (false? (:is-premium-customer result))
            "Should mark bronze as not premium")
    (assert (true? (:is-high-value result))
            "Should mark 5000 as high-value"))

  ;; Test exact boundary (1000)
  (let [result (extract-transaction-summary
                 {:id "TXN-005"
                  :customer {:name "Eve" :tier :silver}
                  :payment {:method :credit-card :amount 1000.00 :currency "USD"}
                  :metadata {:timestamp "2024-01-19" :ip "192.168.1.5"}})]
    (assert (false? (:is-high-value result))
            "Should mark exactly 1000 as not high-value (> not >=)"))

  (println "✓ All tests passed!"))

;; Run: (-test)
