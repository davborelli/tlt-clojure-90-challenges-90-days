;; =============================================================================
;; 007 - UPPERCASE NAMES
;; Level: 2/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This function demonstrates collection transformation using the map higher-order
;; function. Unlike filter which selects elements, map transforms each element
;; by applying a function to it.
;;
;; We use clojure.string/upper-case to convert each name to uppercase. The map
;; function handles the iteration and builds a new collection with the transformed
;; values. We use mapv (map + vec) to return a vector instead of a lazy sequence.
;;
;; This pattern is extremely common in data processing pipelines where you need
;; to apply the same transformation to every item in a collection.

(ns challenge-007.solution
  (:require [clojure.string :as str]))

;; IMPLEMENTATION
;; --------------

(defn uppercase-names
  "Transforms a collection of names to uppercase.

  Takes a vector of name strings and returns a new vector with each
  name converted to uppercase letters.

  Parameters:
  - names: Vector of strings

  Returns: Vector of uppercase strings"
  [names]
  ;; Use mapv to transform each name to uppercase
  (mapv str/upper-case names))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. map Function
;;    map is a higher-order function that transforms collections.
;;    It takes:
;;    - A transformation function
;;    - One or more collections
;;
;;    It applies the function to each element and returns a new collection.
;;
;;    Syntax: (map f coll)
;;
;;    Examples:
;;    (map inc [1 2 3])        ;; => (2 3 4)
;;    (map str/upper-case ["a" "b"]) ;; => ("A" "B")
;;
;;    With multiple collections:
;;    (map + [1 2 3] [10 20 30]) ;; => (11 22 33)
;;
;; 2. mapv vs map
;;    - map: Returns lazy sequence (evaluated on demand)
;;    - mapv: Returns vector (immediate evaluation)
;;
;;    When to use mapv:
;;    - Need immediate results
;;    - Want a vector specifically
;;    - Will use all results
;;    - Working with small to medium collections
;;
;;    When to use map:
;;    - Working with infinite sequences
;;    - Want lazy evaluation
;;    - May not use all results
;;    - Chaining multiple transformations
;;
;; 3. clojure.string Namespace
;;    The clojure.string namespace provides string utilities:
;;    - upper-case: Convert to uppercase
;;    - lower-case: Convert to lowercase
;;    - capitalize: Capitalize first letter
;;    - trim: Remove whitespace from ends
;;    - split: Split string by pattern
;;    - join: Join collection into string
;;    - replace: Replace substrings
;;
;;    Always require it: (:require [clojure.string :as str])
;;
;; 4. Function Composition with map
;;    map enables function composition on collections:
;;
;;    Single transformation:
;;    (mapv str/upper-case names)
;;
;;    Multiple transformations (less efficient):
;;    (mapv str/trim (mapv str/upper-case names))
;;
;;    Better with comp:
;;    (mapv (comp str/trim str/upper-case) names)
;;
;;    Or threading:
;;    (->> names
;;         (mapv str/upper-case)
;;         (mapv str/trim))

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo2.md
;;
;; Pattern used: Collection transformation with map
;;
;; Real-world usage: The reference code shows similar patterns:
;; - Transforming collections of data
;; - Normalizing input data (like uppercase for comparison)
;; - Preparing data for display
;;
;; map is used constantly for:
;; - Extracting fields from maps: (map :name users)
;; - Converting types: (map str numbers)
;; - Normalizing data: (map str/lower-case emails)
;; - Applying calculations: (map #(* % 1.1) prices)

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Simple lowercase names
  (uppercase-names ["john" "jane" "bob"])
  ;; => ["JOHN" "JANE" "BOB"]

  ;; Example 2: Mixed case names
  (uppercase-names ["Alice" "Charlie"])
  ;; => ["ALICE" "CHARLIE"]

  ;; Example 3: Empty input
  (uppercase-names [])
  ;; => []

  ;; Example 4: Names with empty strings
  (uppercase-names ["" "test" ""])
  ;; => ["" "TEST" ""]

  ;; Example 5: Already uppercase
  (uppercase-names ["DAVID" "EMMA"])
  ;; => ["DAVID" "EMMA"]

  ;; Example 6: Names with special characters
  (uppercase-names ["josé" "françois" "björk"])
  ;; => ["JOSÉ" "FRANÇOIS" "BJÖRK"]

  ;; Example 7: Names with numbers
  (uppercase-names ["user1" "user2" "admin3"])
  ;; => ["USER1" "USER2" "ADMIN3"]

  ;; Example 8: Single name
  (uppercase-names ["clojure"])
  ;; => ["CLOJURE"]

  ;; Example 9: Names with spaces
  (uppercase-names ["mary anne" "john smith"])
  ;; => ["MARY ANNE" "JOHN SMITH"]
)

;; TESTS
;; -----

(defn -test []
  (assert (= (uppercase-names ["john" "jane" "bob"])
             ["JOHN" "JANE" "BOB"])
          "Should convert lowercase names to uppercase")

  (assert (= (uppercase-names ["Alice" "Charlie"])
             ["ALICE" "CHARLIE"])
          "Should convert mixed case names to uppercase")

  (assert (= (uppercase-names [])
             [])
          "Should return empty vector for empty input")

  (assert (= (uppercase-names ["" "test" ""])
             ["" "TEST" ""])
          "Should preserve empty strings")

  (assert (= (uppercase-names ["DAVID" "EMMA"])
             ["DAVID" "EMMA"])
          "Should handle already uppercase names")

  (assert (= (uppercase-names ["josé" "françois" "björk"])
             ["JOSÉ" "FRANÇOIS" "BJÖRK"])
          "Should handle international characters")

  (assert (= (uppercase-names ["user1" "user2" "admin3"])
             ["USER1" "USER2" "ADMIN3"])
          "Should handle names with numbers")

  (assert (= (uppercase-names ["clojure"])
             ["CLOJURE"])
          "Should work with single name")

  (assert (= (uppercase-names ["mary anne" "john smith"])
             ["MARY ANNE" "JOHN SMITH"])
          "Should handle names with spaces")

  (println "✓ All tests passed! The uppercase-names function works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
