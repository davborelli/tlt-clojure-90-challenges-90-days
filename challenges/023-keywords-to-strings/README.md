# 023 - Convert Keywords to Strings

**Level**: 5/18
**Type**: Adapter
**Concepts**: Type transformation, reduce-kv, keyword/string conversion

## Context

Some external systems (legacy APIs, certain databases) require string keys instead of keywords. When preparing data for these systems, we need to convert all keyword keys to string keys while preserving values.

## Objective

Implement an adapter function that converts all keyword keys in a map to string keys.

## Specification

### Input

- `data` (map): A map with keyword keys

### Output

- (map): Same map with string keys (without leading colon)

### Rules

- Convert all keyword keys to strings
- Remove the leading colon (`:name` → `"name"`)
- Preserve all values unchanged
- Function must be pure

## Examples

### Example 1
```clojure
(keywords->strings {:name "John" :age 25})
;; => {"name" "John" "age" 25}
```

### Example 2
```clojure
(keywords->strings {:user-id 123 :email "test@example.com"})
;; => {"user-id" 123 "email" "test@example.com"}
```

### Example 3
```clojure
(keywords->strings {})
;; => {}
```

## Tips

- Use `name` to convert keyword to string
- Use `reduce-kv` to build new map
- Pattern: `:keyword` → "keyword" (no colon in result)

## Testing your solution

```bash
cd challenges/023-keywords-to-strings/
clj -M solution.clj
```
