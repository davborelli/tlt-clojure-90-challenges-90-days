;; =============================================================================
;; 048 - FLATTEN NESTED USER PROFILE
;; Level: 10/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This adapter flattens a deeply nested user profile (3 levels) into a flat
;; map suitable for database storage. We use get-in to safely extract values
;; from nested paths, handling structures like [:contact :email :primary].
;;
;; The approach groups related extractions for readability: first personal
;; info, then email fields, then phone fields, then address fields. This makes
;; the transformation clear and maintainable.
;;
;; This pattern is ubiquitous in production systems that consume external APIs:
;; nested JSON responses are flattened into flat database schemas. Flattening
;; makes data easier to query, validate, and work with in business logic.

(ns challenge-048.solution)

;; IMPLEMENTATION
;; --------------

(defn flatten-user-profile
  "Flattens deeply nested user profile into flat map.

  Parameters:
  - nested-profile: 3-level nested profile from external API

  Returns: Flat map with all nested fields extracted"
  [nested-profile]
  {:id                   (:user-id nested-profile)
   ;; Extract personal fields (level 2)
   :name                 (get-in nested-profile [:personal :name])
   :birthdate            (get-in nested-profile [:personal :birthdate])
   ;; Extract email fields (level 3)
   :email                (get-in nested-profile [:contact :email :primary])
   :email-verified       (get-in nested-profile [:contact :email :verified])
   ;; Extract phone fields (level 3)
   :mobile-phone         (get-in nested-profile [:contact :phone :mobile])
   :phone-country-code   (get-in nested-profile [:contact :phone :country-code])
   ;; Extract address fields (level 3)
   :street               (get-in nested-profile [:contact :address :street])
   :city                 (get-in nested-profile [:contact :address :city])
   :zip                  (get-in nested-profile [:contact :address :zip])
   :country              (get-in nested-profile [:contact :address :country])})

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Flattening Nested Structures
;;    Nested structures organize data hierarchically:
;;      {:contact {:email {:primary "..." :verified ...}}}
;;    Flat structures are simpler:
;;      {:email "..." :email-verified ...}
;;    Flattening trades organization for simplicity. Benefits:
;;    - Easier database mapping (most DBs prefer flat rows)
;;    - Simpler validation (no nested path navigation)
;;    - More convenient for business logic
;;    Trade-off: lose some semantic grouping
;;
;; 2. get-in for Deep Extraction
;;    get-in safely navigates nested maps:
;;      (get-in data [:contact :email :primary])
;;    Equivalent to chaining gets:
;;      (get (get (get data :contact) :email) :primary)
;;    But get-in is more readable and returns nil if any key missing.
;;    Essential for working with nested data from external sources.
;;
;; 3. Three-Level Nesting
;;    This profile has three levels:
;;    - Level 1 (root): :user-id, :personal, :contact
;;    - Level 2: :name, :email, :phone, :address
;;    - Level 3: :primary, :verified, :mobile, :street, ...
;;    get-in handles arbitrary depth: just provide path as vector.
;;
;; 4. Field Name Translation
;;    We rename fields during flattening:
;;    - :user-id → :id
;;    - [:contact :email :primary] → :email
;;    - [:contact :phone :mobile] → :mobile-phone
;;    - [:contact :phone :country-code] → :phone-country-code
;;    This makes the flat structure more convenient to use.
;;
;; 5. Grouping for Readability
;;    We group extractions by category (personal, email, phone, address).
;;    This makes the code easier to read and modify. When adding a new
;;    address field, you know exactly where to add it.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo1.md, exemplo2.md
;;
;; Pattern used: Flattening nested structures with get-in
;;
;; Real-world usage: Production adapters flatten API responses:
;;   (defn api->domain [response]
;;     {:id (get-in response [:data :user :id])
;;      :name (get-in response [:data :user :profile :name])
;;      ...})
;;
;; The references show similar patterns where nested external data is
;; transformed into flat internal format. This is fundamental for API
;; integration, ETL pipelines, and data normalization.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Complete profile
  (flatten-user-profile
    {:user-id "USER-123"
     :personal {:name "Alice Johnson" :birthdate "1990-05-15"}
     :contact {:email {:primary "alice@example.com" :verified true}
               :phone {:mobile "555-0100" :country-code "+1"}
               :address {:street "123 Main St" :city "New York" :zip "10001" :country "USA"}}})
  ;; => {:id "USER-123"
  ;;     :name "Alice Johnson"
  ;;     :birthdate "1990-05-15"
  ;;     :email "alice@example.com"
  ;;     :email-verified true
  ;;     :mobile-phone "555-0100"
  ;;     :phone-country-code "+1"
  ;;     :street "123 Main St"
  ;;     :city "New York"
  ;;     :zip "10001"
  ;;     :country "USA"}

  ;; Example 2: Different user
  (flatten-user-profile
    {:user-id "USER-456"
     :personal {:name "Bob Smith" :birthdate "1985-10-20"}
     :contact {:email {:primary "bob@example.com" :verified false}
               :phone {:mobile "555-0200" :country-code "+44"}
               :address {:street "456 Oak Ave" :city "London" :zip "SW1A 1AA" :country "UK"}}})
  ;; => {:id "USER-456"
  ;;     :name "Bob Smith"
  ;;     :birthdate "1985-10-20"
  ;;     :email "bob@example.com"
  ;;     :email-verified false
  ;;     :mobile-phone "555-0200"
  ;;     :phone-country-code "+44"
  ;;     :street "456 Oak Ave"
  ;;     :city "London"
  ;;     :zip "SW1A 1AA"
  ;;     :country "UK"}
)

