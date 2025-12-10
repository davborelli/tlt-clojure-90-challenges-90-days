# 044 - Build Nested Report

**Level**: 9/18
**Type**: Adapter
**Concepts**: Reverse transformation (flat to nested), Calculated fields, Report generation

## Context

Reporting systems often need to transform flat database records into nested hierarchical structures suitable for JSON APIs, PDF reports, or dashboards. This is the reverse of typical adapters: instead of flattening, we're building structure by grouping related fields and calculating summaries.

## Objective

Implement an adapter function that transforms a flat transaction record into a nested report structure with calculated summary fields.

## Specification

### Input

- `flat-record` (map): Flat transaction record
  ```clojure
  {:transaction-id "..." :amount ... :fee ... :user-name "..."
   :user-email "..." :merchant-name "..." :merchant-category "..."}
  ```

### Output

- (map): Nested report structure
  ```clojure
  {:id "..."
   :financial {:amount ... :fee ... :net ...}
   :user {:name "..." :email "..."}
   :merchant {:name "..." :category "..."}}
  ```

### Rules

- Extract `:transaction-id` → `:id`
- Create `:financial` nested map:
  - `:amount` from `:amount`
  - `:fee` from `:fee`
  - `:net` calculated as `(- amount fee)`
- Create `:user` nested map:
  - `:name` from `:user-name`
  - `:email` from `:user-email`
- Create `:merchant` nested map:
  - `:name` from `:merchant-name`
  - `:category` from `:merchant-category`
- Function must be pure

## Examples

### Example 1
```clojure
(build-report
  {:transaction-id "TXN-123"
   :amount 100.00
   :fee 2.50
   :user-name "John Doe"
   :user-email "john@example.com"
   :merchant-name "Coffee Shop"
   :merchant-category "dining"})
;; => {:id "TXN-123"
;;     :financial {:amount 100.0 :fee 2.5 :net 97.5}
;;     :user {:name "John Doe" :email "john@example.com"}
;;     :merchant {:name "Coffee Shop" :category "dining"}}
```

### Example 2
```clojure
(build-report
  {:transaction-id "TXN-456"
   :amount 1500.00
   :fee 45.00
   :user-name "Jane Smith"
   :user-email "jane@example.com"
   :merchant-name "Electronics Store"
   :merchant-category "retail"})
;; => {:id "TXN-456"
;;     :financial {:amount 1500.0 :fee 45.0 :net 1455.0}
;;     :user {:name "Jane Smith" :email "jane@example.com"}
;;     :merchant {:name "Electronics Store" :category "retail"}}
```

## Tips

- Destructure input with `:keys`: `{:keys [transaction-id amount fee ...]}`
- Calculate net: `(- amount fee)`
- Build nested maps with literal syntax
- Group related fields logically: financial, user, merchant
- Consider using `let` to bind calculated values
- This is the reverse of flattening: flat → nested

## Testing your solution

```bash
cd challenges/044-build-nested-report/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-044.solution)
(challenge-044.solution/-test)
```
