;; =============================================================================
;; 077 - COMPLEX TRANSACTION VALIDATION
;; Level: 16/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This function validates transactions against 12 different business rules,
;; collecting ALL validation errors rather than failing fast. This approach
;; gives users complete feedback about what's wrong with their transaction.
;;
;; The key pattern is error accumulation: we start with an empty errors vector
;; and use cond-> to conditionally add error messages. Each condition checks
;; one rule, and if it fails, adds a descriptive error message.
;;
;; This differs from fail-fast validation where we return on the first error.
;; Error accumulation is better for user experience (fix all issues at once)
;; but requires checking all rules even when early ones fail.
;;
;; The pattern mirrors production validation systems in financial applications
;; where comprehensive error reporting is critical for compliance and UX.

(ns challenge-077.solution)

;; CONSTANTS
;; ---------

(def supported-currencies #{:USD :EUR :GBP :BRL})
(def restricted-countries #{:KP :IR :SY :CU})
(def max-single-amount 50000)
(def new-account-limit 5000)
(def new-account-threshold-days 30)
(def velocity-limit 10)
(def high-velocity-threshold 5)
(def high-velocity-amount 10000)
(def large-international-amount 10000)

;; MAIN IMPLEMENTATION
;; -------------------

(defn validate-transaction
  "Validates a transaction against multiple business rules.

  Checks 12 validation rules and collects all errors:
  1. Account status must be active
  2. Cannot transfer to same account
  3. Amount must be positive
  4. Amount within single transaction limit
  5. Currency must be supported
  6. Sufficient balance
  7. New account limits
  8. Velocity limit check
  9. High velocity + high amount check
  10. Country restrictions
  11. International 2FA requirement
  12. Large international 2FA requirement

  Parameters:
  - transaction: Map with all transaction details

  Returns: {:status :valid/:invalid :transaction-id ... :errors [...]}
           Success has no :errors key, failure has :errors with all violations"
  [transaction]
  (let [{:keys [transaction-id from-account-id to-account-id amount currency
                country-code transaction-count-24h account-status available-balance
                account-age-days is-international requires-2fa has-2fa-verified]} transaction

        ;; Accumulate all validation errors
        errors (cond-> []
                 ;; Rule 1: Account must be active
                 (not= account-status :active)
                 (conj "Account status must be active")

                 ;; Rule 2: Cannot transfer to same account
                 (= from-account-id to-account-id)
                 (conj "Cannot transfer to same account")

                 ;; Rule 3: Amount must be positive
                 (<= amount 0)
                 (conj "Transaction amount must be positive")

                 ;; Rule 4: Single transaction limit
                 (> amount max-single-amount)
                 (conj (str "Amount exceeds single transaction limit of " max-single-amount))

                 ;; Rule 5: Currency support
                 (not (contains? supported-currencies currency))
                 (conj (str "Currency " (name currency) " is not supported"))

                 ;; Rule 6: Sufficient balance
                 (> amount available-balance)
                 (conj "Insufficient funds - amount exceeds available balance")

                 ;; Rule 7: New account limit
                 (and (< account-age-days new-account-threshold-days)
                      (> amount new-account-limit))
                 (conj (str "New accounts (< " new-account-threshold-days
                            " days) limited to " new-account-limit " per transaction"))

                 ;; Rule 8: Transaction velocity limit
                 (> transaction-count-24h velocity-limit)
                 (conj (str "Transaction velocity limit exceeded ("
                            transaction-count-24h " in 24h)"))

                 ;; Rule 9: High velocity + high amount
                 (and (> transaction-count-24h high-velocity-threshold)
                      (> amount high-velocity-amount))
                 (conj "High transaction velocity with large amount detected")

                 ;; Rule 10: Country restrictions
                 (contains? restricted-countries (keyword country-code))
                 (conj (str "Transactions to country " country-code " are restricted"))

                 ;; Rule 11: International 2FA requirement
                 (and is-international requires-2fa (not has-2fa-verified))
                 (conj "International transaction requires 2FA verification")

                 ;; Rule 12: Large international requires 2FA
                 (and is-international (> amount large-international-amount) (not has-2fa-verified))
                 (conj "Large international transactions require 2FA verification"))]

    ;; Return success or failure with all errors
    (if (empty? errors)
      {:status :valid :transaction-id transaction-id}
      {:status :invalid :transaction-id transaction-id :errors errors})))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Error Accumulation vs Fail-Fast
;;    Two validation approaches:
;;    - Fail-fast: Return immediately on first error (faster, less complete)
;;    - Error accumulation: Collect ALL errors (slower, better UX)
;;    We use accumulation because users want to fix all issues at once.
;;    In production, this reduces support tickets and improves satisfaction.
;;
;; 2. cond-> for Conditional Building
;;    The cond-> macro threads a value through conditions:
;;    (cond-> []
;;      condition1 (conj "error1")
;;      condition2 (conj "error2"))
;;    If condition1 is true, (conj [] "error1") is evaluated.
;;    If condition2 is true, (conj [...] "error2") is evaluated.
;;    This builds up the errors vector incrementally.
;;
;; 3. Constants for Magic Numbers
;;    Instead of hardcoding 50000, 5000, etc., we define constants at the top.
;;    Benefits:
;;    - Self-documenting: max-single-amount is clearer than 50000
;;    - Easy to change: update one place, affects all uses
;;    - Type safety: def enforces immutability
;;    - Reusability: other functions can reference the same limits
;;
;; 4. Compound Conditions with and
;;    Rules like "high velocity + high amount" require multiple conditions:
;;    (and (> transaction-count-24h 5) (> amount 10000))
;;    Both must be true to add the error. The `and` short-circuits,
;;    so if velocity is low, amount isn't even checked.
;;
;; 5. Dynamic Error Messages
;;    Using (str "Amount exceeds " max-single-amount) creates messages
;;    that reflect current configuration. If max-single-amount changes
;;    from 50000 to 100000, error messages update automatically.
;;    This keeps errors accurate without manual updates.
;;
;; 6. Set Membership with contains?
;;    (contains? supported-currencies currency) checks if currency is in the set.
;;    Sets provide O(1) lookup, much faster than searching vectors.
;;    This is crucial when checking against large lists (countries, currencies).
;;
;; 7. Keyword Conversion with keyword
;;    (keyword country-code) converts string "US" to keyword :US.
;;    This normalizes data for comparison with restricted-countries set.
;;    Always normalize data types before comparisons to avoid bugs.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo5.md
;;
;; Pattern used: Multi-rule validation with error accumulation
;;
;; The reference shows validation in money transfer systems:
;;   (defn valid-money-out? [request]
;;     (cond
;;       (not (valid-amount? amount)) [:error "Invalid amount"]
;;       (not (sufficient-balance? account amount)) [:error "Insufficient funds"]
;;       (not (within-limits? amount)) [:error "Exceeds limits"]
;;       :else [:ok]))
;;
;; Real-world usage: Production financial systems validate:
;; - Payment requests (amount, account, limits, fraud signals)
;; - User registrations (email format, age, country, terms acceptance)
;; - API requests (authentication, rate limits, schema validation)
;; - Data imports (format, completeness, business rules)
;;
;; The key insight: comprehensive validation catches issues early,
;; reduces errors in downstream systems, and improves user experience
;; by providing all feedback at once.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Valid Transaction - All Rules Pass
  (validate-transaction {:transaction-id "TX123"
                         :from-account-id "ACC001"
                         :to-account-id "ACC002"
                         :amount 5000
                         :currency :USD
                         :country-code "US"
                         :transaction-count-24h 2
                         :account-status :active
                         :available-balance 10000
                         :account-age-days 100
                         :is-international false
                         :requires-2fa false
                         :has-2fa-verified false})
  ;; => {:status :valid :transaction-id "TX123"}

  ;; Example 2: Multiple Validation Errors
  (validate-transaction {:transaction-id "TX124"
                         :from-account-id "ACC001"
                         :to-account-id "ACC001"
                         :amount 60000
                         :currency :JPY
                         :country-code "US"
                         :transaction-count-24h 11
                         :account-status :suspended
                         :available-balance 50000
                         :account-age-days 15
                         :is-international false
                         :requires-2fa false
                         :has-2fa-verified false})
  ;; => {:status :invalid
  ;;     :transaction-id "TX124"
  ;;     :errors ["Account status must be active"
  ;;              "Cannot transfer to same account"
  ;;              "Amount exceeds single transaction limit of 50000"
  ;;              "Currency JPY is not supported"
  ;;              "New accounts (< 30 days) limited to 5000 per transaction"
  ;;              "Transaction velocity limit exceeded (11 in 24h)"]}

  ;; Example 3: Insufficient Balance
  (validate-transaction {:transaction-id "TX125"
                         :from-account-id "ACC001"
                         :to-account-id "ACC002"
                         :amount 15000
                         :currency :USD
                         :country-code "US"
                         :transaction-count-24h 1
                         :account-status :active
                         :available-balance 10000
                         :account-age-days 100
                         :is-international false
                         :requires-2fa false
                         :has-2fa-verified false})
  ;; => {:status :invalid
  ;;     :transaction-id "TX125"
  ;;     :errors ["Insufficient funds - amount exceeds available balance"]}

  ;; Example 4: International 2FA Required
  (validate-transaction {:transaction-id "TX126"
                         :from-account-id "ACC001"
                         :to-account-id "ACC002"
                         :amount 12000
                         :currency :EUR
                         :country-code "DE"
                         :transaction-count-24h 2
                         :account-status :active
                         :available-balance 20000
                         :account-age-days 100
                         :is-international true
                         :requires-2fa true
                         :has-2fa-verified false})
  ;; => {:status :invalid
  ;;     :transaction-id "TX126"
  ;;     :errors ["International transaction requires 2FA verification"
  ;;              "Large international transactions require 2FA verification"]}

  ;; Example 5: Restricted Country
  (validate-transaction {:transaction-id "TX127"
                         :from-account-id "ACC001"
                         :to-account-id "ACC002"
                         :amount 1000
                         :currency :USD
                         :country-code "IR"
                         :transaction-count-24h 1
                         :account-status :active
                         :available-balance 5000
                         :account-age-days 100
                         :is-international true
                         :requires-2fa false
                         :has-2fa-verified false})
  ;; => {:status :invalid
  ;;     :transaction-id "TX127"
  ;;     :errors ["Transactions to country IR are restricted"]}

  ;; Example 6: New Account Limit Violation
  (validate-transaction {:transaction-id "TX128"
                         :from-account-id "ACC001"
                         :to-account-id "ACC002"
                         :amount 7000
                         :currency :USD
                         :country-code "US"
                         :transaction-count-24h 1
                         :account-status :active
                         :available-balance 10000
                         :account-age-days 15
                         :is-international false
                         :requires-2fa false
                         :has-2fa-verified false})
  ;; => {:status :invalid
  ;;     :transaction-id "TX128"
  ;;     :errors ["New accounts (< 30 days) limited to 5000 per transaction"]}

  ;; Example 7: High Velocity Warning
  (validate-transaction {:transaction-id "TX129"
                         :from-account-id "ACC001"
                         :to-account-id "ACC002"
                         :amount 15000
                         :currency :USD
                         :country-code "US"
                         :transaction-count-24h 6
                         :account-status :active
                         :available-balance 20000
                         :account-age-days 100
                         :is-international false
                         :requires-2fa false
                         :has-2fa-verified false})
  ;; => {:status :invalid
  ;;     :transaction-id "TX129"
  ;;     :errors ["High transaction velocity with large amount detected"]}
)

;; TESTS
;; -----

(defn -test []
  ;; Test valid transaction
  (let [result (validate-transaction {:transaction-id "TX123" :from-account-id "ACC001"
                                      :to-account-id "ACC002" :amount 5000 :currency :USD
                                      :country-code "US" :transaction-count-24h 2
                                      :account-status :active :available-balance 10000
                                      :account-age-days 100 :is-international false
                                      :requires-2fa false :has-2fa-verified false})]
    (assert (= (:status result) :valid) "Should be valid")
    (assert (nil? (:errors result)) "Should have no errors"))

  ;; Test multiple errors
  (let [result (validate-transaction {:transaction-id "TX124" :from-account-id "ACC001"
                                      :to-account-id "ACC001" :amount 60000 :currency :JPY
                                      :country-code "US" :transaction-count-24h 11
                                      :account-status :suspended :available-balance 50000
                                      :account-age-days 15 :is-international false
                                      :requires-2fa false :has-2fa-verified false})]
    (assert (= (:status result) :invalid) "Should be invalid")
    (assert (= (count (:errors result)) 6) "Should have 6 errors"))

  ;; Test insufficient balance
  (let [result (validate-transaction {:transaction-id "TX125" :from-account-id "ACC001"
                                      :to-account-id "ACC002" :amount 15000 :currency :USD
                                      :country-code "US" :transaction-count-24h 1
                                      :account-status :active :available-balance 10000
                                      :account-age-days 100 :is-international false
                                      :requires-2fa false :has-2fa-verified false})]
    (assert (= (:status result) :invalid) "Should be invalid")
    (assert (= (count (:errors result)) 1) "Should have 1 error")
    (assert (some #(re-find #"Insufficient funds" %) (:errors result)) "Should mention insufficient funds"))

  ;; Test international 2FA
  (let [result (validate-transaction {:transaction-id "TX126" :from-account-id "ACC001"
                                      :to-account-id "ACC002" :amount 12000 :currency :EUR
                                      :country-code "DE" :transaction-count-24h 2
                                      :account-status :active :available-balance 20000
                                      :account-age-days 100 :is-international true
                                      :requires-2fa true :has-2fa-verified false})]
    (assert (= (:status result) :invalid) "Should be invalid")
    (assert (= (count (:errors result)) 2) "Should have 2FA errors"))

  ;; Test new account limit
  (let [result (validate-transaction {:transaction-id "TX128" :from-account-id "ACC001"
                                      :to-account-id "ACC002" :amount 7000 :currency :USD
                                      :country-code "US" :transaction-count-24h 1
                                      :account-status :active :available-balance 10000
                                      :account-age-days 15 :is-international false
                                      :requires-2fa false :has-2fa-verified false})]
    (assert (= (:status result) :invalid) "Should be invalid")
    (assert (some #(re-find #"New accounts" %) (:errors result)) "Should mention new account limit"))

  (println "✓ All tests passed! The validate-transaction function works correctly."))

;; Run: (-test)
