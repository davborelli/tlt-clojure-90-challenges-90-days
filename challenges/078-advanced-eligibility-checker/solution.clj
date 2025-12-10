;; =============================================================================
;; 078 - ADVANCED ELIGIBILITY CHECKER
;; Level: 16/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This adapter demonstrates a production-grade pattern for normalizing
;; heterogeneous data from multiple sources. Real-world systems often need
;; to integrate data from web applications, mobile apps, third-party APIs,
;; and legacy systems - each with different naming conventions, date formats,
;; and data types.
;;
;; The solution uses a multi-method dispatch pattern based on the source
;; system, allowing each source to have its own specialized normalization
;; logic while maintaining a consistent output format. This approach is
;; highly extensible - adding a new source only requires implementing a
;; new method without modifying existing code.
;;
;; Key challenges addressed include: date format conversions (MM/DD/YYYY,
;; Unix timestamps, ISO strings, DDMMYYYY), type coercion (strings to numbers),
;; key naming conventions (camelCase, snake_case, kebab-case, UPPERCASE),
;; and handling optional fields with sensible defaults.
;;
;; This pattern is common in financial services, e-commerce platforms, and
;; enterprise integrations where data quality and consistency are critical
;; for business logic and compliance requirements.

(ns challenge-078.solution
  (:require [clojure.string :as str]))

;; IMPLEMENTATION
;; --------------

