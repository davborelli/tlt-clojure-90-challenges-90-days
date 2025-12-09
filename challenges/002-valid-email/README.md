# 002 - Valid Email

**Level**: 1/18
**Type**: Pure Function
**Concepts**: String functions, Predicates, Boolean logic

## Context

Email validation is a fundamental requirement in user registration systems. While full email validation is complex, a basic check for the presence of an @ symbol is a good starting point.

## Objective

Implement a pure function that checks if a string contains an @ symbol, indicating it might be a valid email address.

## Specification

### Input

- `email` (string): A string that might be an email address

### Output

- (boolean): `true` if the string contains @, `false` otherwise

### Rules

- Must check for the presence of @ character
- Empty strings should return false
- Function must be pure

## Examples

### Example 1
```clojure
(valid-email? "user@example.com")
;; => true
```

### Example 2
```clojure
(valid-email? "invalid-email")
;; => false
```

### Example 3
```clojure
(valid-email? "")
;; => false
```

## Tips

- Use `clojure.string/includes?` to check if string contains a character
- Remember to require `[clojure.string :as str]`
- Consider what happens with empty strings

## Testing your solution

```bash
cd challenges/002-valid-email/
clj -M solution.clj
```
