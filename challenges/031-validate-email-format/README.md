# 031 - Validate Email Format

**Level**: 7/18
**Type**: Pure Function
**Concepts**: String processing, Regular expressions, Validation patterns

## Context

Email validation is a common requirement in web applications and user registration systems. While complete RFC-compliant validation is complex, most systems use practical validation rules that check for basic format requirements: a username part, an @ symbol, and a domain part with at least one dot.

## Objective

Implement a pure function that validates email addresses using practical format rules, checking for username, @ symbol, and domain structure.

## Specification

### Input

- `email` (string): The email address to validate

### Output

- (boolean): `true` if email is valid, `false` otherwise

### Rules

- Email must contain exactly one `@` symbol
- Must have at least one character before `@` (username)
- Must have at least one character after `@` (domain)
- Domain must contain at least one `.` (dot)
- Must have at least one character after the last dot (TLD)
- Function must be pure

## Examples

### Example 1
```clojure
(valid-email-format? "user@example.com")
;; => true
```

### Example 2
```clojure
(valid-email-format? "invalid.email")
;; => false
```

### Example 3
```clojure
(valid-email-format? "@example.com")
;; => false
```

### Example 4
```clojure
(valid-email-format? "user@domain")
;; => false
```

## Tips

- Use `clojure.string/split` to separate parts by `@`
- Check the count of parts after splitting by `@` (should be 2)
- Use `clojure.string/includes?` to check for dot in domain
- Use `clojure.string/blank?` to verify non-empty parts
- Consider edge cases like multiple `@` symbols or missing domain

## Testing your solution

```bash
cd challenges/031-validate-email-format/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-031.solution)
(challenge-031.solution/-test)
```
