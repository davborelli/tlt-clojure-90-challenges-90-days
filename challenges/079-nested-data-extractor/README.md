# 079 - Nested Data Extractor

**Level**: 16/18
**Type**: Adapter
**Concepts**: Deep nested extraction, Path navigation, Recursive traversal, Safe navigation

## Context

Modern APIs often return deeply nested JSON structures with complex hierarchies. Applications need to extract specific values from these structures safely, handling missing keys, null values, and varying depths. A robust path-based extraction system is essential for working with GraphQL responses, configuration files, and hierarchical data stores.

## Objective

Implement a flexible data extractor that navigates deeply nested structures using path specifications, safely handling missing keys and providing default values or error information.

## Specification

### Input

- `data` (map): Deeply nested data structure
- `extraction-spec` (map): Specification with:
  - `:paths` (vector of vectors): Each path is a vector of keys to traverse
  - `:aliases` (map): Optional key renames in output
  - `:defaults` (map): Default values for missing paths
  - `:required` (set): Paths that must exist

### Output

- (map): Extracted data with keys from aliases or original paths
  - If required paths missing: includes `:extraction-errors` vector
  - Successfully extracted values use provided aliases
  - Missing optional paths use defaults if provided

### Rules

- Support navigation through nested maps and vectors
- Handle missing intermediate keys gracefully
- Apply default values only to optional paths
- Collect all extraction errors for required paths
- Support keyword and string keys in paths
- Allow extracting from vector indices
- Preserve original data types

## Examples

### Example 1: Simple nested extraction
```clojure
(extract-nested-data
  {:user {:profile {:name "John Doe"
                    :contact {:email "john@example.com"
                             :phone "555-1234"}}
          :settings {:theme "dark"}}}
  {:paths [[:user :profile :name]
           [:user :profile :contact :email]
           [:user :settings :theme]]
   :aliases {[:user :profile :name] :name
             [:user :profile :contact :email] :email
             [:user :settings :theme] :theme}})
;; => {:name "John Doe"
;;     :email "john@example.com"
;;     :theme "dark"}
```

### Example 2: With defaults for missing paths
```clojure
(extract-nested-data
  {:user {:profile {:name "Jane Smith"}}}
  {:paths [[:user :profile :name]
           [:user :profile :contact :email]
           [:user :settings :notifications]]
   :aliases {[:user :profile :name] :name
             [:user :profile :contact :email] :email
             [:user :settings :notifications] :notifications}
   :defaults {[:user :profile :contact :email] "no-email@example.com"
              [:user :settings :notifications] true}})
;; => {:name "Jane Smith"
;;     :email "no-email@example.com"
;;     :notifications true}
```

### Example 3: Required paths validation
```clojure
(extract-nested-data
  {:user {:profile {:name "Bob Jones"}}}
  {:paths [[:user :profile :name]
           [:user :profile :id]
           [:user :settings :role]]
   :required #{[:user :profile :id] [:user :settings :role]}
   :aliases {[:user :profile :name] :name
             [:user :profile :id] :user-id
             [:user :settings :role] :role}})
;; => {:name "Bob Jones"
;;     :extraction-errors ["Required path not found: [:user :profile :id]"
;;                        "Required path not found: [:user :settings :role]"]}
```

### Example 4: Array navigation
```clojure
(extract-nested-data
  {:orders [{:id 1 :total 100}
            {:id 2 :total 200}]
   :user {:name "Alice"}}
  {:paths [[:orders 0 :id]
           [:orders 1 :total]
           [:user :name]]
   :aliases {[:orders 0 :id] :first-order-id
             [:orders 1 :total] :second-order-total
             [:user :name] :customer-name}})
;; => {:first-order-id 1
;;     :second-order-total 200
;;     :customer-name "Alice"}
```

## Tips

- Use `get-in` for safe nested navigation
- Handle both keyword and string keys
- Use `reduce` to navigate paths step by step
- Collect errors rather than failing fast
- Consider using `try-catch` for array index errors
- Test edge cases: empty paths, nil values, missing keys

## Testing your solution

```bash
cd challenges/079-nested-data-extractor/
clj -M solution.clj
```
Then in the REPL:
```clojure
(require 'challenge-079.solution)
(challenge-079.solution/-test)
```
