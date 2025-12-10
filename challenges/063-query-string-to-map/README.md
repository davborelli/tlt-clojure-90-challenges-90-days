# 063 - Query String to Map (Bidirectional Part 1)

**Level**: 13/18
**Type**: Adapter
**Concepts**: Query string parsing, String manipulation, Bidirectional transformation

## Context

Web applications receive query parameters as URL-encoded strings (`"key1=value1&key2=value2"`). To work with these parameters internally, we need to parse them into Clojure maps with keyword keys. This is Part 1 of a bidirectional transformation (Part 2 in Challenge 064 converts maps back to query strings).

## Objective

Implement an adapter that parses URL query strings into Clojure maps with keyword keys and string values.

## Specification

### Input

- `query-string` (string): URL query parameters
  - Format: `"key1=value1&key2=value2&key3=value3"`
  - Keys and values are alphanumeric (no special URL encoding needed for this challenge)

### Output

- (map): Parsed parameters as map
  ```clojure
  {:key1 "value1" :key2 "value2" :key3 "value3"}
  ```

### Rules

- Split string by `&` to get key-value pairs
- Split each pair by `=` to separate key from value
- Convert keys to keywords (`:key1`, `:key2`)
- Keep values as strings
- Handle empty string input → return empty map `{}`
- Assume no duplicate keys (each key appears once)
- Assume well-formed input (always `key=value` format)

## Examples

### Example 1
```clojure
(query-string->map "name=John&age=25&city=NYC")
;; => {:name "John" :age "25" :city "NYC"}
```

### Example 2
```clojure
(query-string->map "status=active&verified=true")
;; => {:status "active" :verified "true"}
```

### Example 3
```clojure
(query-string->map "")
;; => {}
```

## Tips

- Use `clojure.string/split` to split by `&` and `=`
- Pattern: `(str/split query-string #"&")` splits by ampersand
- Pattern: `(str/split pair #"=")` splits key-value pair
- Use `map` to process each pair
- Convert string key to keyword with `keyword`
- Use `into {}` to build map from key-value pairs
- Handle empty string as special case

## Testing your solution

```bash
cd challenges/063-query-string-to-map/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-063.solution)
(challenge-063.solution/-test)
```

## Related Challenge

See Challenge 064 for the inverse transformation (map → query-string).
