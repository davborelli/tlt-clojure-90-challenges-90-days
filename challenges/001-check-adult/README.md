# 001 - Check if Adult

**Level**: 1/18
**Type**: Pure Function
**Concepts**: Predicates, Comparison operators, Boolean values

## Context

In many systems, we need to verify if a person is of legal age (18 years or older) to grant access to certain features or content. This is a fundamental validation in age-restricted services, financial applications, and content platforms.

## Objective

Implement a pure function that checks if a given age represents an adult (18 years or older).

## Specification

### Input

- `age` (integer): The person's age in years

### Output

- (boolean): `true` if age >= 18, `false` otherwise

### Rules

- Legal age is defined as 18 years or older
- Function must be pure (same input = same output)
- No side effects allowed

## Examples

### Example 1
```clojure
(adult? 18)
;; => true
```

### Example 2
```clojure
(adult? 17)
;; => false
```

### Example 3
```clojure
(adult? 25)
;; => true
```

### Example 4
```clojure
(adult? 0)
;; => false
```

## Tips

- Use the `>=` comparison operator
- Comparison operators return boolean values directly
- The function should be a simple one-liner
- Think about the boolean nature of comparisons

## Testing your solution

```bash
cd challenges/001-check-adult/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-001.solution)
(challenge-001.solution/-test)
```
