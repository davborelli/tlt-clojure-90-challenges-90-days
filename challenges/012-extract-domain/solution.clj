;; =============================================================================
;; 012 - EXTRACT DOMAIN
;; Level: 3/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This function demonstrates string parsing by extracting structured information
;; from formatted text. Email addresses have a predictable structure (user@domain),
;; which we can exploit to extract the domain part.
;;
;; We use str/split to break the email into parts based on the @ delimiter.
;; For valid emails, this produces a vector with two elements: ["user" "domain"].
;; We extract the second part (domain) using last, which safely handles both
;; valid emails and edge cases.
;;
;; This pattern of splitting strings and extracting parts appears constantly
;; in data processing, parsing, and validation tasks.

(ns challenge-012.solution
  (:require [clojure.string :as str]))

;; IMPLEMENTATION
;; --------------

(defn extract-domain
  "Extracts the domain part from an email address.

  Splits the email by @ and returns the domain part (everything after @).
  Returns empty string if @ is not present.

  Parameters:
  - email: Email address string

  Returns: Domain string (e.g., \"example.com\")"
  [email]
  ;; Split by @ and get the last part (domain)
  ;; If no @ exists, split returns single element, last returns it
  (let [parts (str/split email #"@")]
    (if (> (count parts) 1)
      (last parts)
      "")))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. clojure.string/split
;;    split divides a string into parts based on a pattern.
;;
;;    Syntax: (str/split s pattern)
;;           (str/split s pattern limit)
;;
;;    Examples:
;;    (str/split "a,b,c" #",")
;;    ;; => ["a" "b" "c"]
;;
;;    (str/split "john@example.com" #"@")
;;    ;; => ["john" "example.com"]
;;
;;    (str/split "a::b::c" #"::")
;;    ;; => ["a" "b" "c"]
;;
;;    With limit:
;;    (str/split "a,b,c,d" #"," 2)
;;    ;; => ["a" "b,c,d"]
;;
;;    Note: split takes a regex pattern (use #"..." for regex literals)
;;
;; 2. Regular Expression Patterns
;;    In Clojure, regex patterns are written with #"..." syntax.
;;
;;    Literal characters:
;;    #"@"       ; matches @
;;    #","       ; matches ,
;;    #"\\."     ; matches . (escaped)
;;
;;    Common patterns:
;;    #"\\s+"    ; one or more whitespace
;;    #"[,;]"    ; comma or semicolon
;;    #"\\d+"    ; one or more digits
;;
;;    For this challenge, #"@" matches the literal @ character.
;;
;; 3. Accessing Collection Elements
;;    Multiple ways to get elements from vectors:
;;
;;    By index:
;;    (nth ["a" "b" "c"] 1)     ;; => "b"
;;    (get ["a" "b" "c"] 1)     ;; => "b"
;;    (["a" "b" "c"] 1)         ;; => "b"
;;
;;    First/last:
;;    (first ["a" "b" "c"])     ;; => "a"
;;    (last ["a" "b" "c"])      ;; => "c"
;;
;;    Safe access with default:
;;    (nth ["a"] 5 "default")   ;; => "default"
;;    (get ["a"] 5 "default")   ;; => "default"
;;
;;    For this problem, last is perfect because:
;;    - Valid email: ["user" "domain"] → "domain"
;;    - Just @: ["" ""] → ""
;;
;; 4. String Parsing Patterns
;;    Common string parsing scenarios:
;;
;;    a) Split by delimiter:
;;       (str/split "a,b,c" #",")
;;
;;    b) Split and destructure:
;;       (let [[user domain] (str/split email #"@")]
;;         domain)
;;
;;    c) Split and extract specific part:
;;       (last (str/split email #"@"))
;;
;;    d) Split and process each part:
;;       (map str/trim (str/split csv-line #","))
;;
;;    e) Split with limit:
;;       (str/split "key=value=extra" #"=" 2)
;;       ;; => ["key" "value=extra"]
;;
;; 5. Error Handling Approaches
;;    For this problem, we handle invalid input gracefully:
;;
;;    Our approach (check count):
;;    (if (> (count parts) 1)
;;      (last parts)
;;      "")
;;
;;    Alternative (try-catch, but unnecessary here):
;;    (try
;;      (second (str/split email #"@"))
;;      (catch Exception _ ""))
;;
;;    Alternative (use second with nil check):
;;    (or (second (str/split email #"@")) "")
;;
;;    Our approach is explicit and clear about the intent.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo2.md
;;
;; Pattern used: String parsing with split
;;
;; Real-world usage: String parsing appears in:
;; - Email validation and processing
;; - URL parsing (protocol://domain/path)
;; - CSV/TSV file processing
;; - Log file analysis
;; - Configuration file parsing
;; - Query string parsing (key=value&key2=value2)
;;
;; The reference code shows similar patterns with split-query-string and
;; client-assertion->client-id, both parsing structured strings to extract
;; specific information.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Standard email
  (extract-domain "john@example.com")
  ;; => "example.com"

  ;; Example 2: Different TLD
  (extract-domain "jane@test.org")
  ;; => "test.org"

  ;; Example 3: Multi-part domain
  (extract-domain "bob@company.co.uk")
  ;; => "company.co.uk"

  ;; Example 4: Invalid - no @
  (extract-domain "invalid-email")
  ;; => ""

  ;; Example 5: Subdomain
  (extract-domain "user@mail.example.com")
  ;; => "mail.example.com"

  ;; Example 6: Just @ (edge case)
  (extract-domain "@")
  ;; => ""

  ;; Example 7: @ at start
  (extract-domain "@example.com")
  ;; => "example.com"

  ;; Example 8: @ at end
  (extract-domain "user@")
  ;; => ""

  ;; Example 9: Multiple @ (takes last part)
  (extract-domain "user@host@example.com")
  ;; => "example.com"

  ;; Example 10: Using in a pipeline
  (->> ["john@example.com"
        "jane@test.org"
        "bob@company.co.uk"]
       (map extract-domain)
       (distinct))
  ;; => ("example.com" "test.org" "company.co.uk")
)

;; TESTS
;; -----

(defn -test []
  (assert (= (extract-domain "john@example.com")
             "example.com")
          "Should extract domain from standard email")

  (assert (= (extract-domain "jane@test.org")
             "test.org")
          "Should work with different TLD")

  (assert (= (extract-domain "bob@company.co.uk")
             "company.co.uk")
          "Should work with multi-part domains")

  (assert (= (extract-domain "invalid-email")
             "")
          "Should return empty string when no @ present")

  (assert (= (extract-domain "user@mail.example.com")
             "mail.example.com")
          "Should extract full domain including subdomain")

  (assert (= (extract-domain "@")
             "")
          "Should handle just @ symbol")

  (assert (= (extract-domain "@example.com")
             "example.com")
          "Should handle @ at start")

  (assert (= (extract-domain "user@")
             "")
          "Should return empty string when @ at end")

  (assert (= (extract-domain "user@host@example.com")
             "example.com")
          "Should take last part when multiple @ present")

  ;; Test with map
  (assert (= (map extract-domain ["john@example.com"
                                  "jane@test.org"
                                  "bob@company.co.uk"])
             '("example.com" "test.org" "company.co.uk"))
          "Should work when mapping over collection")

  (println "✓ All tests passed! The extract-domain function works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
