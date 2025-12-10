;; =============================================================================
;; 034 - COERCE USER DATA TYPES
;; Level: 7/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This adapter performs multiple type coercions on a user data map, converting
;; string values from external sources into appropriate Clojure types for
;; internal processing. Each field requires a different coercion strategy.
;;
;; The approach destructures the input map to get string values, applies the
;; appropriate conversion function to each field, then reconstructs the map
;; with typed values. This is a fundamental pattern in wire→domain adapters.
;;
;; This pattern is extremely common in production systems that receive form
;; data, CSV imports, or API responses where all values are initially strings
;; and need to be converted to proper types for validation and business logic.

(ns challenge-034.solution)

;; IMPLEMENTATION
;; --------------

(defn coerce-user-types
  "Transforms user data from strings to appropriate types.

  Parameters:
  - raw-user: Map with string values for all fields

  Returns: Map with properly typed values"
  [raw-user]
  ;; Destructure to get each field as a string
  (let [{:keys [name age active role]} raw-user]
    ;; Rebuild map with type coercions applied
    {:name   name                         ; Keep as string
     :age    (Integer/parseInt age)       ; String → Integer
     :active (= active "true")            ; String → Boolean
     :role   (keyword role)}))            ; String → Keyword

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Multiple Type Coercions
;;    Different fields require different coercion strategies:
;;    - Strings stay as strings (identity transformation)
;;    - Numeric strings → integers (Integer/parseInt)
;;    - Boolean strings → booleans (comparison with "true")
;;    - String names → keywords (keyword function)
;;
;; 2. Boolean Coercion Pattern
;;    (= active "true") converts "true" → true, anything else → false.
;;    Alternative: (Boolean/parseBoolean active) - but = pattern is clearer.
;;    This handles the common pattern where forms send "true"/"false" strings.
;;
;; 3. Keyword Coercion
;;    The keyword function converts strings to keywords: "admin" → :admin.
;;    Keywords are preferred for enumerated values (roles, statuses, types)
;;    because they're more efficient for lookups and comparisons.
;;
;; 4. Destructuring with :keys
;;    {:keys [name age active role]} is shorthand for binding map values.
;;    Equivalent to: (let [name (:name raw-user) age (:age raw-user) ...])
;;    This makes the code more concise and readable.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo5.md
;;
;; Pattern used: Multiple type coercions in wire→domain transformation
;;
;; Real-world usage: The reference code shows similar multi-field coercion:
;;   {:amount (BigDecimal. amount-str)
;;    :status (keyword status-str)
;;    :active (Boolean/parseBoolean active-str)}
;;
;; This demonstrates how production adapters handle external data that arrives
;; as strings (from databases, APIs, forms) and needs to be converted to
;; strongly-typed domain models for business logic processing.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Active admin user
  (coerce-user-types {:name "John" :age "25" :active "true" :role "admin"})
  ;; => {:name "John", :age 25, :active true, :role :admin}

  ;; Example 2: Inactive regular user
  (coerce-user-types {:name "Jane" :age "30" :active "false" :role "user"})
  ;; => {:name "Jane", :age 30, :active false, :role :user}

  ;; Example 3: Active moderator
  (coerce-user-types {:name "Bob" :age "45" :active "true" :role "moderator"})
  ;; => {:name "Bob", :age 45, :active true, :role :moderator}

  ;; Example 4: Inactive admin
  (coerce-user-types {:name "Alice" :age "35" :active "false" :role "admin"})
  ;; => {:name "Alice", :age 35, :active false, :role :admin}
)

;; TESTS
;; -----

(defn -test []
  (assert (= (coerce-user-types {:name "John" :age "25" :active "true" :role "admin"})
             {:name "John" :age 25 :active true :role :admin})
          "Should coerce all types for active admin")
  (assert (= (coerce-user-types {:name "Jane" :age "30" :active "false" :role "user"})
             {:name "Jane" :age 30 :active false :role :user})
          "Should coerce all types for inactive user")
  (assert (= (coerce-user-types {:name "Bob" :age "45" :active "true" :role "moderator"})
             {:name "Bob" :age 45 :active true :role :moderator})
          "Should coerce all types for active moderator")
  ;; Test individual type coercions
  (let [result (coerce-user-types {:name "Test" :age "18" :active "false" :role "guest"})]
    (assert (string? (:name result)) "Name should be string")
    (assert (integer? (:age result)) "Age should be integer")
    (assert (boolean? (:active result)) "Active should be boolean")
    (assert (keyword? (:role result)) "Role should be keyword"))
  (println "✓ All tests passed!"))

;; Run: (-test)
