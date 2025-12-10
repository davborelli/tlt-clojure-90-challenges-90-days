;; =============================================================================
;; 021 - GROUP USERS BY AGE RANGE
;; Level: 5/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This solution demonstrates categorical grouping using group-by with a custom
;; classifier function. Rather than grouping by exact values (like group-by :age),
;; we group by computed categories based on age ranges.
;;
;; We define an age-category function that maps each age to a category keyword,
;; then use group-by to partition users by these categories. To ensure all
;; categories appear in the result (even if empty), we merge the grouped result
;; with a template containing empty vectors for each category.
;;
;; This pattern is common in analytics and reporting where raw data needs to be
;; bucketed into meaningful categories for analysis, visualization, or business
;; rules.

(ns challenge-021.solution)

;; IMPLEMENTATION
;; --------------

(defn age-category
  "Determines the age category for a given age.

  Categories:
  - :child (0-12)
  - :teen (13-17)
  - :adult (18-64)
  - :senior (65+)

  Parameters:
  - age: Integer age value

  Returns: Keyword representing age category"
  [age]
  (cond
    (<= age 12) :child
    (<= age 17) :teen
    (<= age 64) :adult
    :else       :senior))

(defn group-by-age
  "Groups users by age category.

  Categorizes users into :child, :teen, :adult, and :senior groups
  based on their age. All categories are present in the result even
  if some have no users.

  Parameters:
  - users: Collection of user maps with :name and :age

  Returns: Map with category keywords as keys and user collections as values"
  [users]
  ;; Start with template containing all categories with empty vectors
  (let [empty-categories {:child [] :teen [] :adult [] :senior []}
        ;; Group users by their age category
        grouped (group-by (fn [user]
                           (age-category (:age user)))
                         users)]
    ;; Merge to ensure all categories exist (grouped overwrites empty)
    (merge empty-categories grouped)))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. group-by Function
;;    group-by partitions a collection into groups based on a classifier
;;    function. It returns a map where keys are the classification values
;;    and values are collections of items with that classification.
;;
;;    Signature: (group-by f coll)
;;
;;    Examples:
;;    ; Group by exact value:
;;    (group-by :type [{:type :a :val 1}
;;                     {:type :b :val 2}
;;                     {:type :a :val 3}])
;;    ; => {:a [{:type :a :val 1} {:type :a :val 3}]
;;    ;     :b [{:type :b :val 2}]}
;;
;;    ; Group numbers by odd/even:
;;    (group-by #(if (even? %) :even :odd) [1 2 3 4 5])
;;    ; => {:odd [1 3 5] :even [2 4]}
;;
;;    ; Group strings by length:
;;    (group-by count ["a" "bb" "ccc" "dd" "e"])
;;    ; => {1 ["a" "e"] 2 ["bb" "dd"] 3 ["ccc"]}
;;
;;    ; Group by first letter:
;;    (group-by first ["apple" "apricot" "banana" "blueberry"])
;;    ; => {\a ["apple" "apricot"] \b ["banana" "blueberry"]}
;;
;; 2. Custom Classifier Functions
;;    The power of group-by comes from custom classifiers that compute
;;    categories based on complex logic.
;;
;;    Pattern:
;;    (defn classifier [item]
;;      (cond
;;        (condition1 item) :category1
;;        (condition2 item) :category2
;;        :else            :default))
;;
;;    Examples:
;;    ; Risk level classifier:
;;    (defn risk-level [amount]
;;      (cond
;;        (< amount 100)    :low
;;        (< amount 1000)   :medium
;;        :else             :high))
;;
;;    ; Priority classifier:
;;    (defn priority [task]
;;      (cond
;;        (:urgent task)    :high
;;        (:important task) :medium
;;        :else            :low))
;;
;; 3. Ensuring All Categories Exist
;;    Sometimes you need all possible categories in the result, even if empty.
;;    Use merge with a template:
;;
;;    Pattern:
;;    (let [template {:cat1 [] :cat2 [] :cat3 []}
;;          grouped (group-by classifier coll)]
;;      (merge template grouped))
;;
;;    Why this works:
;;    - merge combines maps, with right maps overwriting left
;;    - template provides all keys with default values
;;    - grouped overwrites only keys that have data
;;    - result has all keys, some with data, some with defaults
;;
;;    Example:
;;    (merge {:a [] :b [] :c []}
;;           {:a [1 2] :c [3]})
;;    ; => {:a [1 2] :b [] :c [3]}
;;
;; 4. Data Categorization Patterns
;;    Common categorization scenarios:
;;
;;    ; Numeric ranges:
;;    (defn score-grade [score]
;;      (cond
;;        (>= score 90) :A
;;        (>= score 80) :B
;;        (>= score 70) :C
;;        :else        :F))
;;
;;    ; Time-based:
;;    (defn time-of-day [hour]
;;      (cond
;;        (< hour 6)   :night
;;        (< hour 12)  :morning
;;        (< hour 18)  :afternoon
;;        :else        :evening))
;;
;;    ; Size categories:
;;    (defn file-size-category [bytes]
;;      (cond
;;        (< bytes 1024)         :small   ; < 1KB
;;        (< bytes (* 1024 1024)) :medium  ; < 1MB
;;        :else                   :large)) ; >= 1MB
;;
;; 5. Analytics Use Cases
;;    Grouping by categories enables many analytics patterns:
;;
;;    ; Count per category:
;;    (defn count-by-category [grouped]
;;      (into {} (map (fn [[k v]] [k (count v)]) grouped)))
;;
;;    ; Percentage per category:
;;    (defn percentage-by-category [grouped total]
;;      (into {} (map (fn [[k v]]
;;                      [k (* 100.0 (/ (count v) total))])
;;                    grouped)))
;;
;;    ; Statistics per category:
;;    (defn avg-per-category [grouped]
;;      (into {} (map (fn [[k users]]
;;                      [k (if (seq users)
;;                           (/ (apply + (map :age users))
;;                              (count users))
;;                           0)])
;;                    grouped)))

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo2.md
;;
;; Pattern used: Custom categorical grouping
;;
;; Real-world usage: Similar to grouping patterns in the reference code
;; where data is categorized for processing or reporting.
;;
;; In production systems, this appears in:
;; - User demographics (age groups, income brackets)
;; - Transaction analysis (amount ranges, risk levels)
;; - Performance monitoring (latency buckets, error categories)
;; - A/B testing (variant groups, cohort analysis)

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Mixed ages, one per category
  (group-by-age [{:name "Alice" :age 10}
                 {:name "Bob" :age 16}
                 {:name "Charlie" :age 30}
                 {:name "Diana" :age 70}])
  ;; => {:child [{:name "Alice" :age 10}]
  ;;     :teen [{:name "Bob" :age 16}]
  ;;     :adult [{:name "Charlie" :age 30}]
  ;;     :senior [{:name "Diana" :age 70}]}

  ;; Example 2: All adults
  (group-by-age [{:name "John" :age 25}
                 {:name "Jane" :age 30}])
  ;; => {:child []
  ;;     :teen []
  ;;     :adult [{:name "John" :age 25} {:name "Jane" :age 30}]
  ;;     :senior []}

  ;; Example 3: Empty collection
  (group-by-age [])
  ;; => {:child [] :teen [] :adult [] :senior []}

  ;; Example 4: Boundary ages
  (group-by-age [{:name "Exactly12" :age 12}
                 {:name "Exactly13" :age 13}
                 {:name "Exactly17" :age 17}
                 {:name "Exactly18" :age 18}
                 {:name "Exactly64" :age 64}
                 {:name "Exactly65" :age 65}])
  ;; => {:child [{:name "Exactly12" :age 12}]
  ;;     :teen [{:name "Exactly13" :age 13} {:name "Exactly17" :age 17}]
  ;;     :adult [{:name "Exactly18" :age 18} {:name "Exactly64" :age 64}]
  ;;     :senior [{:name "Exactly65" :age 65}]}

  ;; Example 5: Count per category
  (let [grouped (group-by-age [{:name "A" :age 10} {:name "B" :age 20}
                               {:name "C" :age 15} {:name "D" :age 70}])]
    (into {} (map (fn [[k v]] [k (count v)]) grouped)))
  ;; => {:child 1 :teen 1 :adult 1 :senior 1}

  ;; Example 6: Get specific category
  (:adult (group-by-age [{:name "A" :age 25} {:name "B" :age 30}]))
  ;; => [{:name "A" :age 25} {:name "B" :age 30}]

  ;; Example 7: Test age-category helper
  (map age-category [0 5 12 13 17 18 30 64 65 100])
  ;; => (:child :child :child :teen :teen :adult :adult :adult :senior :senior)

  ;; Example 8: Percentage distribution
  (let [grouped (group-by-age [{:name "A" :age 10} {:name "B" :age 20}
                               {:name "C" :age 15} {:name "D" :age 70}])
        total 4]
    (into {} (map (fn [[k v]] [k (* 100.0 (/ (count v) total))]) grouped)))
  ;; => {:child 25.0 :teen 25.0 :adult 25.0 :senior 25.0}
)