;; TESTS
;; -----

(defn -test []
  (let [result (flatten-user-profile
                 {:user-id "USER-123"
                  :personal {:name "Alice Johnson" :birthdate "1990-05-15"}
                  :contact {:email {:primary "alice@example.com" :verified true}
                            :phone {:mobile "555-0100" :country-code "+1"}
                            :address {:street "123 Main St" :city "New York" :zip "10001" :country "USA"}}})]
    ;; Test root extraction
    (assert (= (:id result) "USER-123")
            "Should extract user-id as id")
    ;; Test personal extraction
    (assert (= (:name result) "Alice Johnson")
            "Should extract name from personal")
    (assert (= (:birthdate result) "1990-05-15")
            "Should extract birthdate from personal")
    ;; Test email extraction
    (assert (= (:email result) "alice@example.com")
            "Should extract primary email")
    (assert (true? (:email-verified result))
            "Should extract email verified status")
    ;; Test phone extraction
    (assert (= (:mobile-phone result) "555-0100")
            "Should extract mobile phone")
    (assert (= (:phone-country-code result) "+1")
            "Should extract phone country code")
    ;; Test address extraction
    (assert (= (:street result) "123 Main St")
            "Should extract street from address")
    (assert (= (:city result) "New York")
            "Should extract city from address")
    (assert (= (:zip result) "10001")
            "Should extract zip from address")
    (assert (= (:country result) "USA")
            "Should extract country from address"))

  ;; Test different profile
  (let [result (flatten-user-profile
                 {:user-id "USER-456"
                  :personal {:name "Bob Smith" :birthdate "1985-10-20"}
                  :contact {:email {:primary "bob@example.com" :verified false}
                            :phone {:mobile "555-0200" :country-code "+44"}
                            :address {:street "456 Oak Ave" :city "London" :zip "SW1A 1AA" :country "UK"}}})]
    (assert (= (:id result) "USER-456")
            "Should handle different user")
    (assert (false? (:email-verified result))
            "Should handle unverified email")
    (assert (= (:phone-country-code result) "+44")
            "Should handle different country code")
    (assert (= (:country result) "UK")
            "Should handle different country"))

  (println "✓ All tests passed!"))

;; Run: (-test)
