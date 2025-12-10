# 016 - Count Valid Emails

**Level**: 4/18
**Type**: Pure Function
**Concepts**: Function composition, filter + count, higher-order functions

## Context

When processing user data from multiple sources, it's common to need statistics about data quality. Counting how many valid emails exist in a dataset is a typical data validation task that combines filtering with aggregation.

## Objective

Implement a pure function that counts how many valid email addresses exist in a collection of email strings. An email is considered valid if it contains the "@" symbol and is not blank.

## Specification

### Input

- `emails` (collection): A collection of email strings (may include invalid or blank entries)

### Output

- (integer): The count of valid email addresses

### Rules

- A valid email must contain the "@" symbol
- A valid email must not be blank (empty or only whitespace)
- Use composition of filter and count operations
- Function must be pure

## Examples

### Example 1
```clojure
(count-valid-emails ["john@example.com" "invalid" "jane@test.com"])
;; => 2
```

### Example 2
```clojure
(count-valid-emails ["test@test.com" "" "   " "user@domain.org"])
;; => 2
```

### Example 3
```clojure
(count-valid-emails ["invalid" "no-at-sign" ""])
;; => 0
```

### Example 4
```clojure
(count-valid-emails [])
;; => 0
```

## Tips

- Combine `filter` with a validation predicate
- Use `count` on the filtered result
- Reuse the validation logic from challenge 002 (valid-email?)
- Consider using `clojure.string/blank?` and `clojure.string/includes?`
- Can be expressed as composition: count after filtering

## Testing your solution

```bash
cd challenges/016-count-valid-emails/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-016.solution)
(challenge-016.solution/-test)
```
