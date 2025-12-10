;; =============================================================================
;; 022 - EXTRACT NESTED DATA
;; Level: 5/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This solution demonstrates extracting data from deeply nested structures using
;; get-in, which safely navigates nested maps. This is essential when working
;; with JSON APIs, configuration files, or complex domain models.

(ns challenge-022.solution)

;; IMPLEMENTATION
;; --------------

(defn extract-location
  "Extracts city and zip from nested profile structure.

  Parameters:
  - profile: Nested map with user contact address information

  Returns: Flat map with :city and :zip"
  [profile]
  {:city (get-in profile [:user :contact :address :city])
   :zip  (get-in profile [:user :contact :address :zip])})

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. get-in Function
;;    Navigates nested data structures safely using a path vector.
;;
;;    Examples:
;;    (get-in {:a {:b {:c 42}}} [:a :b :c])  ; => 42
;;    (get-in {:user {:name "John"}} [:user :name])  ; => "John"
;;    (get-in {} [:a :b :c] "default")  ; => "default"
;;
;; 2. Nested Destructuring Alternative
;;    Can also use destructuring:
;;    (let [{{{{city :city zip :zip} :address} :contact} :user} profile]
;;      {:city city :zip zip})
;;    But get-in is clearer for deep nesting.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo1.md
;; Pattern used: Extracting values from nested maps

;; TESTS
;; -----

(defn -test []
  (assert (= (extract-location {:user {:name "John"
                                       :contact {:address {:city "NYC"
                                                           :zip "10001"}}}})
             {:city "NYC" :zip "10001"}))
  (assert (= (extract-location {:user {:name "Jane"
                                       :contact {:address {:city "LA"
                                                           :zip "90001"}}}})
             {:city "LA" :zip "90001"}))
  (println "✓ All tests passed!"))

;; Run: (-test)
