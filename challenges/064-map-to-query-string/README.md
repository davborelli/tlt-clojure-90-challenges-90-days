# 064 - Map to Query String (Bidirectional Part 2)

**Level**: 13/18
**Type**: Adapter
**Concepts**: Query string generation, String building, Bidirectional transformation

## Context

This is Part 2 of the bidirectional transformation pair (Challenge 063 implemented query-string → map). When making web API requests or constructing URLs, we need to convert Clojure maps into query string format for HTTP transmission.

## Objective

Implement an adapter that transforms Clojure maps into URL query strings.

## Specification

### Input

- `params-map` (map): Parameters as map with keyword keys and string values
  ```clojure
  {:key1 "value1" :key2 "value2" :key3 "value3"}
  ```

### Output

- (string): URL query string format
  - Format: `"key1=value1&key2=value2&key3=value3"`

### Rules

- Convert keyword keys to strings (`:name` → `"name"`)
- Keep values as strings
- Join key-value pairs with `&`
- Join key and value with `=`
- Handle empty map input → return empty string `""`
- Order of parameters doesn't matter (maps are unordered)
- Assume no special URL encoding needed (simple alphanumeric values)

## Examples

### Example 1
```clojure
(map->query-string {:name "John" :age "25" :city "NYC"})
;; => "name=John&age=25&city=NYC"
;; (order may vary)
```

### Example 2
```clojure
(map->query-string {:status "active" :verified "true"})
;; => "status=active&verified=true"
;; (order may vary)
```

### Example 3
```clojure
(map->query-string {})
;; => ""
```

## Tips

- Use `clojure.string/join` to join pairs with `&`
- Pattern: `(str/join "&" ["pair1" "pair2"])` → `"pair1&pair2"`
- Use `map` to transform map entries to `"key=value"` strings
- Convert keyword to string with `name`: `(name :key)` → `"key"`
- Build each pair: `(str (name k) "=" v)`
- Handle empty map as special case

## Testing your solution

```bash
cd challenges/064-map-to-query-string/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-064.solution)
(challenge-064.solution/-test)
```

## Related Challenge

See Challenge 063 for the inverse transformation (query-string → map).

## Round-Trip Property

These two functions should be inverses:
```clojure
(= original-map
   (-> original-map
       map->query-string
       query-string->map))
;; => true
```