;; TESTS
;; -----

(defn -test []
  ;; Test mixed ages
  (let [result (group-by-age [{:name "Alice" :age 10}
                              {:name "Bob" :age 16}
                              {:name "Charlie" :age 30}
                              {:name "Diana" :age 70}])]
    (assert (= (count (:child result)) 1) "Should have 1 child")
    (assert (= (count (:teen result)) 1) "Should have 1 teen")
    (assert (= (count (:adult result)) 1) "Should have 1 adult")
    (assert (= (count (:senior result)) 1) "Should have 1 senior"))

  ;; Test all adults
  (let [result (group-by-age [{:name "John" :age 25}
                              {:name "Jane" :age 30}])]
    (assert (empty? (:child result)) "Child should be empty")
    (assert (empty? (:teen result)) "Teen should be empty")
    (assert (= (count (:adult result)) 2) "Should have 2 adults")
    (assert (empty? (:senior result)) "Senior should be empty"))

  ;; Test empty collection
  (let [result (group-by-age [])]
    (assert (= result {:child [] :teen [] :adult [] :senior []})
            "All categories should exist and be empty"))

  ;; Test boundary ages
  (let [result (group-by-age [{:name "Age12" :age 12}
                              {:name "Age13" :age 13}
                              {:name "Age17" :age 17}
                              {:name "Age18" :age 18}
                              {:name "Age64" :age 64}
                              {:name "Age65" :age 65}])]
    (assert (= (count (:child result)) 1) "12 should be child")
    (assert (= (count (:teen result)) 2) "13-17 should be teen")
    (assert (= (count (:adult result)) 2) "18-64 should be adult")
    (assert (= (count (:senior result)) 1) "65+ should be senior"))

  ;; Test age-category helper
  (assert (= (age-category 0) :child))
  (assert (= (age-category 12) :child))
  (assert (= (age-category 13) :teen))
  (assert (= (age-category 17) :teen))
  (assert (= (age-category 18) :adult))
  (assert (= (age-category 64) :adult))
  (assert (= (age-category 65) :senior))
  (assert (= (age-category 100) :senior))

  ;; Test all categories exist
  (let [result (group-by-age [{:name "Test" :age 25}])]
    (assert (contains? result :child))
    (assert (contains? result :teen))
    (assert (contains? result :adult))
    (assert (contains? result :senior)))

  (println "✓ All tests passed! The group-by-age function works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
