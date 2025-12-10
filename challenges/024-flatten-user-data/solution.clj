;; =============================================================================
;; 024 - FLATTEN NESTED USER DATA
;; Level: 5/18 | Type: Adapter
;; =============================================================================

(ns challenge-024.solution)

(defn flatten-profile
  "Flattens nested user profile to single-level map.

  Parameters:
  - profile: Nested map with :user key containing user data

  Returns: Flat map with user fields at top level"
  [profile]
  (:user profile))

;; CONCEPT EXPLANATIONS
;; --------------------
;; 1. Simple extraction: Since the inner map is already flat, just extract it
;; 2. Keywords as functions: (:user profile) extracts the :user value
;; 3. This is the simplest form of flattening (one level deep)

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo1.md
;; Pattern used: Flattening nested structures for simpler access

;; TESTS
;; -----

(defn -test []
  (assert (= (flatten-profile {:user {:name "John" :age 25 :email "john@example.com"}})
             {:name "John" :age 25 :email "john@example.com"}))
  (assert (= (flatten-profile {:user {:name "Jane" :age 30 :email "jane@test.com"}})
             {:name "Jane" :age 30 :email "jane@test.com"}))
  (println "✓ All tests passed!"))

;; Run: (-test)
