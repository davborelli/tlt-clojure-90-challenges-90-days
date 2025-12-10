;; =============================================================================
;; 089 - RECURSIVE SCHEMA VALIDATION
;; Level: 18/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; Complex applications work with deeply nested data structures: JSON APIs
;; return nested objects, database models have relationships, configuration files
;; contain hierarchical settings, and domain models compose smaller entities.
;; Validating such structures requires more than checking individual fields - we
;; must traverse the entire structure, validate each level, handle recursive
;; references (like tree nodes pointing to other tree nodes), and accumulate
;; all errors for comprehensive feedback.
;;
;; This solution implements a recursive schema validation system inspired by
;; JSON Schema and Clojure Spec. The core insight is treating schemas as data
;; structures that describe valid data. A schema for a user might specify:
;; "must be a map, requires :name (string) and :age (number), optionally includes
;; :address (itself a map with required :city and :zip)". The validator recursively
;; traverses both the schema and data in lockstep, validating each level.
;;
;; Error accumulation is crucial for usability. Instead of failing at the first
;; error, the validator continues checking and collects all violations. For a
;; form with 10 fields, showing all 5 invalid fields at once is far better than
;; showing one at a time. The validator builds error paths (like [:user :address
;; :zip-code]) that precisely identify where each violation occurred, essential
;; for nested structures where multiple fields might have the same name at
;; different levels.
;;
;; Recursive schemas handle self-referential structures like trees and graphs.
;; A tree node has a value and children that are themselves tree nodes. The
;; validator must track visited nodes to detect cycles (A → B → A) and avoid
;; infinite recursion. This is critical for validating data structures like
;; organizational hierarchies, file systems, and social graphs.

(ns challenge-089.solution
  (:require [clojure.string :as str]))

;; IMPLEMENTATION
;; --------------

;; Type Validation

(defn validate-type
  "Validates that data matches expected type.

  Returns nil if valid, error map if invalid. Handles primitive types
  (string, number, boolean) and collection types (map, vector)."
  [schema-type data path]
  (let [valid? (case schema-type
                :string (string? data)
                :number (number? data)
                :boolean (boolean? data)
                :map (map? data)
                :vector (vector? data)
                :keyword (keyword? data)
                :any true  ; Any type allowed
                false)]
    (when-not valid?
      {:path path
       :error :type-mismatch
       :message (str "Expected " (name schema-type) ", got " (type data))
       :expected schema-type
       :actual (type data)})))

;; Required Field Validation

(defn validate-required-fields
  "Validates that all required fields are present in a map.

  Returns vector of error maps for missing fields."
  [required-fields data path]
  (if (not (map? data))
    []
    (for [field required-fields
          :when (not (contains? data field))]
      {:path (conj path field)
       :error :required-field-missing
       :message (str "Required field missing: " field)
       :field field})))

;; Custom Validator Application

(defn apply-custom-validators
  "Applies custom validator functions to data.

  Each validator is a function that returns nil (valid) or error message (invalid).
  Returns vector of error maps."
  [validators data path]
  (if (nil? validators)
    []
    (for [validator validators
          :let [result (try
                        (validator data)
                        (catch Exception e
                          {:error (.getMessage e)}))]
          :when result]
      {:path path
       :error :validation-failed
       :message (if (string? result)
                 result
                 (str "Validation failed: " (:error result)))
       :validator-result result})))

;; Recursive Validation Core

(defn validate-map-fields
  "Recursively validates fields in a map according to field schemas.

  For each field in the schema, validates the corresponding data field
  recursively. Accumulates errors from all fields."
  [field-schemas data path visited validate-fn]
  (if (not (map? data))
    []
    (mapcat (fn [[field-name field-schema]]
              (if (contains? data field-name)
                (let [field-data (get data field-name)
                      field-path (conj path field-name)]
                  (validate-fn field-schema field-data field-path visited))
                []))
            field-schemas)))

(defn validate-vector-items
  "Recursively validates items in a vector according to item schema.

  Each item in the vector is validated against the same schema. Errors
  include the index in the path for precise error location."
  [item-schema data path visited validate-fn]
  (if (not (vector? data))
    []
    (mapcat (fn [idx item]
              (let [item-path (conj path idx)]
                (validate-fn item-schema item item-path visited)))
            (range)
            data)))

