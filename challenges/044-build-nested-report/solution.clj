;; =============================================================================
;; 044 - BUILD NESTED REPORT
;; Level: 9/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This adapter performs the reverse transformation of typical adapters:
;; instead of flattening nested structures, it builds hierarchy from flat data.
;; We group related fields and calculate summary values for reporting.
;;
;; The approach destructures the flat input, calculates the net amount, then
;; constructs a nested structure by grouping related fields into logical
;; sections (financial, user, merchant). This makes the report more readable
;; and suitable for hierarchical serialization (JSON, XML, etc.).
;;
;; This pattern is common in reporting systems, API response builders, and
;; ETL pipelines that transform database records into structured documents
;; for external consumption.

(ns challenge-044.solution)

;; IMPLEMENTATION
;; --------------

(defn build-report
  "Transforms flat transaction record into nested report structure.

  Parameters:
  - flat-record: Flat map with all transaction fields

  Returns: Nested report map with calculated fields"
  [flat-record]
  (let [{:keys [transaction-id amount fee
                user-name user-email
                merchant-name merchant-category]} flat-record
        ;; Calculate net amount (amount - fee)
        net-amount (- amount fee)]
    {:id transaction-id
     :financial {:amount amount
                 :fee fee
                 :net net-amount}
     :user {:name user-name
            :email user-email}
     :merchant {:name merchant-name
                :category merchant-category}}))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Reverse Transformation: Flat → Nested
;;    Most adapters flatten nested external data to flat internal format.
;;    This adapter does the opposite: takes flat data (from database/storage)
;;    and builds nested structure (for APIs/reports). This is essential when:
;;    - Serving JSON APIs (clients expect nested objects)
;;    - Generating reports (hierarchy improves readability)
;;    - Integrating with external systems (they expect nested format)
;;
;; 2. Logical Grouping of Fields
;;    We group related fields into nested maps:
;;    - :financial - all money-related fields
;;    - :user - all customer information
;;    - :merchant - all merchant information
;;    This improves readability and makes the structure self-documenting.
;;
;; 3. Calculated Fields in Reports
;;    Reports often include derived values not stored in raw data:
;;    - net = amount - fee
;;    - total = subtotal + tax
;;    - profit = revenue - cost
;;    Adapters are appropriate for simple calculations like this. Complex
;;    business logic should be in domain functions.
;;
;; 4. Denormalization for Reports
;;    Databases normalize data (user-name, user-email as separate columns).
;;    Reports denormalize (group into user object). This trades:
;;    + Readability and structure
;;    + API usability (nested JSON)
;;    - Some redundancy in representation

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo1.md, exemplo2.md
;;
;; Pattern used: Building nested structures from flat data (reverse flatten)
;;
;; Real-world usage: While references show mostly flatten operations, the
;; reverse pattern is equally important. In production systems:
;;
;;   ;; Database record (flat)
;;   {:order_id 123 :user_name "John" :user_email "..." :total 100}
;;
;;   ;; API response (nested)
;;   {:order-id 123
;;    :user {:name "John" :email "..."}
;;    :payment {:total 100}}
;;
;; This transformation is essential for REST APIs, GraphQL resolvers, and
;; report generation where hierarchical structure improves usability.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Dining transaction
  (build-report
    {:transaction-id "TXN-123"
     :amount 100.00
     :fee 2.50
     :user-name "John Doe"
     :user-email "john@example.com"
     :merchant-name "Coffee Shop"
     :merchant-category "dining"})
  ;; => {:id "TXN-123"
  ;;     :financial {:amount 100.0, :fee 2.5, :net 97.5}
  ;;     :user {:name "John Doe", :email "john@example.com"}
  ;;     :merchant {:name "Coffee Shop", :category "dining"}}

  ;; Example 2: Retail transaction
  (build-report
    {:transaction-id "TXN-456"
     :amount 1500.00
     :fee 45.00
     :user-name "Jane Smith"
     :user-email "jane@example.com"
     :merchant-name "Electronics Store"
     :merchant-category "retail"})
  ;; => {:id "TXN-456"
  ;;     :financial {:amount 1500.0, :fee 45.0, :net 1455.0}
  ;;     :user {:name "Jane Smith", :email "jane@example.com"}
  ;;     :merchant {:name "Electronics Store", :category "retail"}}

  ;; Example 3: Zero fee transaction
  (build-report
    {:transaction-id "TXN-789"
     :amount 50.00
     :fee 0.00
     :user-name "Bob Wilson"
     :user-email "bob@example.com"
     :merchant-name "Gas Station"
     :merchant-category "fuel"})
  ;; => {:financial {:net 50.0}}  ; Net equals amount when fee is 0
)

;; TESTS
;; -----

(defn -test []
  (let [result (build-report
                 {:transaction-id "TXN-123"
                  :amount 100.00
                  :fee 2.50
                  :user-name "John Doe"
                  :user-email "john@example.com"
                  :merchant-name "Coffee Shop"
                  :merchant-category "dining"})]
    ;; Test top-level structure
    (assert (= (:id result) "TXN-123")
            "Should extract transaction ID")
    ;; Test financial nested map
    (assert (= (get-in result [:financial :amount]) 100.0)
            "Should include amount in financial")
    (assert (= (get-in result [:financial :fee]) 2.5)
            "Should include fee in financial")
    (assert (= (get-in result [:financial :net]) 97.5)
            "Should calculate net correctly")
    ;; Test user nested map
    (assert (= (get-in result [:user :name]) "John Doe")
            "Should group user name")
    (assert (= (get-in result [:user :email]) "john@example.com")
            "Should group user email")
    ;; Test merchant nested map
    (assert (= (get-in result [:merchant :name]) "Coffee Shop")
            "Should group merchant name")
    (assert (= (get-in result [:merchant :category]) "dining")
            "Should group merchant category"))

  ;; Test different transaction
  (let [result (build-report
                 {:transaction-id "TXN-456"
                  :amount 1500.00
                  :fee 45.00
                  :user-name "Jane Smith"
                  :user-email "jane@example.com"
                  :merchant-name "Electronics Store"
                  :merchant-category "retail"})]
    (assert (= (:id result) "TXN-456")
            "Should handle different transaction")
    (assert (= (get-in result [:financial :net]) 1455.0)
            "Should calculate net for larger amounts"))

  ;; Test zero fee
  (let [result (build-report
                 {:transaction-id "TXN-789"
                  :amount 50.00
                  :fee 0.00
                  :user-name "Bob Wilson"
                  :user-email "bob@example.com"
                  :merchant-name "Gas Station"
                  :merchant-category "fuel"})]
    (assert (= (get-in result [:financial :net]) 50.0)
            "Should handle zero fee correctly"))

  (println "✓ All tests passed!"))

;; Run: (-test)
