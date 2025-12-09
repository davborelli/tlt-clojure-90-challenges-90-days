;; =============================================================================
;; 004 - EXTRACT USER FIELDS
;; Level: 1/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This adapter demonstrates field extraction from a larger map. In real-world
;; applications, we often receive objects with many fields but only need a subset.
;; For example, when sending data to a client, we might want to exclude sensitive
;; information like phone numbers or addresses.
;;
;; We use select-keys, which is the idiomatic Clojure way to extract a subset
;; of fields from a map. It's more concise than manual destructuring and clearly
;; expresses the intent of "keep only these keys".

(ns challenge-004.solution)

;; IMPLEMENTATION
;; --------------

(defn extract-contact-info
  "Extracts only contact information (name and email) from a user map.

  This function creates a subset of the input map containing only the
  fields needed for basic contact purposes, filtering out sensitive
  or unnecessary information.

  Parameters:
  - user: Map with user information including name, email, and other fields

  Returns: Map containing only :name and :email keys"
  [user]
  ;; Use select-keys to extract only the fields we need
  (select-keys user [:name :email]))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. select-keys Function
;;    select-keys is a core Clojure function for extracting a subset of keys
;;    from a map. It takes two arguments:
;;    - A map to extract from
;;    - A collection (usually a vector) of keys to keep
;;
;;    Example:
;;    (select-keys {:a 1 :b 2 :c 3} [:a :c])
;;    ;; => {:a 1 :c 3}
;;
;;    If a key doesn't exist in the map, it won't appear in the result:
;;    (select-keys {:a 1 :b 2} [:a :c])
;;    ;; => {:a 1}
;;
;; 2. Subset Extraction Pattern
;;    Creating subsets of maps is a common operation in:
;;    - API responses (only send needed fields to client)
;;    - Data privacy (exclude sensitive fields)
;;    - Performance optimization (reduce data size)
;;    - Interface segregation (different views of same data)
;;
;; 3. When to Use select-keys vs Destructuring
;;    Use select-keys when:
;;    - You want to keep multiple keys as a map
;;    - The number of keys might change
;;    - You're passing the result to another function
;;
;;    Use destructuring when:
;;    - You need to work with individual values
;;    - You're transforming or computing new values
;;    - You need to rename fields
;;
;; 4. Map Operations are Pure
;;    select-keys returns a new map without modifying the original.
;;    This immutability is a core principle of functional programming
;;    that makes code easier to reason about and test.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo4.md
;;
;; Pattern used: Field extraction from larger data structures
;;
;; Real-world usage: In production code, this pattern appears when:
;; - Preparing data for API responses (hiding internal fields)
;; - Creating different "views" of the same entity
;; - Implementing the "need to know" principle for data access
;; - Optimizing data transfer between services

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Full user record with all fields
  (extract-contact-info {:name "John Doe"
                         :email "john@example.com"
                         :age 30
                         :address "123 Main St"
                         :phone "555-1234"})
  ;; => {:name "John Doe" :email "john@example.com"}

  ;; Example 2: Another user
  (extract-contact-info {:name "Jane Smith"
                         :email "jane@example.com"
                         :age 25
                         :address "456 Oak Ave"
                         :phone "555-5678"})
  ;; => {:name "Jane Smith" :email "jane@example.com"}

  ;; Example 3: User with minimal data
  (extract-contact-info {:name "Bob"
                         :email "bob@test.com"
                         :age 40
                         :address "789 Pine Rd"
                         :phone "555-9999"})
  ;; => {:name "Bob" :email "bob@test.com"}

  ;; Example 4: Map with extra fields is still filtered correctly
  (extract-contact-info {:name "Alice"
                         :email "alice@example.com"
                         :age 28
                         :address "321 Elm St"
                         :phone "555-1111"
                         :ssn "123-45-6789"
                         :credit-card "1234-5678-9012-3456"})
  ;; => {:name "Alice" :email "alice@example.com"}

  ;; Example 5: If a key is missing, it won't appear in result
  (extract-contact-info {:name "Charlie"
                         :age 35})
  ;; => {:name "Charlie"}
  ;; Note: :email is missing from result because it wasn't in input

  ;; Example 6: Empty name or email values are still included
  (extract-contact-info {:name ""
                         :email ""
                         :age 50
                         :phone "555-2222"})
  ;; => {:name "" :email ""}
)

;; TESTS
;; -----

(defn -test []
  (assert (= (extract-contact-info {:name "John Doe"
                                    :email "john@example.com"
                                    :age 30
                                    :address "123 Main St"
                                    :phone "555-1234"})
             {:name "John Doe"
              :email "john@example.com"})
          "Should extract only name and email from full user record")

  (assert (= (extract-contact-info {:name "Jane Smith"
                                    :email "jane@example.com"
                                    :age 25
                                    :address "456 Oak Ave"
                                    :phone "555-5678"})
             {:name "Jane Smith"
              :email "jane@example.com"})
          "Should work for different user data")

  (assert (= (extract-contact-info {:name "Bob Johnson"
                                    :email "bob@test.com"
                                    :age 40
                                    :address "789 Pine Rd"
                                    :phone "555-9999"})
             {:name "Bob Johnson"
              :email "bob@test.com"})
          "Should filter out all non-contact fields")

  (assert (= (extract-contact-info {:name "Alice"
                                    :email "alice@example.com"
                                    :age 28
                                    :ssn "123-45-6789"
                                    :credit-card "1234-5678-9012-3456"})
             {:name "Alice"
              :email "alice@example.com"})
          "Should exclude sensitive fields like SSN and credit card")

  (assert (= (extract-contact-info {:name "Charlie"
                                    :age 35})
             {:name "Charlie"})
          "Should only include keys that exist in input")

  (assert (= (extract-contact-info {:name ""
                                    :email ""
                                    :age 50})
             {:name ""
              :email ""})
          "Should preserve empty string values")

  (println "✓ All tests passed! The extract-contact-info function works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
