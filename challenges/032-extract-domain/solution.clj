;; =============================================================================
;; 032 - EXTRACT DOMAIN FROM URL
;; Level: 7/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This function extracts the domain name from URLs by progressively removing
;; unwanted parts: protocol, path, query parameters, and port numbers.
;; We use the threading macro (->) to make the transformation pipeline clear.
;;
;; The approach handles various URL formats by processing them in order:
;; first removing protocol prefixes, then splitting by delimiters that separate
;; the domain from other parts (/, ?, :), and taking the first part.
;;
;; This pattern is common in production adapters that normalize and parse
;; external URLs for logging, analytics, or API calls.

(ns challenge-032.solution
  (:require [clojure.string :as str]))

;; IMPLEMENTATION
;; --------------

(defn extract-domain
  "Extracts the domain name from a URL string.

  Parameters:
  - url: The URL string to parse

  Returns: String - the domain name"
  [url]
  (-> url
      ;; Remove https:// protocol if present
      (str/replace #"^https://" "")
      ;; Remove http:// protocol if present
      (str/replace #"^http://" "")
      ;; Split by / to separate domain from path, take first part
      (str/split #"/")
      (first)
      ;; Split by ? to remove query params, take first part
      (str/split #"\?")
      (first)
      ;; Split by : to remove port number, take first part
      (str/split #":")
      (first)))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Threading Macro (->)
;;    The -> macro threads a value through a series of transformations.
;;    Each form receives the result of the previous form as its first argument.
;;    This makes sequential transformations very readable.
;;    Example: (-> x (f) (g)) is equivalent to (g (f x))
;;
;; 2. Regular Expression Anchors
;;    The ^ anchor matches the start of the string.
;;    #"^https://" only matches "https://" at the beginning of the string.
;;    This prevents accidentally removing "https://" from the middle of a URL.
;;
;; 3. Progressive Refinement Pattern
;;    We process the URL in stages, each stage removing one type of unwanted
;;    content. This is more maintainable than trying to do everything at once
;;    with a complex regex. Each step is simple and testable.
;;
;; 4. split + first Pattern
;;    Splitting a string and taking the first part is a common way to extract
;;    content before a delimiter. For example, splitting "domain.com/path" by
;;    "/" gives ["domain.com" "path"], and (first ...) gives "domain.com".

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo2.md
;;
;; Pattern used: String processing with replace and split
;;
;; Real-world usage: The reference code uses similar patterns for parsing:
;;   (str/replace spaces-and-newlines #"\s" "")
;;   (str/split client-assertion #"\.")
;;
;; This shows how string transformations are chained to clean and parse
;; external input in production systems, particularly in adapters that
;; handle external data formats.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Full URL with https and path
  (extract-domain "https://www.example.com/path/to/page")
  ;; => "www.example.com"

  ;; Example 2: URL with port and query params
  (extract-domain "http://api.github.com:443/users?page=1")
  ;; => "api.github.com"

  ;; Example 3: URL without protocol
  (extract-domain "example.com/about")
  ;; => "example.com"

  ;; Example 4: localhost with port
  (extract-domain "https://localhost:8080")
  ;; => "localhost"

  ;; Example 5: Subdomain with multiple path segments
  (extract-domain "https://api.service.example.com/v1/users/123")
  ;; => "api.service.example.com"

  ;; Example 6: URL with query params but no path
  (extract-domain "https://search.example.com?q=clojure")
  ;; => "search.example.com"
)

;; TESTS
;; -----

(defn -test []
  (assert (= (extract-domain "https://www.example.com/path/to/page")
             "www.example.com")
          "Should extract domain from https URL with path")
  (assert (= (extract-domain "http://api.github.com:443/users?page=1")
             "api.github.com")
          "Should extract domain removing port and query params")
  (assert (= (extract-domain "example.com/about")
             "example.com")
          "Should extract domain from URL without protocol")
  (assert (= (extract-domain "https://localhost:8080")
             "localhost")
          "Should extract localhost and remove port")
  (assert (= (extract-domain "https://api.service.example.com/v1/users/123")
             "api.service.example.com")
          "Should handle subdomain with multiple path segments")
  (assert (= (extract-domain "https://search.example.com?q=clojure")
             "search.example.com")
          "Should handle query params without path")
  (println "✓ All tests passed!"))

;; Run: (-test)
