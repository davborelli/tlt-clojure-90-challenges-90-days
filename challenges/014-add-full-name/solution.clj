;; =============================================================================
;; 014 - ADD FULL NAME
;; Level: 3/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This adapter demonstrates adding computed/derived fields to maps. Rather than
;; storing full names directly, we store first and last names separately and
;; compute the full name when needed. This approach:
;; - Reduces data redundancy
;; - Makes updates easier (only update one field, not multiple)
;; - Ensures consistency (full name always matches parts)
;;
;; We extract the first and last names using destructuring, concatenate them
;; with a space, and add the result as a new field using assoc. This pattern
;; is common when preparing data for display or external systems.

(ns challenge-014.solution)

;; IMPLEMENTATION
;; --------------

(defn add-full-name
  "Adds a computed :full-name field from :first-name and :last-name.

  Creates a full name by concatenating first name and last name with a space.

  Parameters:
  - user: Map with :first-name and :last-name keys

  Returns: New map with :full-name field added"
  [user]
  ;; Destructure to extract first and last names
  (let [{:keys [first-name last-name]} user]
    ;; Add :full-name field with concatenated value
    (assoc user :full-name (str first-name " " last-name))))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Computed/Derived Fields
;;    Fields calculated from other fields rather than stored directly.
;;
;;    Benefits:
;;    - Single source of truth (no redundancy)
;;    - Automatic consistency (always correct)
;;    - Easier updates (change source field, derived field updates)
;;    - Saves storage space
;;
;;    Common examples:
;;    - Full name from first + last name
;;    - Age from birthdate
;;    - Total from price * quantity
;;    - Display strings from structured data
;;    - URLs from slugs and domains
;;
;;    When to compute vs store:
;;    - Compute: Fast calculation, used infrequently
;;    - Store: Expensive calculation, used frequently
;;
;; 2. String Concatenation with str
;;    str concatenates multiple values into a string.
;;
;;    Examples:
;;    (str "Hello" " " "World")
;;    ;; => "Hello World"
;;
;;    (str "Count: " 42)
;;    ;; => "Count: 42"
;;
;;    (str nil " " "test")
;;    ;; => " test" (nil becomes empty string)
;;
;;    (str)
;;    ;; => "" (no args returns empty string)
;;
;;    str handles any type:
;;    (str {:a 1})    ;; => "{:a 1}"
;;    (str [1 2 3])   ;; => "[1 2 3]"
;;
;; 3. Map Destructuring Patterns
;;    Multiple ways to extract values from maps:
;;
;;    a) Direct access:
;;       (let [first-name (:first-name user)
;;             last-name (:last-name user)]
;;         ...)
;;
;;    b) Destructuring with :keys:
;;       (let [{:keys [first-name last-name]} user]
;;         ...)
;;
;;    c) Destructuring with custom names:
;;       (let [{first :first-name last :last-name} user]
;;         ...)
;;
;;    d) Destructuring with defaults:
;;       (let [{:keys [first-name last-name]
;;              :or {first-name "Unknown" last-name "Unknown"}} user]
;;         ...)
;;
;;    :keys destructuring is most common for kebab-case keywords.
;;
;; 4. Enrichment Pattern
;;    Adding computed fields is a form of data enrichment:
;;
;;    Step 1: Start with base data
;;    {:first-name "John" :last-name "Doe"}
;;
;;    Step 2: Enrich with computed field
;;    {:first-name "John" :last-name "Doe" :full-name "John Doe"}
;;
;;    Multiple enrichments:
;;    (-> user
;;        (add-full-name)
;;        (add-display-email)
;;        (add-age-from-birthdate))
;;
;;    This pattern appears in:
;;    - View preparation (add display fields)
;;    - API responses (add computed metadata)
;;    - Data pipelines (progressive enrichment)
;;
;; 5. Alternative Implementations
;;    Different ways to solve this problem:
;;
;;    a) Our solution (with destructuring):
;;       (let [{:keys [first-name last-name]} user]
;;         (assoc user :full-name (str first-name " " last-name)))
;;
;;    b) Direct access (no destructuring):
;;       (assoc user :full-name
;;              (str (:first-name user) " " (:last-name user)))
;;
;;    c) Using format:
;;       (assoc user :full-name
;;              (format "%s %s" (:first-name user) (:last-name user)))
;;
;;    d) With threading macro:
;;       (->> [(get user :first-name)
;;             (get user :last-name)]
;;            (str/join " ")
;;            (assoc user :full-name))
;;
;;    Our solution balances clarity and conciseness.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo4.md
;;
;; Pattern used: Adding computed/derived fields to maps
;;
;; Real-world usage: Computed fields appear everywhere:
;; - User profiles (full name, initials, age)
;; - E-commerce (subtotal, tax, total)
;; - Analytics (rates, percentages, ratios)
;; - Display formatting (formatted dates, currency)
;;
;; The reference code shows adapters enriching data, and adding computed
;; fields is a key part of this enrichment process.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Standard name
  (add-full-name {:first-name "John" :last-name "Doe"})
  ;; => {:first-name "John" :last-name "Doe" :full-name "John Doe"}

  ;; Example 2: Different name
  (add-full-name {:first-name "Jane" :last-name "Smith"})
  ;; => {:first-name "Jane" :last-name "Smith" :full-name "Jane Smith"}

  ;; Example 3: Another name
  (add-full-name {:first-name "Bob" :last-name "Johnson"})
  ;; => {:first-name "Bob" :last-name "Johnson" :full-name "Bob Johnson"}

  ;; Example 4: With extra fields preserved
  (add-full-name {:first-name "Alice"
                  :last-name "Brown"
                  :age 30
                  :email "alice@example.com"})
  ;; => {:first-name "Alice" :last-name "Brown" :age 30
  ;;     :email "alice@example.com" :full-name "Alice Brown"}

  ;; Example 5: Hyphenated last name
  (add-full-name {:first-name "Mary" :last-name "Anne-Smith"})
  ;; => {:first-name "Mary" :last-name "Anne-Smith" :full-name "Mary Anne-Smith"}

  ;; Example 6: Single letter names
  (add-full-name {:first-name "J" :last-name "K"})
  ;; => {:first-name "J" :last-name "K" :full-name "J K"}

  ;; Example 7: Using in a pipeline
  (->> {:first-name "Charlie" :last-name "Wilson"}
       (add-full-name)
       (:full-name))
  ;; => "Charlie Wilson"

  ;; Example 8: Mapping over collection
  (map add-full-name [{:first-name "Dave" :last-name "Miller"}
                      {:first-name "Eve" :last-name "Davis"}])
  ;; => ({:first-name "Dave" :last-name "Miller" :full-name "Dave Miller"}
  ;;     {:first-name "Eve" :last-name "Davis" :full-name "Eve Davis"})

  ;; Example 9: Chaining enrichments
  (-> {:first-name "Frank" :last-name "Garcia" :age 25}
      (add-full-name)
      (assoc :status :active))
  ;; => {:first-name "Frank" :last-name "Garcia" :age 25
  ;;     :full-name "Frank Garcia" :status :active}
)