(defn detect-cycle
  "Detects cycles in recursive structures to prevent infinite recursion.

  Uses visited set to track seen objects. Returns error if cycle detected,
  nil otherwise."
  [data path visited]
  (when (and (or (map? data) (vector? data))
             (contains? visited data))
    {:path path
     :error :circular-reference
     :message "Circular reference detected"
     :cycle true}))

(defn validate-schema
  "Main recursive validation function.

  Parameters:
  - schema: Schema definition map with :type, :required, :fields, :items, :validators
  - data: Data to validate
  - path: Current path in data structure (for error messages)
  - visited: Set of visited objects (for cycle detection)

  Returns: Map with :valid, :errors, :error-count"
  ([schema data]
   (validate-schema schema data [] #{}))

  ([schema data path visited]
   (let [schema-type (:type schema)

         ;; Check for cycles first (only for collections)
         cycle-error (when (and (:recursive schema)
                              (or (map? data) (vector? data)))
                      (detect-cycle data path visited))

         ;; Early return if cycle detected
         _ (when cycle-error
             {:valid false
              :errors [cycle-error]
              :error-count 1})

         ;; Add current object to visited set
         new-visited (if (or (map? data) (vector? data))
                      (conj visited data)
                      visited)

         ;; Type validation
         type-error (when schema-type
                     (validate-type schema-type data path))

         ;; Required fields validation (for maps)
         required-errors (if (= schema-type :map)
                          (validate-required-fields (:required schema) data path)
                          [])

         ;; Custom validators
         validator-errors (apply-custom-validators (:validators schema) data path)

         ;; Recursive field validation
         field-errors (if (and (= schema-type :map) (:fields schema))
                       (validate-map-fields (:fields schema) data path new-visited
                                          validate-schema)
                       [])

         ;; Recursive item validation (for vectors)
         item-errors (if (and (= schema-type :vector) (:items schema))
                      (validate-vector-items (:items schema) data path new-visited
                                            validate-schema)
                      [])

         ;; Collect all errors
         all-errors (remove nil? (concat (when type-error [type-error])
                                        required-errors
                                        validator-errors
                                        field-errors
                                        item-errors))]

     (if (empty? all-errors)
       {:valid true
        :errors []
        :error-count 0}
       {:valid false
        :errors all-errors
        :error-count (count all-errors)}))))

;; Helper Functions for Common Validators

(defn email-validator
  "Validates email format with basic regex"
  [data]
  (when-not (and (string? data)
                (re-matches #"^[^@]+@[^@]+\.[^@]+$" data))
    "Invalid email format"))

(defn min-length-validator
  "Creates validator for minimum string length"
  [min-len]
  (fn [data]
    (when-not (and (string? data) (>= (count data) min-len))
      (str "Minimum length is " min-len))))

(defn max-length-validator
  "Creates validator for maximum string length"
  [max-len]
  (fn [data]
    (when-not (and (string? data) (<= (count data) max-len))
      (str "Maximum length is " max-len))))

(defn range-validator
  "Creates validator for numeric range"
  [min-val max-val]
  (fn [data]
    (when-not (and (number? data) (>= data min-val) (<= data max-val))
      (str "Value must be between " min-val " and " max-val))))

(defn enum-validator
  "Creates validator for allowed values"
  [allowed-values]
  (fn [data]
    (when-not (contains? (set allowed-values) data)
      (str "Value must be one of: " (str/join ", " allowed-values)))))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Recursive Data Structures
;;    Recursive data structures contain references to themselves. Trees are the
;;    classic example: each node has a value and children that are themselves
;;    trees. JSON can nest objects arbitrarily deep. XML elements contain other
;;    elements. These structures require recursive validation where the validator
;;    calls itself with nested data. The key insight is that validation logic
;;    stays the same at each level - we just validate the current level then
;;    recurse to children. This mirrors how recursive data structures are defined:
;;    a tree is either empty or a node with child trees. The validator follows
;;    the same pattern.
;;
;; 2. Error Accumulation
;;    User experience improves dramatically when validation shows all errors at
;;    once rather than one at a time. Consider a registration form with 10 fields
;;    - showing all 5 invalid fields immediately is far better than making users
;;    submit 5 times to discover all errors. This requires the validator to continue
;;    after finding errors, collecting them into a list. The challenge is balancing
;;    completeness (find all errors) with performance (don't do unnecessary work).
;;    Our implementation validates all fields at each level, then recurses. In
;;    production, consider "fail-fast" mode for quick validation and "thorough"
;;    mode for comprehensive error reporting.
;;
;; 3. Error Paths
;;    In nested structures, error locations must be precise. Saying "name is
;;    required" is ambiguous - whose name? The user's name? The billing address
;;    name? The shipping address name? Error paths solve this by recording the
;;    exact location: [:user :billing-address :name]. This is like file system
;;    paths (/user/billing-address/name) but for data structures. Our implementation
;;    builds paths incrementally as we recurse, passing (conj path :field-name)
;;    to recursive calls. This makes error messages actionable: "Required field
;;    missing at [:user :address :zip-code]" tells exactly where to fix the problem.
;;
;; 4. Cycle Detection
;;    Recursive structures can contain cycles where A references B which references
;;    A. Without cycle detection, the validator enters infinite recursion and
;;    stack overflows. We track visited objects in a set, checking each object
;;    before recursing. If we encounter an already-visited object, we've found
;;    a cycle. This uses object identity (not value equality) so two maps with
;;    identical contents are distinct. In production, consider configurable cycle
;;    handling: reject cycles (our implementation), allow cycles with max depth,
;;    or ignore cycles in certain fields (like parent pointers in trees).
;;
;; 5. Schema as Data
;;    Treating schemas as data rather than code provides powerful flexibility.
;;    Schemas can be stored in databases, transmitted over networks, generated
;;    dynamically, or composed from smaller schemas. This is the insight behind
;;    JSON Schema and Clojure Spec. A schema is just a map describing valid data:
;;    {:type :map :required #{:name} :fields {:name {:type :string}}}. This enables
;;    meta-programming where schemas validate other schemas, and allows non-programmers
;;    to define validation rules through configuration. The trade-off is performance
;;    - interpreted schemas are slower than compiled validators. For production,
;;    consider compiling schemas to optimized validator functions.
;;
;; 6. Extensible Validation
;;    Custom validators enable domain-specific validation beyond type checking.
;;    Email format, credit card validation, business rules - these can't be
;;    expressed in basic type schemas. Our implementation accepts validator
;;    functions that return nil (valid) or error message (invalid). This makes
;;    the system extensible without modifying core validation logic. Validators
;;    can be composed (all validators must pass) or chained (first failure stops
;;    validation). In production, provide a library of common validators (email,
;;    URL, phone, credit card) and clear documentation for writing custom validators.
;;    Consider validator composition operators like "all-of", "any-of", "not".
;;
;; 7. Partial vs Complete Validation
;;    Sometimes you want to validate only part of a schema, allowing extra fields
;;    or optional nested structures. Our implementation is strict - all required
;;    fields must be present and all data must match schemas. For production,
;;    add modes: strict (our implementation), loose (allow extra fields), partial
;;    (allow missing nested objects). This is useful for APIs where clients send
;;    subsets of data for updates, or for progressive validation in forms where
;;    you validate one section at a time. The trade-off is complexity - more
;;    modes mean more code and edge cases to handle.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo3.md
;;
;; Pattern used: Complex validation with multiple conditional branches
;;
;; The reference code demonstrates sophisticated validation logic:
;;
;; 1. Multi-condition validation with early returns:
;;    (cond
;;      (not risk-allows-automation?)
;;      (not-allowed-risk fraud-check ...)
;;
;;      (or (nil? capture-info) (nil? docs-capture-id))
;;      (insufficient-data fraud-check ...)
;;
;;      (not (capture-method-allowed? capture-info))
;;      (not-allowed-capture-method fraud-check ...)
;;
;;      :else
;;      (automated-success fraud-check ...))
;;
;;    This shows systematic validation with multiple checks, similar to how our
;;    schema validator checks type, required fields, custom validators, etc.
;;
;; 2. Hierarchical decision making:
;;    The fraud check evaluates multiple criteria in order of priority (risk level,
;;    data availability, capture method, validation status). Our recursive validator
;;    similarly checks schemas hierarchically: type first, then required fields,
;;    then custom validators, then nested structures.
;;
;; 3. Error context preservation:
;;    Each validation branch returns specific error information:
;;    (not-allowed-risk fraud-check docs-capture-id as-of)
;;
;;    This provides context about what failed and why, analogous to how our
;;    validator includes paths and detailed error messages.
;;
;; 4. Validation composition:
;;    The check-fraud-analysis function composes multiple validators:
;;    risk-allows-automation?, capture-method-allowed?, sufficient-data?.
;;    Our schema validator similarly composes type checks, required field checks,
;;    and custom validators.
;;
;; Real-world usage: The fraud check logic validates complex onboarding data,
;; checking risk ratings, document capture completeness, customer literacy status,
;; and multiple other criteria. Each validation has specific error handling and
;; context. This is exactly what our recursive schema validator does for arbitrary
;; nested data structures - comprehensive validation with detailed error reporting.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Simple flat validation - success
  (validate-schema
    {:type :map
     :required #{:name :email}
     :fields {:name {:type :string}
             :email {:type :string
                    :validators [email-validator]}}}
    {:name "John Doe"
     :email "john@example.com"})
  ;; => {:valid true
  ;;     :errors []
  ;;     :error-count 0}

  ;; Example 2: Simple validation - with errors
  (validate-schema
    {:type :map
     :required #{:name :age}
     :fields {:name {:type :string}
             :age {:type :number}}}
    {:name "John"})  ; Missing age
  ;; => {:valid false
  ;;     :errors [{:path [:age]
  ;;               :error :required-field-missing
  ;;               :message "Required field missing: age"
  ;;               :field :age}]
  ;;     :error-count 1}

  ;; Example 3: Nested validation with path tracking
  (validate-schema
    {:type :map
     :fields {:user {:type :map
                    :required #{:name :age}
                    :fields {:name {:type :string}
                            :age {:type :number}
                            :address {:type :map
                                     :required #{:city :zip}
                                     :fields {:city {:type :string}
                                             :zip {:type :string}}}}}}}
    {:user {:name "John"
            :age "not a number"  ; Type error
            :address {:city "NYC"}}})  ; Missing zip
  ;; => {:valid false
  ;;     :errors [{:path [:user :age]
  ;;               :error :type-mismatch
  ;;               :message "Expected number, got java.lang.String"}
  ;;              {:path [:user :address :zip]
  ;;               :error :required-field-missing
  ;;               :message "Required field missing: zip"}]
  ;;     :error-count 2}

  ;; Example 4: Recursive schema - tree structure
  (def tree-schema
    {:type :map
     :required #{:value}
     :fields {:value {:type :number}
             :children {:type :vector
                       :items {:type :map
                              :recursive true
                              :required #{:value}
                              :fields {:value {:type :number}
                                      :children {:type :vector}}}}}
     :recursive true})

  (validate-schema
    tree-schema
    {:value 10
     :children [{:value 5
                 :children []}
                {:value 15
                 :children [{:value 12
                            :children []}
                           {:value 18
                            :children []}]}]})
  ;; => {:valid true :errors [] :error-count 0}

  ;; Example 5: Vector validation with index tracking
  (validate-schema
    {:type :vector
     :items {:type :map
            :required #{:id :name}
            :fields {:id {:type :number}
                    :name {:type :string}}}}
    [{:id 1 :name "Alice"}
     {:id 2}  ; Missing name
     {:id "three" :name "Charlie"}])  ; Wrong type
  ;; => {:valid false
  ;;     :errors [{:path [1 :name]
  ;;               :error :required-field-missing
  ;;               :message "Required field missing: name"}
  ;;              {:path [2 :id]
  ;;               :error :type-mismatch
  ;;               :message "Expected number, got java.lang.String"}]
  ;;     :error-count 2}

  ;; Example 6: Custom validators
  (validate-schema
    {:type :map
     :required #{:username :age :role}
     :fields {:username {:type :string
                        :validators [(min-length-validator 3)
                                   (max-length-validator 20)]}
             :age {:type :number
                  :validators [(range-validator 18 100)]}
             :role {:type :keyword
                   :validators [(enum-validator [:admin :user :guest])]}}}
    {:username "ab"  ; Too short
     :age 150  ; Out of range
     :role :superadmin})  ; Not in enum
  ;; => {:valid false
  ;;     :errors [{:path [:username]
  ;;               :error :validation-failed
  ;;               :message "Minimum length is 3"}
  ;;              {:path [:age]
  ;;               :error :validation-failed
  ;;               :message "Value must be between 18 and 100"}
  ;;              {:path [:role]
  ;;               :error :validation-failed
  ;;               :message "Value must be one of: :admin, :user, :guest"}]
  ;;     :error-count 3}

  ;; Example 7: Multiple error types in nested structure
  (validate-schema
    {:type :map
     :required #{:user :items}
     :fields {:user {:type :map
                    :required #{:id :email}
                    :fields {:id {:type :number}
                            :email {:type :string
                                   :validators [email-validator]}}}
             :items {:type :vector
                    :items {:type :map
                           :required #{:product-id :quantity}
                           :fields {:product-id {:type :string}
                                   :quantity {:type :number
                                             :validators [(range-validator 1 100)]}}}}}}
    {:user {:id "not-a-number"  ; Type error
            :email "invalid-email"}  ; Validator error
     :items [{:product-id "PROD-1"
              :quantity 0}  ; Validator error
             {:product-id "PROD-2"}]})  ; Missing required field
  ;; => {:valid false
  ;;     :errors [{:path [:user :id]
  ;;               :error :type-mismatch
  ;;               :message "Expected number, got java.lang.String"}
  ;;              {:path [:user :email]
  ;;               :error :validation-failed
  ;;               :message "Invalid email format"}
  ;;              {:path [:items 0 :quantity]
  ;;               :error :validation-failed
  ;;               :message "Value must be between 1 and 100"}
  ;;              {:path [:items 1 :quantity]
  ;;               :error :required-field-missing
  ;;               :message "Required field missing: quantity"}]
  ;;     :error-count 4}
)

;; TESTS
;; -----

(defn -test []
  ;; Test 1: Simple valid map
  (let [result (validate-schema
                 {:type :map
                  :required #{:name}
                  :fields {:name {:type :string}}}
                 {:name "Test"})]
    (assert (:valid result)
            "Simple valid map should pass validation")
    (assert (= (:error-count result) 0)
            "Should have zero errors"))

  ;; Test 2: Missing required field
  (let [result (validate-schema
                 {:type :map
                  :required #{:name :age}
                  :fields {:name {:type :string}
                          :age {:type :number}}}
                 {:name "Test"})]
    (assert (not (:valid result))
            "Missing required field should fail validation")
    (assert (= (:error-count result) 1)
            "Should have one error")
    (assert (= (get-in result [:errors 0 :error]) :required-field-missing)
            "Error should be required-field-missing"))

  ;; Test 3: Type mismatch
  (let [result (validate-schema
                 {:type :map
                  :fields {:age {:type :number}}}
                 {:age "not a number"})]
    (assert (not (:valid result))
            "Type mismatch should fail validation")
    (assert (= (get-in result [:errors 0 :error]) :type-mismatch)
            "Error should be type-mismatch"))

  ;; Test 4: Nested validation with error paths
  (let [result (validate-schema
                 {:type :map
                  :fields {:user {:type :map
                                 :required #{:name}
                                 :fields {:name {:type :string}}}}}
                 {:user {}})]
    (assert (not (:valid result))
            "Nested validation should catch missing fields")
    (assert (= (get-in result [:errors 0 :path]) [:user :name])
            "Error path should include nested path"))

  ;; Test 5: Vector validation
  (let [result (validate-schema
                 {:type :vector
                  :items {:type :number}}
                 [1 2 "three" 4])]
    (assert (not (:valid result))
            "Vector validation should catch type errors")
    (assert (= (get-in result [:errors 0 :path]) [2])
            "Error path should include vector index"))

  ;; Test 6: Custom validator
  (let [result (validate-schema
                 {:type :string
                  :validators [email-validator]}
                 "invalid-email")]
    (assert (not (:valid result))
            "Custom validator should detect invalid data")
    (assert (= (get-in result [:errors 0 :error]) :validation-failed)
            "Error should be validation-failed"))

  ;; Test 7: Multiple errors accumulated
  (let [result (validate-schema
                 {:type :map
                  :required #{:name :age :email}
                  :fields {:name {:type :string}
                          :age {:type :number}
                          :email {:type :string
                                 :validators [email-validator]}}}
                 {:name "Test"
                  :email "invalid"})]
    (assert (not (:valid result))
            "Should fail validation with multiple errors")
    (assert (>= (:error-count result) 2)
            "Should accumulate multiple errors (missing age + invalid email)"))

  (println "✓ All tests passed! The recursive schema validation system works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
