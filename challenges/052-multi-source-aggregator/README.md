# 052 - Multi-Source Aggregator

**Level**: 11/18
**Type**: Pure Function
**Concepts**: merge function, Data aggregation, Multiple source handling

## Context

Applications often need to aggregate data from multiple sources to build a complete view. For example, user data might come from a user service, preferences from a settings service, and activity from an analytics service. Merging these sources while handling missing data and calculating summaries requires careful composition.

## Objective

Implement a function that aggregates user data from three different sources, merges them intelligently, and calculates summary statistics.

## Specification

### Input

Three separate data sources (all maps):

1. `user-profile` - Basic user information
   ```clojure
   {:user-id "..." :name "..." :email "..." :join-date "..."}
   ```

2. `user-preferences` - User settings (may be empty/nil)
   ```clojure
   {:theme "..." :language "..." :notifications boolean}
   ```

3. `user-activity` - Activity metrics
   ```clojure
   {:last-login "..." :login-count ... :posts-count ...}
   ```

### Output

- (map): Aggregated user data with calculated fields
  ```clojure
  {:user-id "..."
   :name "..."
   :email "..."
   :join-date "..."
   :theme "..." ; or "default" if missing
   :language "..." ; or "en" if missing
   :notifications boolean ; or false if missing
   :last-login "..."
   :login-count ...
   :posts-count ...
   :activity-level :... ; calculated based on login-count}
  ```

### Rules

- Merge all three sources using `merge` (profile, preferences, activity)
- Handle missing preferences with defaults:
  - `:theme` defaults to `"default"`
  - `:language` defaults to `"en"`
  - `:notifications` defaults to `false`
- Calculate `:activity-level` based on `:login-count`:
  - `>= 100` → `:very-active`
  - `>= 50` → `:active`
  - `>= 10` → `:moderate`
  - `< 10` → `:low`
- Function must handle `nil` preferences gracefully
- Function must be pure

## Examples

### Example 1
```clojure
(aggregate-user-data
  {:user-id "USER-1" :name "Alice" :email "alice@example.com" :join-date "2023-01-15"}
  {:theme "dark" :language "pt" :notifications true}
  {:last-login "2024-01-15" :login-count 150 :posts-count 45})
;; => {:user-id "USER-1"
;;     :name "Alice"
;;     :email "alice@example.com"
;;     :join-date "2023-01-15"
;;     :theme "dark"
;;     :language "pt"
;;     :notifications true
;;     :last-login "2024-01-15"
;;     :login-count 150
;;     :posts-count 45
;;     :activity-level :very-active}
```

### Example 2 (missing preferences)
```clojure
(aggregate-user-data
  {:user-id "USER-2" :name "Bob" :email "bob@example.com" :join-date "2024-01-01"}
  nil
  {:last-login "2024-01-10" :login-count 5 :posts-count 2})
;; => {:user-id "USER-2"
;;     ...
;;     :theme "default"
;;     :language "en"
;;     :notifications false
;;     :login-count 5
;;     :activity-level :low}
```

## Tips

- Use `merge` to combine maps: `(merge map1 map2 map3)`
- Later maps override earlier ones in merge
- Provide defaults before merging: `(merge {:theme "default"} preferences)`
- Use `or` for nil handling: `(or preferences {})`
- Calculate activity-level using `cond` based on login-count
- Pattern: `(merge defaults profile preferences activity calculated-fields)`

## Testing your solution

```bash
cd challenges/052-multi-source-aggregator/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-052.solution)
(challenge-052.solution/-test)
```
