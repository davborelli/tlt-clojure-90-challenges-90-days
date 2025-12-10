;; =============================================================================
;; 069 - FORMAT CONVERTER
;; Level: 14/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This adapter provides multi-format conversion for user records across four
;; different representations: domain (internal), database, API, and CSV. Each
;; format has its own naming conventions and data types.
;;
;; The approach uses domain format as the canonical intermediate representation.
;; To convert between any two formats, we normalize to domain first, then
;; transform to the target format. This reduces the number of conversion
;; functions needed (4 to/from domain instead of 12 direct conversions).
;;
;; This pattern is fundamental in systems integrating multiple external services,
;; databases, and APIs where data must flow between different representations.

(ns challenge-069.solution)

;; HELPER FUNCTIONS - TO DOMAIN
;; -----------------------------

(defn database->domain
  "Converts database format to domain format.

  Transformations:
  - :id → :user-id
  - :name → :full-name
  - :email_address → :email
  - :is_active (1/0) → :active (boolean)

  Parameters:
  - db-record: Database format map

  Returns: Domain format map"
  [db-record]
  {:user-id (:id db-record)
   :full-name (:name db-record)
   :email (:email_address db-record)
   :active (= (:is_active db-record) 1)})

(defn api->domain
  "Converts API format to domain format.

  Transformations:
  - :userId → :user-id
  - :fullName → :full-name
  - :email → :email
  - :status (\"active\"/\"inactive\") → :active (boolean)

  Parameters:
  - api-record: API format map

  Returns: Domain format map"
  [api-record]
  {:user-id (:userId api-record)
   :full-name (:fullName api-record)
   :email (:email api-record)
   :active (= (:status api-record) "active")})

;; HELPER FUNCTIONS - FROM DOMAIN
;; -------------------------------

(defn domain->database
  "Converts domain format to database format.

  Transformations:
  - :user-id → :id
  - :full-name → :name
  - :email → :email_address
  - :active (boolean) → :is_active (1/0)

  Parameters:
  - domain-record: Domain format map

  Returns: Database format map"
  [domain-record]
  {:id (:user-id domain-record)
   :name (:full-name domain-record)
   :email_address (:email domain-record)
   :is_active (if (:active domain-record) 1 0)})

(defn domain->api
  "Converts domain format to API format.

  Transformations:
  - :user-id → :userId
  - :full-name → :fullName
  - :email → :email
  - :active (boolean) → :status (\"active\"/\"inactive\")

  Parameters:
  - domain-record: Domain format map

  Returns: API format map"
  [domain-record]
  {:userId (:user-id domain-record)
   :fullName (:full-name domain-record)
   :email (:email domain-record)
   :status (if (:active domain-record) "active" "inactive")})

(defn domain->csv
  "Converts domain format to CSV string.

  Format: \"user_id,full_name,email,active\"

  Parameters:
  - domain-record: Domain format map

  Returns: CSV string"
  [domain-record]
  (str (:user-id domain-record) ","
       (:full-name domain-record) ","
       (:email domain-record) ","
       (:active domain-record)))

;; MAIN IMPLEMENTATION
;; -------------------

(defn convert-format
  "Converts user record between different formats.

  Supported formats: :domain, :database, :api, :csv
  Uses domain as intermediate format for conversions.

  Parameters:
  - user-record: User data in source format
  - source-format: Format of input (:domain, :database, :api, :csv)
  - target-format: Desired output format

  Returns: Converted record in target format"
  [user-record source-format target-format]
  (if (= source-format target-format)
    user-record
    ;; Convert source → domain → target
    (let [domain-record (case source-format
                          :domain user-record
                          :database (database->domain user-record)
                          :api (api->domain user-record))]
      (case target-format
        :domain domain-record
        :database (domain->database domain-record)
        :api (domain->api domain-record)
        :csv (domain->csv domain-record)))))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Multi-Format Conversion Pattern
;;    Direct conversions between N formats require N*(N-1) functions.
;;    For 4 formats: 4*3 = 12 conversion functions.
;;
;;    Using a canonical intermediate (domain) reduces this:
;;    - 3 to-domain converters (database, api, csv)
;;    - 3 from-domain converters (database, api, csv)
;;    Total: 6 functions (+ identity for domain↔domain)
;;
;;    Benefits:
;;    - Fewer functions to maintain
;;    - Single source of truth (domain format)
;;    - Easier to add new formats (just 2 functions: to/from domain)
;;
;; 2. Field Name Conventions
;;    Each format has its own conventions:
;;    - Domain: kebab-case keywords (:full-name)
;;    - Database: snake_case keywords (:email_address)
;;    - API: camelCase keywords (:fullName)
;;    - CSV: snake_case strings
;;
;;    Respecting conventions makes integration seamless.
;;
;; 3. Type Transformations
;;    Active status has different representations:
;;    - Domain: boolean (true/false)
;;    - Database: integer (1/0)
;;    - API: string ("active"/"inactive")
;;    - CSV: string ("true"/"false")
;;
;;    Each conversion handles the appropriate transformation.
;;
;; 4. Canonical Intermediate Format
;;    Domain format is the "truth" representation:
;;    - Idiomatic Clojure (kebab-case keywords)
;;    - Semantic types (boolean for active, not 1/0)
;;    - Clean field names (no underscores or camelCase)
;;
;;    All other formats are external representations.
;;
;; 5. CSV as String Format
;;    Unlike map formats, CSV is a string.
;;    We build it with simple string concatenation.
;;    Production would handle:
;;    - Escaping (quotes, commas in values)
;;    - Header row
;;    - Multiple records
;;    This challenge keeps it simple for instructional clarity.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo1.md, exemplo2.md
;;
;; Pattern used: Multi-format conversion with canonical intermediate
;;
;; Real-world usage: The reference shows similar multi-format adapters:
;;   (defn convert-user [user source target]
;;     (let [normalized (normalize-to-domain user source)]
;;       (serialize-from-domain normalized target)))
;;
;; Production systems use this pattern for:
;; - ETL pipelines (extract from DB, load to API)
;; - Data export (internal → CSV/Excel)
;; - API gateways (transform between client/server formats)
;; - Migration tools (old format → new format)
;; - Reporting systems (domain → presentation format)

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Database → Domain
  (convert-format
    {:id "U123" :name "John Doe" :email_address "john@example.com" :is_active 1}
    :database
    :domain)
  ;; => {:user-id "U123" :full-name "John Doe" :email "john@example.com" :active true}

  ;; Example 2: Domain → API
  (convert-format
    {:user-id "U456" :full-name "Jane Smith" :email "jane@example.com" :active false}
    :domain
    :api)
  ;; => {:userId "U456" :fullName "Jane Smith" :email "jane@example.com" :status "inactive"}

  ;; Example 3: Domain → CSV
  (convert-format
    {:user-id "U789" :full-name "Bob Wilson" :email "bob@example.com" :active true}
    :domain
    :csv)
  ;; => "U789,Bob Wilson,bob@example.com,true"

  ;; Example 4: Database → API (through domain intermediate)
  (convert-format
    {:id "U999" :name "Alice Brown" :email_address "alice@example.com" :is_active 0}
    :database
    :api)
  ;; => {:userId "U999" :fullName "Alice Brown" :email "alice@example.com" :status "inactive"}

  ;; Example 5: API → Database
  (convert-format
    {:userId "U111" :fullName "Charlie Davis" :email "charlie@example.com" :status "active"}
    :api
    :database)
  ;; => {:id "U111" :name "Charlie Davis" :email_address "charlie@example.com" :is_active 1}

  ;; Example 6: Same format (identity)
  (convert-format
    {:user-id "U222" :full-name "Diana Evans" :email "diana@example.com" :active true}
    :domain
    :domain)
  ;; => {:user-id "U222" :full-name "Diana Evans" :email "diana@example.com" :active true}
)

;; TESTS
;; -----

(defn -test []
  ;; Test database → domain
  (assert (= (convert-format
               {:id "U123" :name "John Doe" :email_address "john@example.com" :is_active 1}
               :database :domain)
             {:user-id "U123" :full-name "John Doe" :email "john@example.com" :active true})
          "Should convert database to domain")

  ;; Test domain → API
  (assert (= (convert-format
               {:user-id "U456" :full-name "Jane Smith" :email "jane@example.com" :active false}
               :domain :api)
             {:userId "U456" :fullName "Jane Smith" :email "jane@example.com" :status "inactive"})
          "Should convert domain to API")

  ;; Test domain → CSV
  (assert (= (convert-format
               {:user-id "U789" :full-name "Bob Wilson" :email "bob@example.com" :active true}
               :domain :csv)
             "U789,Bob Wilson,bob@example.com,true")
          "Should convert domain to CSV")

  ;; Test database → API (through domain)
  (assert (= (convert-format
               {:id "U999" :name "Alice Brown" :email_address "alice@example.com" :is_active 0}
               :database :api)
             {:userId "U999" :fullName "Alice Brown" :email "alice@example.com" :status "inactive"})
          "Should convert database to API through domain intermediate")

  ;; Test API → database
  (assert (= (convert-format
               {:userId "U111" :fullName "Charlie Davis" :email "charlie@example.com" :status "active"}
               :api :database)
             {:id "U111" :name "Charlie Davis" :email_address "charlie@example.com" :is_active 1})
          "Should convert API to database")

  ;; Test same format (identity)
  (let [record {:user-id "U222" :full-name "Diana Evans" :email "diana@example.com" :active true}]
    (assert (= (convert-format record :domain :domain) record)
            "Should return unchanged when source = target"))

  ;; Test domain → database → domain (round-trip)
  (let [original {:user-id "U333" :full-name "Eve Foster" :email "eve@example.com" :active true}
        round-trip (-> original
                       (convert-format :domain :database)
                       (convert-format :database :domain))]
    (assert (= original round-trip)
            "Should satisfy round-trip property"))

  (println "✓ All tests passed! The convert-format function works correctly."))

;; Run: (-test)
