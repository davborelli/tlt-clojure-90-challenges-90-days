# 082 - Multi-Format Serializer

**Level**: 17/18
**Type**: Adapter
**Concepts**: Serialization, Format conversion, JSON/EDN/Transit, Protocol design

## Context

Modern applications need to serialize data to multiple formats for different contexts: JSON for REST APIs and browsers, EDN for Clojure systems, Transit for efficient wire protocols, and custom formats for legacy integration. A robust serializer must handle format-specific constraints (JSON doesn't support keywords), preserve type information, and provide consistent error handling.

## Objective

Implement a multi-format serialization system that converts Clojure data structures to and from JSON, EDN, and Transit formats, handling type preservation and format-specific constraints.

## Specification

### Input

- `data` (any): Clojure data structure to serialize/deserialize
- `format` (keyword): Target format - `:json`, `:edn`, or `:transit`
- `operation` (keyword): `:serialize` or `:deserialize`
- `options` (map, optional): Format-specific options
  - `:preserve-keywords` (boolean): Convert keywords to strings in JSON
  - `:pretty` (boolean): Pretty-print output
  - `:key-fn` (function): Custom key transformation

### Output

- For `:serialize`: String representation in target format
- For `:deserialize`: Clojure data structure
- Includes `:format` metadata and `:warnings` if type information lost

### Rules

- JSON: Keywords → strings with "__kw__" prefix for preservation
- EDN: Native Clojure format, preserves all types
- Transit: Efficient format, preserves most Clojure types
- Handle nil, numbers, strings, booleans, vectors, maps, sets
- Provide warnings when losing type information
- Support round-trip: data → serialize → deserialize → data (with type considerations)

## Examples

### Example 1: JSON serialization with keyword preservation
```clojure
(multi-serialize
  {:user-id 123 :name "John" :roles #{:admin :user}}
  :json
  :serialize
  {:preserve-keywords true})
;; => "{\"__kw__user-id\":123,\"__kw__name\":\"John\",\"__kw__roles\":[\"__kw__admin\",\"__kw__user\"]}"
```

### Example 2: EDN round-trip (lossless)
```clojure
(let [data {:id 1 :tags #{:clojure :functional}}
      serialized (multi-serialize data :edn :serialize {})
      deserialized (multi-serialize serialized :edn :deserialize {})]
  deserialized)
;; => {:id 1 :tags #{:clojure :functional}}  ; Exact match
```

### Example 3: JSON with type information loss warning
```clojure
(multi-serialize
  {:count 42 :tags #{:a :b :c}}
  :json
  :serialize
  {:preserve-keywords false})
;; => {:result "{\"count\":42,\"tags\":[\"a\",\"b\",\"c\"]}"
;;     :warnings ["Set converted to array (order not preserved)"
;;                "Keywords converted to strings"]}
```

## Tips

- Use `clojure.data.json` for JSON operations
- EDN can use `pr-str` and `read-string`
- Transit requires cognitect transit-clj library
- Consider using protocols for extensibility
- Handle circular references gracefully
- Benchmark different formats for performance trade-offs

## Testing your solution

```bash
cd challenges/082-multi-format-serializer/
clj -M solution.clj
```
Then in the REPL:
```clojure
(require 'challenge-082.solution)
(challenge-082.solution/-test)
```
