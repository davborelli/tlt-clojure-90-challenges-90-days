;; =============================================================================
;; 046 - COMPOSE VALIDATORS
;; Level: 10/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This solution demonstrates function composition for validation pipelines.
;; Instead of one monolithic validation function, we compose small, focused
;; validators that each check one rule. The composition runs validators in
;; sequence and returns the first error or nil if all pass.
;;
;; We use `some` to iterate through validators, applying each to the data.
;; Since validators return nil (pass) or error map (fail), `some` will return
;; the first non-nil result (first error) or nil if all validators return nil.
;;
;; This pattern is fundamental in production systems: validation rules are
;; composed from small, testable functions rather than large conditional blocks.
;; Each validator has single responsibility, making them easy to test, reuse
;; across different use cases, and modify independently.

(ns challenge-046.solution
  (:require [clojure.string :as str]))

;; HELPER VALIDATORS (for testing examples)
;; -----------------------------------------

(defn validate-required-name
  "Validates that name is not blank"
  [data]
  (when (str/blank? (:name data))
    {:error "Name is required"}))

(defn validate-age-limit
  "Validates that age is 18 or older"
  [data]
  (when (< (:age data) 18)
    {:error "Must be 18 or older"}))

(defn validate-email
  "Validates that email contains @"
  [data]
  (when-not (str/includes? (:email data) "@")
    {:error "Invalid email format"}))

;; IMPLEMENTATION
;; --------------

(defn compose-validators
  "Composes multiple validator functions into validation pipeline.

  Parameters:
  - validators: Vector of validator functions [fn1 fn2 ...]
  - data: Map to validate

  Returns: nil if all pass, or first error map {:error \"...\"}

  Each validator takes data and returns nil (pass) or error map (fail).
  Runs validators in sequence, stopping at first error (fail-fast)."
  [validators data]
  ;; Use `some` to find first non-nil result (first error)
  ;; If all validators return nil, `some` returns nil
  (some (fn [validator]
          ;; Apply validator to data
          (validator data))
        validators))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Function Composition for Validation
;;    Instead of one large function with multiple conditions:
;;      (defn validate [data]
;;        (cond
;;          (blank? name) {:error "..."}
;;          (< age 18) {:error "..."}
;;          ...))
;;    We compose small validators:
;;      (compose-validators [val1 val2 val3] data)
;;    Benefits:
;;    - Each validator is independently testable
;;    - Validators are reusable across different contexts
;;    - Easy to add/remove validation rules
;;    - Single Responsibility Principle
;;
;; 2. Higher-Order Functions
;;    `compose-validators` is a higher-order function: it takes functions
;;    (validators) as parameters and combines them. This is core to functional
;;    programming - treating functions as first-class values that can be passed
;;    around, combined, and transformed.
;;
;; 3. `some` for Fail-Fast Validation
;;    `some` iterates through a collection and returns the first truthy value
;;    (non-nil, non-false). For validation:
;;    - Validators return nil (pass) or error map (fail)
;;    - `some` returns first error map encountered
;;    - If all validators return nil, `some` returns nil
;;    This implements fail-fast: stop at first problem, don't continue validating.
;;
;; 4. Validator Function Contract
;;    Each validator follows a simple contract:
;;    - Input: data map
;;    - Output: nil (validation passed) or {:error "message"} (failed)
;;    This consistent interface makes validators composable. Any function
;;    following this contract can be used in the composition.
;;
;; 5. Declarative Validation
;;    The validation pipeline is declared as a vector of validators:
;;      [validate-required-name validate-age-limit validate-email]
;;    This is more declarative than imperative conditionals. The code
;;    describes what validations to run, not how to run them.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo1.md
;;
;; Pattern used: Function composition with higher-order functions
;;
;; Real-world usage: Production systems compose validation functions:
;;   (defn validate-request [request]
;;     (or (validate-headers request)
;;         (validate-auth request)
;;         (validate-payload request)))
;;
;; The reference shows composition patterns where multiple small functions
;; are combined to implement complex logic. Each function has single
;; responsibility, and composition creates the complete behavior.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: All validations pass
  (compose-validators
    [validate-required-name validate-age-limit validate-email]
    {:name "John" :age 25 :email "john@example.com"})
  ;; => nil

  ;; Example 2: Name validation fails (first validator)
  (compose-validators
    [validate-required-name validate-age-limit validate-email]
    {:name "" :age 25 :email "john@example.com"})
  ;; => {:error "Name is required"}

  ;; Example 3: Age validation fails (second validator)
  (compose-validators
    [validate-required-name validate-age-limit validate-email]
    {:name "John" :age 16 :email "john@example.com"})
  ;; => {:error "Must be 18 or older"}

  ;; Example 4: Email validation fails (third validator)
  (compose-validators
    [validate-required-name validate-age-limit validate-email]
    {:name "John" :age 25 :email "invalid"})
  ;; => {:error "Invalid email format"}

  ;; Example 5: Empty validator list (all pass)
  (compose-validators
    []
    {:name "John" :age 25 :email "john@example.com"})
  ;; => nil

  ;; Example 6: Custom validators
  (compose-validators
    [(fn [data] (when (> (:amount data) 10000) {:error "Amount too high"}))
     (fn [data] (when (nil? (:currency data)) {:error "Currency required"}))]
    {:amount 15000 :currency "USD"})
  ;; => {:error "Amount too high"}
)

;; TESTS
;; -----

(defn -test []
  ;; Test all validations pass
  (assert (nil? (compose-validators
                  [validate-required-name validate-age-limit validate-email]
                  {:name "John" :age 25 :email "john@example.com"}))
          "Should return nil when all validations pass")

  ;; Test first validation fails
  (assert (= (compose-validators
               [validate-required-name validate-age-limit validate-email]
               {:name "" :age 25 :email "john@example.com"})
             {:error "Name is required"})
          "Should return first error (name required)")

  ;; Test second validation fails
  (assert (= (compose-validators
               [validate-required-name validate-age-limit validate-email]
               {:name "John" :age 16 :email "john@example.com"})
             {:error "Must be 18 or older"})
          "Should return second error (age limit)")

  ;; Test third validation fails
  (assert (= (compose-validators
               [validate-required-name validate-age-limit validate-email]
               {:name "John" :age 25 :email "invalid"})
             {:error "Invalid email format"})
          "Should return third error (email format)")

  ;; Test empty validators
  (assert (nil? (compose-validators
                  []
                  {:name "John" :age 25 :email "john@example.com"}))
          "Should return nil for empty validators")

  ;; Test multiple failures (returns first)
  (assert (= (compose-validators
               [validate-required-name validate-age-limit validate-email]
               {:name "" :age 16 :email "invalid"})
             {:error "Name is required"})
          "Should return first error when multiple fail")

  (println "✓ All tests passed!"))

;; Run: (-test)
