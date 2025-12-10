# 048 - Flatten Nested User Profile

**Level**: 10/18
**Type**: Adapter
**Concepts**: Nested extraction, Flattening data structures, get-in patterns

## Context

External APIs often return deeply nested JSON structures (3+ levels) to organize related data. For internal use, flat structures are often preferred because they're easier to work with in databases, simpler to validate, and more convenient for business logic. Adapters flatten these structures by extracting nested fields into a flat map.

## Objective

Implement an adapter that flattens a deeply nested user profile (3 levels deep) into a flat map suitable for database storage.

## Specification

### Input

- `nested-profile` (map): Deeply nested user profile
  ```clojure
  {:user-id "..."
   :personal {:name "..." :birthdate "..."}
   :contact {:email {:primary "..." :verified boolean}
             :phone {:mobile "..." :country-code "..."}
             :address {:street "..." :city "..." :zip "..." :country "..."}}}
  ```

### Output

- (map): Flat profile with all nested fields extracted
  ```clojure
  {:id "..."
   :name "..."
   :birthdate "..."
   :email "..."
   :email-verified boolean
   :mobile-phone "..."
   :phone-country-code "..."
   :street "..."
   :city "..."
   :zip "..."
   :country "..."}
  ```

### Rules

- Extract `:user-id` → `:id`
- Extract `:personal` → `:name`, `:birthdate`
- Extract `:contact :email` → `:email` (primary), `:email-verified`
- Extract `:contact :phone` → `:mobile-phone`, `:phone-country-code`
- Extract `:contact :address` → `:street`, `:city`, `:zip`, `:country`
- Use `get-in` for safe nested access
- Function must be pure

## Examples

### Example 1
```clojure
(flatten-user-profile
  {:user-id "USER-123"
   :personal {:name "Alice Johnson" :birthdate "1990-05-15"}
   :contact {:email {:primary "alice@example.com" :verified true}
             :phone {:mobile "555-0100" :country-code "+1"}
             :address {:street "123 Main St" :city "New York" :zip "10001" :country "USA"}}})
;; => {:id "USER-123"
;;     :name "Alice Johnson"
;;     :birthdate "1990-05-15"
;;     :email "alice@example.com"
;;     :email-verified true
;;     :mobile-phone "555-0100"
;;     :phone-country-code "+1"
;;     :street "123 Main St"
;;     :city "New York"
;;     :zip "10001"
;;     :country "USA"}
```

### Example 2
```clojure
(flatten-user-profile
  {:user-id "USER-456"
   :personal {:name "Bob Smith" :birthdate "1985-10-20"}
   :contact {:email {:primary "bob@example.com" :verified false}
             :phone {:mobile "555-0200" :country-code "+44"}
             :address {:street "456 Oak Ave" :city "London" :zip "SW1A 1AA" :country "UK"}}})
;; => {:id "USER-456"
;;     :name "Bob Smith"
;;     :email-verified false
;;     ...}
```

## Tips

- Use `get-in` for nested extraction: `(get-in data [:contact :email :primary])`
- Group related extractions for readability
- Consider using destructuring or let bindings for complex nesting
- Pattern: Extract all fields in order (personal, email, phone, address)
- Three levels of nesting requires vectors like `[:a :b :c]` in get-in

## Testing your solution

```bash
cd challenges/048-flatten-nested-user/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-048.solution)
(challenge-048.solution/-test)
```