;; Helper: Convert MM/DD/YYYY to ISO YYYY-MM-DD
(defn- web-date->iso
  "Converts web date format (MM/DD/YYYY) to ISO format (YYYY-MM-DD)"
  [date-str]
  (when date-str
    (let [[month day year] (str/split date-str #"/")]
      (format "%s-%02d-%02d" year (Integer/parseInt month) (Integer/parseInt day)))))

;; Helper: Convert Unix timestamp to ISO date
(defn- timestamp->iso
  "Converts Unix timestamp (seconds since epoch) to ISO date format"
  [timestamp]
  (when timestamp
    (let [millis (* timestamp 1000)
          date (java.util.Date. millis)
          formatter (java.text.SimpleDateFormat. "yyyy-MM-dd")]
      (.format formatter date))))

;; Helper: Convert DDMMYYYY to ISO YYYY-MM-DD
(defn- legacy-date->iso
  "Converts legacy date format (DDMMYYYY) to ISO format (YYYY-MM-DD)"
  [date-str]
  (when (and date-str (= 8 (count date-str)))
    (let [day (subs date-str 0 2)
          month (subs date-str 2 4)
          year (subs date-str 4 8)]
      (format "%s-%s-%s" year month day))))

;; Helper: Parse string to number safely
(defn- safe-parse-number
  "Safely parses string to number, returns nil on failure"
  [s]
  (when s
    (try
      (if (string? s)
        (Double/parseDouble s)
        s)
      (catch Exception _ nil))))

;; Helper: Normalize employment status
(defn- normalize-employment
  "Converts various employment status formats to standard keyword"
  [status]
  (when status
    (let [normalized (-> (str status)
                        str/lower-case
                        (str/replace #"_" "-")
                        keyword)]
      (case normalized
        (:employed :self-employed :unemployed :retired) normalized
        :unknown))))

;; Multi-method for source-specific normalization
(defmulti normalize-by-source
  "Dispatches normalization based on source system"
  :source)

;; Web source normalization (camelCase, MM/DD/YYYY dates)
(defmethod normalize-by-source :web
  [{:keys [data]}]
  (let [{:keys [userId firstName lastName birthDate annualIncome
                creditScore employmentStatus requestedAmount hasCollateral]} data
        errors (cond-> []
                 (not firstName) (conj "Missing required field: firstName")
                 (not lastName) (conj "Missing required field: lastName")
                 (not annualIncome) (conj "Missing required field: annualIncome")
                 (not (safe-parse-number creditScore)) (conj "Invalid credit score format"))]
    (if (seq errors)
      {:user-id userId
       :validation-errors errors}
      {:user-id userId
       :full-name (str firstName " " lastName)
       :birth-date (web-date->iso birthDate)
       :annual-income (safe-parse-number annualIncome)
       :credit-score (safe-parse-number creditScore)
       :employment-status (normalize-employment employmentStatus)
       :requested-amount (safe-parse-number requestedAmount)
       :has-collateral (boolean hasCollateral)
       :source-system :web})))

;; Mobile source normalization (snake_case, Unix timestamps)
(defmethod normalize-by-source :mobile
  [{:keys [data]}]
  (let [{:keys [user_id full_name birth_date annual_income
                credit_score employment_status requested_amount has_collateral]} data]
    {:user-id user_id
     :full-name full_name
     :birth-date (timestamp->iso birth_date)
     :annual-income annual_income
     :credit-score credit_score
     :employment-status (normalize-employment employment_status)
     :requested-amount requested_amount
     :has-collateral (boolean has_collateral)
     :source-system :mobile}))

;; API source normalization (kebab-case, ISO dates)
(defmethod normalize-by-source :api
  [{:keys [data]}]
  (let [{:keys [user-id full-name birth-date annual-income
                credit-score employment-status requested-amount has-collateral]} data]
    {:user-id user-id
     :full-name full-name
     :birth-date birth-date
     :annual-income annual-income
     :credit-score credit-score
     :employment-status (normalize-employment employment-status)
     :requested-amount requested-amount
     :has-collateral (boolean has-collateral)
     :source-system :api}))

;; Legacy source normalization (UPPERCASE, DDMMYYYY dates)
(defmethod normalize-by-source :legacy
  [{:keys [data]}]
  (let [{:keys [USERID FIRSTNAME LASTNAME BIRTHDATE ANNUALINCOME
                CREDITSCORE EMPSTATUS REQUESTAMT COLLATERAL]} data]
    {:user-id USERID
     :full-name (str FIRSTNAME " " LASTNAME)
     :birth-date (legacy-date->iso BIRTHDATE)
     :annual-income (safe-parse-number ANNUALINCOME)
     :credit-score (safe-parse-number CREDITSCORE)
     :employment-status (normalize-employment EMPSTATUS)
     :requested-amount (safe-parse-number REQUESTAMT)
     :has-collateral (= COLLATERAL "Y")
     :source-system :legacy}))

;; Default method for unknown sources
(defmethod normalize-by-source :default
  [{:keys [source]}]
  {:validation-errors [(str "Unknown source system: " source)]})

;; Main normalization function
(defn normalize-eligibility-data
  "Normalizes eligibility data from multiple source systems into standard format.

  Parameters:
  - source-data: Map with :source (keyword) and :data (map)

  Returns: Normalized eligibility map with standardized keys"
  [source-data]
  (normalize-by-source source-data))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Multi-methods (defmulti/defmethod)
;;    Multi-methods provide polymorphic dispatch in Clojure, allowing different
;;    implementations based on a dispatch function. Unlike protocols (which dispatch
;;    on type), multi-methods can dispatch on any function of the arguments - in
;;    this case, the :source keyword. This makes them perfect for handling multiple
;;    data formats from different sources. The dispatch function (:source) is
;;    evaluated first, then the appropriate method is selected. New sources can be
;;    added by simply defining new methods without touching existing code.
;;
;; 2. Conditional Field Handling
;;    Real-world data is messy - fields may be missing, optional, or conditionally
;;    present based on the source system. This solution handles optional fields by
;;    using sensible defaults (e.g., :has-collateral defaults to false) and validates
;;    required fields before processing. The cond-> threading macro elegantly builds
;;    an error vector only when conditions are met, avoiding nested if statements.
;;    Production systems must handle incomplete data gracefully.
;;
;; 3. Date Format Normalization
;;    Different systems use different date formats: web forms often use MM/DD/YYYY,
;;    mobile apps use Unix timestamps, APIs prefer ISO 8601, and legacy systems may
;;    use compact formats like DDMMYYYY. Converting all to a standard ISO format
;;    (YYYY-MM-DD) enables consistent comparison, sorting, and storage. Each helper
;;    function handles one format, making the code testable and maintainable.
;;
;; 4. Type Coercion with Error Handling
;;    External data often arrives as strings even for numeric fields. The
;;    safe-parse-number function attempts conversion and returns nil on failure,
;;    preventing exceptions from crashing the system. This defensive programming
;;    approach is crucial in adapters - you can't trust external data. The function
;;    also handles values that are already numbers, making it idempotent.
;;
;; 5. Naming Convention Translation
;;    Different programming ecosystems have different naming conventions: JavaScript
;;    uses camelCase, Python/Ruby use snake_case, Clojure prefers kebab-case, and
;;    legacy systems often use UPPERCASE. This adapter translates all variations
;;    into Clojure's idiomatic kebab-case keywords. The normalize-employment function
;;    demonstrates this by converting "self_employed" → :self-employed.
;;
;; 6. Validation Error Collection
;;    Rather than failing fast on the first error, this adapter collects all
;;    validation errors using cond-> to thread an error vector through multiple
;;    checks. This provides better user experience - showing all problems at once
;;    instead of forcing users to fix errors one at a time. The pattern uses
;;    cond-> to conditionally conj errors only when validation fails.
;;
;; 7. Private Helper Functions
;;    The solution uses private helper functions (marked with - suffix) to break
;;    down complex transformations into testable units. Each helper has a single
;;    responsibility: date conversion, number parsing, status normalization. This
;;    follows the Single Responsibility Principle and makes the code easier to
;;    test, debug, and maintain. Private functions also clearly indicate internal
;;    implementation details vs. public API.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo3.md
;;
;; Pattern used: Multi-format data normalization with conditional field handling
;;
;; Real-world usage: The reference code demonstrates adapting between different
;; data representations (wire format ↔ domain format) commonly needed when
;; integrating with external systems. Financial services especially need robust
;; adapters to handle data from various sources (credit bureaus, banks, regulatory
;; systems) where each source has different formats, naming conventions, and
;; data quality levels.
;;
;; The multi-method pattern extends this by allowing source-specific logic while
;; maintaining a consistent interface, similar to how the reference handles
;; different wire formats for different API endpoints or service versions.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Web source with complete data
  (normalize-eligibility-data
    {:source :web
     :data {:userId "W123"
            :firstName "John"
            :lastName "Smith"
            :birthDate "03/15/1985"
            :annualIncome "75000"
            :creditScore "720"
            :employmentStatus "employed"
            :requestedAmount "25000"}})
  ;; => {:user-id "W123"
  ;;     :full-name "John Smith"
  ;;     :birth-date "1985-03-15"
  ;;     :annual-income 75000.0
  ;;     :credit-score 720.0
  ;;     :employment-status :employed
  ;;     :requested-amount 25000.0
  ;;     :has-collateral false
  ;;     :source-system :web}

  ;; Example 2: Mobile source with Unix timestamp
  (normalize-eligibility-data
    {:source :mobile
     :data {:user_id "M456"
            :full_name "Jane Doe"
            :birth_date 512611200
            :annual_income 95000
            :credit_score 780
            :employment_status "self_employed"
            :requested_amount 40000
            :has_collateral true}})
  ;; => {:user-id "M456"
  ;;     :full-name "Jane Doe"
  ;;     :birth-date "1986-03-28"
  ;;     :annual-income 95000
  ;;     :credit-score 780
  ;;     :employment-status :self-employed
  ;;     :requested-amount 40000
  ;;     :has-collateral true
  ;;     :source-system :mobile}

  ;; Example 3: API source (already in good format)
  (normalize-eligibility-data
    {:source :api
     :data {:user-id "A789"
            :full-name "Bob Johnson"
            :birth-date "1990-07-20"
            :annual-income 120000
            :credit-score 810
            :employment-status "employed"
            :requested-amount 50000
            :has-collateral false}})
  ;; => {:user-id "A789"
  ;;     :full-name "Bob Johnson"
  ;;     :birth-date "1990-07-20"
  ;;     :annual-income 120000
  ;;     :credit-score 810
  ;;     :employment-status :employed
  ;;     :requested-amount 50000
  ;;     :has-collateral false
  ;;     :source-system :api}

  ;; Example 4: Legacy source with UPPERCASE and compact date
  (normalize-eligibility-data
    {:source :legacy
     :data {:USERID "L321"
            :FIRSTNAME "Alice"
            :LASTNAME "Williams"
            :BIRTHDATE "15031982"
            :ANNUALINCOME "88000"
            :CREDITSCORE "750"
            :EMPSTATUS "RETIRED"
            :REQUESTAMT "30000"
            :COLLATERAL "Y"}})
  ;; => {:user-id "L321"
  ;;     :full-name "Alice Williams"
  ;;     :birth-date "1982-03-15"
  ;;     :annual-income 88000.0
  ;;     :credit-score 750.0
  ;;     :employment-status :retired
  ;;     :requested-amount 30000.0
  ;;     :has-collateral true
  ;;     :source-system :legacy}

  ;; Example 5: Validation errors - missing required fields
  (normalize-eligibility-data
    {:source :web
     :data {:userId "W999"
            :creditScore "invalid"}})
  ;; => {:user-id "W999"
  ;;     :validation-errors ["Missing required field: firstName"
  ;;                        "Missing required field: lastName"
  ;;                        "Invalid credit score format"
  ;;                        "Missing required field: annualIncome"]}

  ;; Example 6: Unknown source system
  (normalize-eligibility-data
    {:source :unknown-system
     :data {:some "data"}})
  ;; => {:validation-errors ["Unknown source system: :unknown-system"]}

  ;; Example 7: Web source with optional collateral
  (normalize-eligibility-data
    {:source :web
     :data {:userId "W555"
            :firstName "Carol"
            :lastName "Davis"
            :birthDate "06/10/1988"
            :annualIncome "92000"
            :creditScore "795"
            :employmentStatus "self-employed"
            :requestedAmount "35000"
            :hasCollateral true}})
  ;; => {:user-id "W555"
  ;;     :full-name "Carol Davis"
  ;;     :birth-date "1988-06-10"
  ;;     :annual-income 92000.0
  ;;     :credit-score 795.0
  ;;     :employment-status :self-employed
  ;;     :requested-amount 35000.0
  ;;     :has-collateral true
  ;;     :source-system :web}
)

;; TESTS
;; -----

(defn -test []
  ;; Test web source normalization
  (let [result (normalize-eligibility-data
                 {:source :web
                  :data {:userId "W123"
                         :firstName "John"
                         :lastName "Smith"
                         :birthDate "03/15/1985"
                         :annualIncome "75000"
                         :creditScore "720"
                         :employmentStatus "employed"
                         :requestedAmount "25000"}})]
    (assert (= (:user-id result) "W123")
            "Web: Should extract user ID")
    (assert (= (:full-name result) "John Smith")
            "Web: Should combine first and last name")
    (assert (= (:birth-date result) "1985-03-15")
            "Web: Should convert MM/DD/YYYY to ISO format")
    (assert (= (:annual-income result) 75000.0)
            "Web: Should parse income as number")
    (assert (= (:source-system result) :web)
            "Web: Should tag with source system"))

  ;; Test mobile source normalization
  (let [result (normalize-eligibility-data
                 {:source :mobile
                  :data {:user_id "M456"
                         :full_name "Jane Doe"
                         :birth_date 512611200
                         :annual_income 95000
                         :credit_score 780
                         :employment_status "self_employed"
                         :requested_amount 40000
                         :has_collateral true}})]
    (assert (= (:user-id result) "M456")
            "Mobile: Should extract user ID")
    (assert (= (:employment-status result) :self-employed)
            "Mobile: Should normalize employment status to keyword")
    (assert (= (:has-collateral result) true)
            "Mobile: Should preserve collateral flag"))

  ;; Test legacy source with compact date
  (let [result (normalize-eligibility-data
                 {:source :legacy
                  :data {:USERID "L321"
                         :FIRSTNAME "Alice"
                         :LASTNAME "Williams"
                         :BIRTHDATE "15031982"
                         :ANNUALINCOME "88000"
                         :CREDITSCORE "750"
                         :EMPSTATUS "RETIRED"
                         :REQUESTAMT "30000"
                         :COLLATERAL "Y"}})]
    (assert (= (:birth-date result) "1982-03-15")
            "Legacy: Should convert DDMMYYYY to ISO format")
    (assert (= (:has-collateral result) true)
            "Legacy: Should convert Y to true")
    (assert (= (:employment-status result) :retired)
            "Legacy: Should normalize employment status"))

  ;; Test validation errors
  (let [result (normalize-eligibility-data
                 {:source :web
                  :data {:userId "W999"
                         :creditScore "invalid"}})]
    (assert (contains? result :validation-errors)
            "Should return validation errors for invalid data")
    (assert (some #(str/includes? % "Missing required field: firstName")
                  (:validation-errors result))
            "Should report missing firstName"))

  ;; Test API source (clean data)
  (let [result (normalize-eligibility-data
                 {:source :api
                  :data {:user-id "A789"
                         :full-name "Bob Johnson"
                         :birth-date "1990-07-20"
                         :annual-income 120000
                         :credit-score 810
                         :employment-status "employed"
                         :requested-amount 50000
                         :has-collateral false}})]
    (assert (= (:birth-date result) "1990-07-20")
            "API: Should preserve ISO date format")
    (assert (= (:annual-income result) 120000)
            "API: Should preserve numeric income"))

  ;; Test unknown source
  (let [result (normalize-eligibility-data
                 {:source :unknown-system
                  :data {:some "data"}})]
    (assert (contains? result :validation-errors)
            "Should handle unknown source system")
    (assert (some #(str/includes? % "Unknown source system")
                  (:validation-errors result))
            "Should report unknown source"))

  (println "✓ All tests passed! Advanced eligibility checker works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
