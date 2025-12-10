# 049 - Unflatten Config

**Level**: 10/18
**Type**: Adapter
**Concepts**: Building nested structures, assoc-in patterns, Reverse flattening

## Context

Configuration files are often stored in flat formats (environment variables, flat property files) but applications prefer hierarchical structures for organization and namespacing. This adapter does the reverse of flattening: it builds nested structures from flat key-value pairs, creating a hierarchical config map.

## Objective

Implement an adapter that transforms a flat configuration map into a nested structure organized by category (database, api, logging).

## Specification

### Input

- `flat-config` (map): Flat configuration with prefixed keys
  ```clojure
  {:db-host "..." :db-port ... :db-name "..."
   :api-base-url "..." :api-timeout ... :api-retry-count ...
   :log-level "..." :log-file "..."}
  ```

### Output

- (map): Nested configuration organized by category
  ```clojure
  {:database {:host "..." :port ... :name "..."}
   :api {:base-url "..." :timeout ... :retry-count ...}
   :logging {:level "..." :file "..."}}
  ```

### Rules

- Group `:db-*` keys → `:database` map, removing `db-` prefix
  - `:db-host` → `[:database :host]`
  - `:db-port` → `[:database :port]`
  - `:db-name` → `[:database :name]`
- Group `:api-*` keys → `:api` map, removing `api-` prefix
  - `:api-base-url` → `[:api :base-url]`
  - `:api-timeout` → `[:api :timeout]`
  - `:api-retry-count` → `[:api :retry-count]`
- Group `:log-*` keys → `:logging` map, removing `log-` prefix
  - `:log-level` → `[:logging :level]`
  - `:log-file` → `[:logging :file]`
- Use `assoc-in` to build nested structure
- Function must be pure

## Examples

### Example 1
```clojure
(unflatten-config
  {:db-host "localhost"
   :db-port 5432
   :db-name "myapp"
   :api-base-url "https://api.example.com"
   :api-timeout 30
   :api-retry-count 3
   :log-level "info"
   :log-file "/var/log/app.log"})
;; => {:database {:host "localhost" :port 5432 :name "myapp"}
;;     :api {:base-url "https://api.example.com" :timeout 30 :retry-count 3}
;;     :logging {:level "info" :file "/var/log/app.log"}}
```

### Example 2
```clojure
(unflatten-config
  {:db-host "db.example.com"
   :db-port 3306
   :db-name "production"
   :api-base-url "https://prod-api.example.com"
   :api-timeout 60
   :api-retry-count 5
   :log-level "warn"
   :log-file "/var/log/prod.log"})
;; => {:database {:host "db.example.com" :port 3306 :name "production"}
;;     :api {:base-url "https://prod-api.example.com" :timeout 60 :retry-count 5}
;;     :logging {:level "warn" :file "/var/log/prod.log"}}
```

## Tips

- Start with empty map, use `assoc-in` to build nested structure
- Pattern: `(-> {} (assoc-in [:database :host] val) (assoc-in [:database :port] val2) ...)`
- Or destructure flat map and build nested map directly
- Group related fields together for readability
- This is reverse transformation of flatten pattern

## Testing your solution

```bash
cd challenges/049-unflatten-config/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-049.solution)
(challenge-049.solution/-test)
```