;; TESTS
;; -----

(defn -test []
  (assert (= (add-full-name {:first-name "John" :last-name "Doe"})
             {:first-name "John"
              :last-name "Doe"
              :full-name "John Doe"})
          "Should add full name from first and last name")

  (assert (= (add-full-name {:first-name "Jane" :last-name "Smith"})
             {:first-name "Jane"
              :last-name "Smith"
              :full-name "Jane Smith"})
          "Should work for different names")

  (assert (= (add-full-name {:first-name "Bob" :last-name "Johnson"})
             {:first-name "Bob"
              :last-name "Johnson"
              :full-name "Bob Johnson"})
          "Should concatenate with space correctly")

  (assert (= (add-full-name {:first-name "Alice"
                             :last-name "Brown"
                             :age 30
                             :email "alice@example.com"})
             {:first-name "Alice"
              :last-name "Brown"
              :age 30
              :email "alice@example.com"
              :full-name "Alice Brown"})
          "Should preserve extra fields")

  (assert (= (add-full-name {:first-name "Mary" :last-name "Anne-Smith"})
             {:first-name "Mary"
              :last-name "Anne-Smith"
              :full-name "Mary Anne-Smith"})
          "Should work with hyphenated last names")

  (assert (= (add-full-name {:first-name "J" :last-name "K"})
             {:first-name "J"
              :last-name "K"
              :full-name "J K"})
          "Should work with single letter names")

  ;; Test extraction of full name
  (assert (= (->> {:first-name "Charlie" :last-name "Wilson"}
                  (add-full-name)
                  (:full-name))
             "Charlie Wilson")
          "Should be able to extract computed full name")

  ;; Test with map
  (assert (= (map add-full-name [{:first-name "Dave" :last-name "Miller"}
                                 {:first-name "Eve" :last-name "Davis"}])
             '({:first-name "Dave" :last-name "Miller" :full-name "Dave Miller"}
               {:first-name "Eve" :last-name "Davis" :full-name "Eve Davis"}))
          "Should work when mapping over collection")

  (println "✓ All tests passed! The add-full-name function works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
