;; =============================================================================
;; 009 - REMOVE SENSITIVE FIELDS
;; Level: 2/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This adapter demonstrates data sanitization by removing sensitive fields.
;; This is a critical security pattern when:
;; - Sending user data to clients (frontend applications)
;; - Logging data (never log passwords or SSNs)
;; - Exporting data for analytics
;; - Creating public APIs
;;
;; We use dissoc (dissociate) to remove keys from maps. It's the opposite of
;; assoc - while assoc adds/updates keys, dissoc removes them. Like all Clojure
;; operations, it returns a new map without modifying the original.

(ns challenge-009.solution)

;; IMPLEMENTATION
;; --------------

(defn remove-sensitive
  "Removes sensitive fields (password and SSN) from a user map.

  This function sanitizes user data by removing fields that should never
  be sent to clients or external systems.

  Parameters:
  - user: Map with user information including sensitive fields

  Returns: New map with :password and :ssn removed"
  [user]
  ;; Use dissoc to remove both :password and :ssn keys
  (dissoc user :password :ssn))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. dissoc Function
;;    dissoc (dissociate) removes keys from a map.
;;
;;    Syntax: (dissoc map key)
;;           (dissoc map key1 key2 key3 ...)
;;
;;    Examples:
;;    (dissoc {:a 1 :b 2 :c 3} :b)
;;    ;; => {:a 1 :c 3}
;;
;;    Multiple keys at once:
;;    (dissoc {:a 1 :b 2 :c 3} :a :c)
;;    ;; => {:b 2}
;;
;;    If key doesn't exist, no error:
;;    (dissoc {:a 1} :b)
;;    ;; => {:a 1}
;;
;;    Remove all keys:
;;    (dissoc {:a 1 :b 2} :a :b)
;;    ;; => {}
;;
;; 2. assoc vs dissoc
;;    These are complementary operations:
;;
;;    assoc: Adds or updates keys
;;    (assoc {} :a 1)           ;; => {:a 1}
;;    (assoc {:a 1} :a 2)       ;; => {:a 2}
;;
;;    dissoc: Removes keys
;;    (dissoc {:a 1 :b 2} :a)   ;; => {:b 2}
;;
;;    Together they provide complete map manipulation:
;;    (-> {}
;;        (assoc :a 1 :b 2 :c 3)    ;; Add keys
;;        (dissoc :b))               ;; Remove key
;;    ;; => {:a 1 :c 3}
;;
;; 3. Data Sanitization Pattern
;;    Removing sensitive data is crucial for:
;;
;;    a) Security:
;;       - Never send passwords to clients
;;       - Never log SSNs, credit cards, etc.
;;       - Follow principle of least privilege
;;
;;    b) Privacy:
;;       - GDPR, CCPA compliance
;;       - Only share necessary data
;;       - Audit what data is exposed
;;
;;    c) API design:
;;       - Different views of same entity
;;       - Public vs. internal representations
;;       - Role-based field filtering
;;
;; 4. Common Sensitive Fields
;;    Fields that should typically be sanitized:
;;    - Authentication: :password, :password-hash, :api-key, :token
;;    - Personal: :ssn, :tax-id, :passport-number
;;    - Financial: :credit-card, :bank-account, :cvv
;;    - Internal: :internal-id, :database-id, :audit-log
;;
;;    Always sanitize before:
;;    - Sending to frontend
;;    - Writing to logs
;;    - Exporting to files
;;    - Sending to third parties

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo4.md
;;
;; Pattern used: Field removal for data sanitization
;;
;; Real-world usage: Removing fields is essential in:
;; - API response preparation (hide internal fields)
;; - Logging (exclude sensitive data)
;; - Data export (privacy compliance)
;; - Inter-service communication (send only needed data)
;;
;; The reference code shows adapters transforming data between representations,
;; and this often involves selectively including/excluding fields based on
;; context and security requirements.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Full user record with sensitive data
  (remove-sensitive {:name "John"
                     :email "john@example.com"
                     :password "secret123"
                     :ssn "123-45-6789"
                     :age 30})
  ;; => {:name "John" :email "john@example.com" :age 30}

  ;; Example 2: Different user
  (remove-sensitive {:name "Jane"
                     :email "jane@example.com"
                     :password "pass456"
                     :ssn "987-65-4321"
                     :age 25})
  ;; => {:name "Jane" :email "jane@example.com" :age 25}

  ;; Example 3: Another user
  (remove-sensitive {:name "Bob"
                     :email "bob@test.com"
                     :password "mypass"
                     :ssn "111-22-3333"
                     :age 40})
  ;; => {:name "Bob" :email "bob@test.com" :age 40}

  ;; Example 4: User with extra fields
  (remove-sensitive {:name "Alice"
                     :email "alice@example.com"
                     :password "secret"
                     :ssn "999-88-7777"
                     :age 28
                     :city "New York"
                     :role :admin})
  ;; => {:name "Alice" :email "alice@example.com" :age 28 :city "New York" :role :admin}

  ;; Example 5: Demonstrating immutability
  (def original-user {:name "Charlie"
                      :email "charlie@example.com"
                      :password "pass123"
                      :ssn "555-44-3333"
                      :age 35})
  (def sanitized-user (remove-sensitive original-user))
  ;; original-user still has :password and :ssn
  ;; sanitized-user does not

  ;; Example 6: If sensitive fields don't exist, no error
  (remove-sensitive {:name "Dave"
                     :email "dave@example.com"
                     :age 50})
  ;; => {:name "Dave" :email "dave@example.com" :age 50}

  ;; Example 7: Using in a pipeline
  (->> {:name "Eve"
        :email "eve@example.com"
        :password "secret"
        :ssn "123-45-6789"
        :age 30
        :internal-id "xyz123"}
       (remove-sensitive)
       (dissoc :internal-id))  ;; Remove more fields
  ;; => {:name "Eve" :email "eve@example.com" :age 30}
)

