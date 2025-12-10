# 018 - Parse String Numbers to Integers

**Level**: 4/18
**Type**: Adapter
**Concepts**: Type coercion, String to Integer conversion, Data normalization

## Context

Data often arrives from external systems (APIs, forms, CSV files) as strings, even when representing numbers. Before performing calculations or comparisons, we need to parse these strings into proper numeric types. This is a fundamental data normalization task.

## Objective

Implement an adapter function that transforms a map where age and score are strings into a map where they are integers.

## Specification

### Input

- `user-data` (map): A map with `:name` (string), `:age` (string), and `:score` (string)

### Output

- (map): Same structure but with `:age` and `:score` converted to integers

### Rules

- Convert `:age` from string to integer
- Convert `:score` from string to integer
- Keep `:name` as string (unchanged)
- Preserve all fields
- Assume input strings contain valid integers
- Function must be pure

## Examples

### Example 1
```clojure
(parse-numbers {:name "John" :age "25" :score "100"})
;; => {:name "John" :age 25 :score 100}
```

### Example 2
```clojure
(parse-numbers {:name "Jane" :age "30" :score "95"})
;; => {:name "Jane" :age 30 :score 95}
```

### Example 3
```clojure
(parse-numbers {:name "Bob" :age "18" :score "0"})
;; => {:name "Bob" :age 18 :score 0}
```

## Tips

- Use `Integer/parseInt` to convert string to integer
- Use `update` to transform specific fields
- Can chain multiple `update` calls with `->`
- Keep `:name` unchanged (strings stay as strings)
- Formula: `(Integer/parseInt "25")` => `25`

## Testing your solution

```bash
cd challenges/018-parse-numbers/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-018.solution)
(challenge-018.solution/-test)
```
