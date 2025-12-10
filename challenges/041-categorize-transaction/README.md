# 041 - Categorize Transaction

**Level**: 9/18
**Type**: Pure Function
**Concepts**: Pattern matching, cond with multiple branches, Business rules, Complex conditionals

## Context

Financial systems automatically categorize transactions for reporting, budgeting, and fraud detection. Categorization depends on multiple factors: transaction type, amount, merchant category, and user settings. The rules form a decision tree that must be evaluated in the correct order.

## Objective

Implement a pure function that categorizes a financial transaction based on type, amount, merchant category, and priority flag.

## Specification

### Input

- `transaction` (map): Transaction with `:type`, `:amount`, `:merchant-category`, `:priority`

### Output

- (keyword): Category (`:urgent`, `:high-value`, `:travel`, `:dining`, `:shopping`, `:bills`, `:other`)

### Rules

**Priority order (check from top to bottom, return first match):**
1. If `:priority` is `true` → `:urgent`
2. If `:amount` > 5000 → `:high-value`
3. If `:type` is `:transfer` AND `:amount` > 1000 → `:high-value`
4. If `:merchant-category` is `"travel"` → `:travel`
5. If `:merchant-category` is `"restaurant"` → `:dining`
6. If `:merchant-category` is `"retail"` → `:shopping`
7. If `:type` is `:bill-payment` → `:bills`
8. Otherwise → `:other`

- Function must be pure

## Examples

### Example 1
```clojure
(categorize-transaction {:type :purchase :amount 100 :merchant-category "restaurant" :priority false})
;; => :dining
```

### Example 2
```clojure
(categorize-transaction {:type :transfer :amount 10000 :merchant-category "bank" :priority false})
;; => :high-value
```

### Example 3
```clojure
(categorize-transaction {:type :purchase :amount 50 :merchant-category "other" :priority true})
;; => :urgent
```

### Example 4
```clojure
(categorize-transaction {:type :bill-payment :amount 200 :merchant-category "utilities" :priority false})
;; => :bills
```

## Tips

- Use `cond` to check conditions in priority order
- Check `:priority` flag first (highest priority)
- Check `:amount` thresholds next
- Use `and` for compound conditions (type AND amount)
- Check `:merchant-category` with `=`
- Use `:else` for default case
- Order matters! Check more specific rules before general ones

## Testing your solution

```bash
cd challenges/041-categorize-transaction/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-041.solution)
(challenge-041.solution/-test)
```