;; TESTS
;; -----

(defn -test []
  (assert (= (remove-sensitive {:name "John"
                                :email "john@example.com"
                                :password "secret123"
                                :ssn "123-45-6789"
                                :age 30})
             {:name "John"
              :email "john@example.com"
              :age 30})
          "Should remove :password and :ssn fields")

  (assert (= (remove-sensitive {:name "Jane"
                                :email "jane@example.com"
                                :password "pass456"
                                :ssn "987-65-4321"
                                :age 25})
             {:name "Jane"
              :email "jane@example.com"
              :age 25})
          "Should work for different users")

  (assert (= (remove-sensitive {:name "Bob"
                                :email "bob@test.com"
                                :password "mypass"
                                :ssn "111-22-3333"
                                :age 40})
             {:name "Bob"
              :email "bob@test.com"
              :age 40})
          "Should preserve non-sensitive fields")

  (assert (= (remove-sensitive {:name "Alice"
                                :email "alice@example.com"
                                :password "secret"
                                :ssn "999-88-7777"
                                :age 28
                                :city "New York"
                                :role :admin})
             {:name "Alice"
              :email "alice@example.com"
              :age 28
              :city "New York"
              :role :admin})
          "Should work with extra fields")

  ;; Test immutability
  (let [original {:name "Charlie"
                  :email "charlie@example.com"
                  :password "pass123"
                  :ssn "555-44-3333"
                  :age 35}
        sanitized (remove-sensitive original)]
    (assert (= original {:name "Charlie"
                         :email "charlie@example.com"
                         :password "pass123"
                         :ssn "555-44-3333"
                         :age 35})
            "Should not modify original map")
    (assert (= sanitized {:name "Charlie"
                          :email "charlie@example.com"
                          :age 35})
            "Should return new map without sensitive fields"))

  (assert (= (remove-sensitive {:name "Dave"
                                :email "dave@example.com"
                                :age 50})
             {:name "Dave"
              :email "dave@example.com"
              :age 50})
          "Should work even if sensitive fields don't exist")

  (println "✓ All tests passed! The remove-sensitive function works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
