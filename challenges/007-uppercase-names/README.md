# 007 - Uppercase Names

**Level**: 2/18
**Type**: Pure Function
**Concepts**: Collection transformation, map function, String manipulation

## Context

Data transformation is a core operation in functional programming. When displaying data to users or preparing it for export, we often need to transform all elements in a collection in the same way.

## Objective

Implement a pure function that transforms a list of names to uppercase.

## Specification

### Input

- `names` (vector): A vector of strings representing names

### Output

- (vector): A vector with all names converted to uppercase

### Rules

- Convert each name to uppercase
- Preserve the original order
- Empty strings remain empty
- Return empty vector for empty input
- Function must be pure

## Examples

### Example 1
```clojure
(uppercase-names ["john" "jane" "bob"])
;; => ["JOHN" "JANE" "BOB"]
```

### Example 2
```clojure
(uppercase-names ["Alice" "Charlie"])
;; => ["ALICE" "CHARLIE"]
```

### Example 3
```clojure
(uppercase-names [])
;; => []
```

### Example 4
```clojure
(uppercase-names ["" "test" ""])
;; => ["" "TEST" ""]
```

## Tips

- Use the `map` function to transform each element
- `map` takes a transformation function and a collection
- Use `clojure.string/upper-case` to convert strings to uppercase
- Remember to require `[clojure.string :as str]` in your namespace
- `mapv` returns a vector instead of a lazy sequence

## Testing your solution

```bash
cd challenges/007-uppercase-names/
clj -M solution.clj
```
