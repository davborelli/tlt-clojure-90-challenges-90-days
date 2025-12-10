;; =============================================================================
;; 023 - CONVERT KEYWORDS TO STRINGS
;; Level: 5/18 | Type: Adapter
;; =============================================================================

(ns challenge-023.solution)

(defn keywords->strings
  "Converts all keyword keys in a map to string keys."
  [data]
  (reduce-kv (fn [acc k v]
               (assoc acc (name k) v))
             {}
             data))

;; CONCEPT EXPLANATIONS
;; --------------------
;; 1. name function converts keywords to strings: (name :key) => "key"
;; 2. reduce-kv iterates over map key-value pairs
;; 3. Builds new map with string keys and original values

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo5.md
;; Pattern used: Type coercion for external system integration

;; TESTS
;; -----

(defn -test []
  (assert (= (keywords->strings {:name "John" :age 25})
             {"name" "John" "age" 25}))
  (assert (= (keywords->strings {:user-id 123 :email "test@example.com"})
             {"user-id" 123 "email" "test@example.com"}))
  (assert (= (keywords->strings {}) {}))
  (println "✓ All tests passed!"))

;; Run: (-test)
