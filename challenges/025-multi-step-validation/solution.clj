;; =============================================================================
;; 025 - MULTI-STEP VALIDATION WITH CUSTOM ERRORS
;; Level: 5/18 | Type: Controller
;; =============================================================================

(ns challenge-025.solution
  (:require [clojure.string :as str]))

(defn validate-user-multi
  "Validates user through multiple checks, returning first error or success.

  Parameters:
  - user: Map with :name, :email, and :age

  Returns: Error map with :status :error and :message, or success map"
  [user]
  (let [{:keys [name email age]} user]
    (or
      ;; Check 1: Name required
      (when (str/blank? name)
        {:status :error :message "Name is required"})

      ;; Check 2: Email format
      (when-not (str/includes? email "@")
        {:status :error :message "Invalid email"})

      ;; Check 3: Age range
      (when (or (< age 18) (> age 120))
        {:status :error :message "Invalid age range"})

      ;; All passed
      {:status :success :user user})))

;; CONCEPT EXPLANATIONS
;; --------------------
;; 1. or Short-Circuit: Returns first truthy value or last value
;; 2. when returns nil if condition false, error map if true
;; 3. nil is falsy, so or skips nils and continues to next check
;; 4. First error (truthy) stops evaluation and returns

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/controllers/exemplo1.md
;; Pattern used: or composition for error handling

;; TESTS
;; -----

(defn -test []
  (assert (= (validate-user-multi {:name "John" :email "john@test.com" :age 25})
             {:status :success :user {:name "John" :email "john@test.com" :age 25}}))
  (assert (= (validate-user-multi {:name "" :email "test@test.com" :age 25})
             {:status :error :message "Name is required"}))
  (assert (= (validate-user-multi {:name "Jane" :email "invalid" :age 25})
             {:status :error :message "Invalid email"}))
  (assert (= (validate-user-multi {:name "Bob" :email "bob@test.com" :age 150})
             {:status :error :message "Invalid age range"}))
  (assert (= (validate-user-multi {:name "Alice" :email "alice@test.com" :age 17})
             {:status :error :message "Invalid age range"}))
  (println "✓ All tests passed!"))

;; Run: (-test)
