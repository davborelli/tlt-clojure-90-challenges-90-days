;; =============================================================================
;; 031 - VALIDATE EMAIL FORMAT
;; Level: 7/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This function validates email addresses using a practical approach that
;; checks for the essential components of a valid email: username, @ symbol,
;; and domain with TLD. We split the email by @ and validate each part.
;;
;; The approach uses string manipulation functions rather than complex regex,
;; making it easier to understand and maintain. We check for exactly one @
;; symbol by counting parts after split, then validate each part separately.
;;
;; This pattern is commonly used in production code for input validation,
;; as seen in adapters that parse and validate external data before processing.

(ns challenge-031.solution
  (:require [clojure.string :as str]))

;; IMPLEMENTATION
;; --------------

(defn valid-email-format?
  "Validates email address format using practical rules.

  Parameters:
  - email: The email address string to validate

  Returns: Boolean - true if format is valid, false otherwise"
  [email]
  ;; Split by @ to get username and domain parts
  (let [parts (str/split email #"@")]
    ;; Check that we have exactly 2 parts (one @ symbol)
    (and (= (count parts) 2)
         ;; Get the username and domain parts
         (let [[username domain] parts]
           ;; Validate username is not empty
           (and (not (str/blank? username))
                ;; Validate domain is not empty
                (not (str/blank? domain))
                ;; Validate domain contains at least one dot
                (str/includes? domain ".")
                ;; Validate TLD exists (something after last dot)
                (let [domain-parts (str/split domain #"\.")]
                  (and (> (count domain-parts) 1)
                       (not (str/blank? (last domain-parts))))))))))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. String Processing with split
;;    The split function divides a string by a delimiter (regex pattern).
;;    When splitting by @, we expect exactly 2 parts for valid emails.
;;    Example: "user@domain.com" → ["user" "domain.com"]
;;    Edge case: "user@@domain" → ["user" "" "domain"] (3 parts = invalid)
;;
;; 2. Destructuring in let
;;    We can bind multiple values from a collection using destructuring:
;;    [username domain] binds first element to username, second to domain.
;;    This makes code more readable than using (first parts) and (second parts).
;;
;; 3. Nested Validation with and
;;    The and operator short-circuits: it stops at the first false value.
;;    This allows us to chain multiple validation checks efficiently.
;;    If any check fails, subsequent checks are not evaluated.
;;
;; 4. Regular Expressions for Splitting
;;    #"@" is a regex literal for the @ character.
;;    #"\." is a regex literal for the dot character (escaped because . is
;;    a special regex character meaning "any character").

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo2.md
;;
;; Pattern used: String processing with split and validation
;;
;; Real-world usage: The reference code uses similar patterns to parse
;; query strings and validate input formats:
;;   (str/split client-assertion #"\.")
;;   (when (not (str/blank? spaces-removed)) ...)
;;
;; This shows how string processing functions are essential for parsing
;; and validating external input in production systems.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Valid simple email
  (valid-email-format? "user@example.com")
  ;; => true

  ;; Example 2: Missing @ symbol
  (valid-email-format? "invalid.email")
  ;; => false

  ;; Example 3: Empty username
  (valid-email-format? "@example.com")
  ;; => false

  ;; Example 4: Missing dot in domain
  (valid-email-format? "user@domain")
  ;; => false

  ;; Example 5: Valid email with subdomain
  (valid-email-format? "user@mail.example.com")
  ;; => true

  ;; Example 6: Multiple @ symbols
  (valid-email-format? "user@@example.com")
  ;; => false

  ;; Example 7: Empty domain
  (valid-email-format? "user@")
  ;; => false

  ;; Example 8: Missing TLD
  (valid-email-format? "user@example.")
  ;; => false
)

;; TESTS
;; -----

(defn -test []
  (assert (= (valid-email-format? "user@example.com") true)
          "Should return true for valid simple email")
  (assert (= (valid-email-format? "invalid.email") false)
          "Should return false for email without @")
  (assert (= (valid-email-format? "@example.com") false)
          "Should return false for empty username")
  (assert (= (valid-email-format? "user@domain") false)
          "Should return false for domain without dot")
  (assert (= (valid-email-format? "user@mail.example.com") true)
          "Should return true for email with subdomain")
  (assert (= (valid-email-format? "user@@example.com") false)
          "Should return false for multiple @ symbols")
  (assert (= (valid-email-format? "user@") false)
          "Should return false for empty domain")
  (assert (= (valid-email-format? "user@example.") false)
          "Should return false for missing TLD")
  (assert (= (valid-email-format? "john.doe@company.co.uk") true)
          "Should return true for complex valid email")
  (println "✓ All tests passed!"))

;; Run: (-test)
