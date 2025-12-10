;; =============================================================================
;; 041 - CATEGORIZE TRANSACTION
;; Level: 9/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This function categorizes financial transactions using a decision tree
;; implemented with cond. The order of conditions matters: we check high-
;; priority rules first (urgent flag, high amounts) before specific categories.
;;
;; The approach uses destructuring to access transaction fields, then evaluates
;; conditions in priority order. Some conditions are simple (priority flag),
;; others are compound (type AND amount), and some check string values
;; (merchant category).
;;
;; This pattern is fundamental in production systems for transaction processing,
;; fraud detection, and financial reporting, where complex business rules must
;; be applied consistently to classify operations.

(ns challenge-041.solution)

;; IMPLEMENTATION
;; --------------

(defn categorize-transaction
  "Categorizes a transaction based on type, amount, merchant, and priority.

  Parameters:
  - transaction: Map with :type, :amount, :merchant-category, :priority

  Returns: Keyword category"
  [transaction]
  (let [{:keys [type amount merchant-category priority]} transaction]
    (cond
      ;; Rule 1: Priority flag overrides everything
      (true? priority)
      :urgent

      ;; Rule 2: High-value transactions (any type)
      (> amount 5000)
      :high-value

      ;; Rule 3: High-value transfers (compound condition)
      (and (= type :transfer) (> amount 1000))
      :high-value

      ;; Rule 4: Travel transactions
      (= merchant-category "travel")
      :travel

      ;; Rule 5: Dining transactions
      (= merchant-category "restaurant")
      :dining

      ;; Rule 6: Shopping transactions
      (= merchant-category "retail")
      :shopping

      ;; Rule 7: Bill payments
      (= type :bill-payment)
      :bills

      ;; Rule 8: Default category
      :else
      :other)))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Decision Trees with cond
;;    A decision tree evaluates conditions in order until one matches.
;;    The order is crucial: more specific or high-priority rules must come
;;    first. Here, :urgent and :high-value override category-based rules.
;;    This matches how business rules are typically expressed: "First check X,
;;    then check Y, otherwise Z."
;;
;; 2. Compound Conditions with and
;;    Some rules require multiple conditions to be true simultaneously.
;;    (and (= type :transfer) (> amount 1000)) means BOTH conditions must hold.
;;    This allows expressing complex business rules like "transfers over $1000".
;;
;; 3. Priority Ordering in Business Rules
;;    Real-world business rules have implicit priorities:
;;    - Security/urgency flags (highest priority)
;;    - Value thresholds (high priority)
;;    - Category classifications (medium priority)
;;    - Type-based rules (lower priority)
;;    - Default cases (lowest priority)
;;    Implementing these correctly requires careful ordering in cond.
;;
;; 4. Keyword vs String Comparisons
;;    Note that :type and :merchant-category use different comparison types:
;;    - :type is compared with = against keywords (:transfer, :bill-payment)
;;    - :merchant-category is compared against strings ("travel", "restaurant")
;;    This reflects real data: internal types are keywords, external categories
;;    are strings from merchant databases.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo3.md
;;
;; Pattern used: Multi-branch cond for business rule evaluation
;;
;; Real-world usage: The reference code shows similar decision trees:
;;   (cond
;;     (and (= :low risk-rating) (= :fast-analysis-queue risk-reason))
;;     true
;;     (= :medium risk-rating)
;;     (some #{risk-reason} [:company-matching :company-user :company-owner])
;;     (and (= :high risk-rating) (nil? risk-reason))
;;     false
;;     ...)
;;
;; This demonstrates how production systems use cond to evaluate complex
;; business logic with multiple conditions and priorities. The pattern is
;; essential for fraud detection, compliance checking, and automated
;; decision-making in financial systems.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Restaurant purchase (dining category)
  (categorize-transaction {:type :purchase
                           :amount 100
                           :merchant-category "restaurant"
                           :priority false})
  ;; => :dining

  ;; Example 2: Large transfer (high-value)
  (categorize-transaction {:type :transfer
                           :amount 10000
                           :merchant-category "bank"
                           :priority false})
  ;; => :high-value

  ;; Example 3: Priority flag overrides category
  (categorize-transaction {:type :purchase
                           :amount 50
                           :merchant-category "other"
                           :priority true})
  ;; => :urgent

  ;; Example 4: Bill payment
  (categorize-transaction {:type :bill-payment
                           :amount 200
                           :merchant-category "utilities"
                           :priority false})
  ;; => :bills

  ;; Example 5: Travel transaction
  (categorize-transaction {:type :purchase
                           :amount 500
                           :merchant-category "travel"
                           :priority false})
  ;; => :travel

  ;; Example 6: Shopping
  (categorize-transaction {:type :purchase
                           :amount 75
                           :merchant-category "retail"
                           :priority false})
  ;; => :shopping

  ;; Example 7: Transfer over 1000 (high-value by rule 3)
  (categorize-transaction {:type :transfer
                           :amount 1500
                           :merchant-category "bank"
                           :priority false})
  ;; => :high-value

  ;; Example 8: Generic transaction (other)
  (categorize-transaction {:type :purchase
                           :amount 25
                           :merchant-category "gas-station"
                           :priority false})
  ;; => :other
)

;; TESTS
;; -----

(defn -test []
  ;; Test priority flag (highest priority)
  (assert (= (categorize-transaction {:type :purchase :amount 50 :merchant-category "other" :priority true})
             :urgent)
          "Priority flag should override all other rules")

  ;; Test high-value by amount
  (assert (= (categorize-transaction {:type :purchase :amount 10000 :merchant-category "bank" :priority false})
             :high-value)
          "Amount > 5000 should be high-value")

  ;; Test high-value transfer (compound rule)
  (assert (= (categorize-transaction {:type :transfer :amount 1500 :merchant-category "bank" :priority false})
             :high-value)
          "Transfer > 1000 should be high-value")

  ;; Test merchant categories
  (assert (= (categorize-transaction {:type :purchase :amount 100 :merchant-category "travel" :priority false})
             :travel)
          "Travel category should be recognized")
  (assert (= (categorize-transaction {:type :purchase :amount 100 :merchant-category "restaurant" :priority false})
             :dining)
          "Restaurant category should be dining")
  (assert (= (categorize-transaction {:type :purchase :amount 100 :merchant-category "retail" :priority false})
             :shopping)
          "Retail category should be shopping")

  ;; Test bill payment
  (assert (= (categorize-transaction {:type :bill-payment :amount 200 :merchant-category "utilities" :priority false})
             :bills)
          "Bill payment type should be bills")

  ;; Test default case
  (assert (= (categorize-transaction {:type :purchase :amount 25 :merchant-category "gas-station" :priority false})
             :other)
          "Unmatched transaction should be other")

  (println "✓ All tests passed!"))

;; Run: (-test)
