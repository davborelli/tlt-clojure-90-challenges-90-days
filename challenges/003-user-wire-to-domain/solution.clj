;; =============================================================================
;; 003 - USER WIRE TO DOMAIN
;; Level: 1/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This adapter function transforms data from an external API format (wire format)
;; to our internal domain format. The transformation involves two key changes:
;; 1. Converting camelCase keys to kebab-case (Clojure convention)
;; 2. Renaming :emailAddress to the shorter :email
;;
;; We use destructuring to extract values from the input map and then construct
;; a new map with the transformed keys. This is a common pattern in Clojure
;; applications that integrate with external APIs or services.

(ns challenge-003.solution)

;; IMPLEMENTATION
;; --------------

(defn wire->domain
  "Transforms a user map from wire format to domain format.

  Wire format uses camelCase keys (:firstName, :lastName, :emailAddress)
  Domain format uses kebab-case keys (:first-name, :last-name, :email)

  Parameters:
  - user-wire: Map with wire format keys

  Returns: Map with domain format keys"
  [user-wire]
  ;; Destructure the input map to extract the three fields
  (let [{:keys [firstName lastName emailAddress]} user-wire]
    ;; Build the output map with transformed keys
    {:first-name firstName
     :last-name lastName
     :email emailAddress}))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Map Destructuring with :keys
;;    Destructuring allows us to extract values from a map in a concise way.
;;    The :keys form is used when the local variable names match the keywords:
;;
;;    (let [{:keys [firstName lastName]} user-wire]
;;      ;; Now firstName and lastName are bound to their values
;;      firstName)
;;
;;    This is equivalent to:
;;    (let [firstName (:firstName user-wire)
;;          lastName (:lastName user-wire)]
;;      firstName)
;;
;; 2. Map Construction with Literal Syntax
;;    We build the output map using the literal map syntax {:key value}.
;;    This is clear, idiomatic, and efficient. The keys are keywords (starting
;;    with :) and the values come from the destructured variables.
;;
;; 3. Adapter Pattern
;;    Adapters are functions that transform data between different formats.
;;    They're crucial in real applications for:
;;    - Converting external API responses to internal models
;;    - Transforming domain objects before sending to external services
;;    - Maintaining separation between external contracts and internal design
;;
;;    Key principles:
;;    - Pure functions (no side effects)
;;    - One-way or bidirectional transformations
;;    - Preserve semantic meaning while changing structure
;;
;; 4. Naming Conventions
;;    - camelCase: Common in JavaScript, Java APIs (firstName, emailAddress)
;;    - kebab-case: Clojure convention for keywords and symbols (first-name, email)
;;    - The -> in function name indicates transformation direction (wire->domain)

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo4.md
;;
;; Pattern used: Direct field mapping adapter (wire->*)
;;
;; Real-world usage: In production systems, adapters like this are used
;; extensively to transform data between:
;; - HTTP request bodies → Domain models
;; - Domain models → Database records
;; - Internal format → External API payloads
;;
;; The reference shows similar patterns like:
;; - wire->account: Transforms account data from API format
;; - wire->customer: Transforms customer data from API format

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Standard user transformation
  (wire->domain {:firstName "John"
                 :lastName "Doe"
                 :emailAddress "john@example.com"})
  ;; => {:first-name "John" :last-name "Doe" :email "john@example.com"}

  ;; Example 2: Different user
  (wire->domain {:firstName "Jane"
                 :lastName "Smith"
                 :emailAddress "jane@example.com"})
  ;; => {:first-name "Jane" :last-name "Smith" :email "jane@example.com"}

  ;; Example 3: User with shorter names
  (wire->domain {:firstName "Bob"
                 :lastName "Johnson"
                 :emailAddress "bob@test.com"})
  ;; => {:first-name "Bob" :last-name "Johnson" :email "bob@test.com"}

  ;; Example 4: User with single letter names
  (wire->domain {:firstName "A"
                 :lastName "B"
                 :emailAddress "ab@co.uk"})
  ;; => {:first-name "A" :last-name "B" :email "ab@co.uk"}

  ;; Example 5: User with hyphenated last name
  (wire->domain {:firstName "Mary"
                 :lastName "Anne-Smith"
                 :emailAddress "mary@example.org"})
  ;; => {:first-name "Mary" :last-name "Anne-Smith" :email "mary@example.org"}

  ;; Example 6: User with international characters
  (wire->domain {:firstName "José"
                 :lastName "García"
                 :emailAddress "jose@example.es"})
  ;; => {:first-name "José" :last-name "García" :email "jose@example.es"}
)

;; TESTS
;; -----

(defn -test []
  (assert (= (wire->domain {:firstName "John"
                            :lastName "Doe"
                            :emailAddress "john@example.com"})
             {:first-name "John"
              :last-name "Doe"
              :email "john@example.com"})
          "Standard user transformation should work")

  (assert (= (wire->domain {:firstName "Jane"
                            :lastName "Smith"
                            :emailAddress "jane@example.com"})
             {:first-name "Jane"
              :last-name "Smith"
              :email "jane@example.com"})
          "Different user should transform correctly")

  (assert (= (wire->domain {:firstName "Bob"
                            :lastName "Johnson"
                            :emailAddress "bob@test.com"})
             {:first-name "Bob"
              :last-name "Johnson"
              :email "bob@test.com"})
          "User with different email domain should work")

  (assert (= (wire->domain {:firstName "A"
                            :lastName "B"
                            :emailAddress "ab@co.uk"})
             {:first-name "A"
              :last-name "B"
              :email "ab@co.uk"})
          "Single letter names should work")

  (assert (= (wire->domain {:firstName "Mary"
                            :lastName "Anne-Smith"
                            :emailAddress "mary@example.org"})
             {:first-name "Mary"
              :last-name "Anne-Smith"
              :email "mary@example.org"})
          "Hyphenated last name should be preserved")

  (assert (= (wire->domain {:firstName "José"
                            :lastName "García"
                            :emailAddress "jose@example.es"})
             {:first-name "José"
              :last-name "García"
              :email "jose@example.es"})
          "International characters should be preserved")

  (println "✓ All tests passed! The wire->domain function works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
