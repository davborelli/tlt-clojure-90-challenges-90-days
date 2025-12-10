# 081 - Distributed Cache Manager

**Level**: 17/18
**Type**: Pure Function
**Concepts**: Cache invalidation, TTL (Time To Live), Dependency tracking, LRU eviction

## Context

Distributed caching is critical for high-performance applications. A sophisticated cache manager must handle time-based expiration (TTL), dependency-based invalidation (when key A changes, invalidate keys B and C), and capacity management (evict least recently used items). This is essential for web applications, APIs, and microservices that need to balance consistency with performance.

## Objective

Implement a pure functional cache manager that handles TTL expiration, dependency tracking for cascading invalidation, LRU (Least Recently Used) eviction, and efficient cache operations.

## Specification

### Input

- `cache-state` (map): Current cache state with:
  - `:entries` (map): Cached entries with metadata
  - `:dependencies` (map): Key dependencies (key → set of dependent keys)
  - `:access-order` (vector): Keys in LRU order
  - `:current-time` (number): Current timestamp
  - `:max-size` (number): Maximum cache capacity
- `operation` (map): Cache operation with:
  - `:op` (keyword): One of `:get`, `:put`, `:invalidate`, `:clean-expired`
  - `:key`: Key to operate on (for get/put/invalidate)
  - `:value`: Value to cache (for put)
  - `:ttl`: Time to live in ms (for put, optional)
  - `:depends-on`: Set of keys this entry depends on (for put, optional)

### Output

- (map): New cache state with updated entries, dependencies, and access order
  - `:result` (optional): Retrieved value for :get operations
  - `:evicted` (optional): Keys evicted due to capacity or TTL
  - `:invalidated` (optional): Keys invalidated due to dependencies

### Rules

- `:get` - Retrieve value if present and not expired, update LRU order
- `:put` - Add/update entry with TTL and dependencies, evict if at capacity
- `:invalidate` - Remove key and all dependent keys (cascade)
- `:clean-expired` - Remove all expired entries
- Expired entries should not be returned by :get
- LRU eviction when cache reaches max-size
- Dependency invalidation should cascade (A→B→C all invalidated)

## Examples

### Example 1: Basic put and get
```clojure
(manage-cache
  {:entries {}
   :dependencies {}
   :access-order []
   :current-time 1000
   :max-size 100}
  {:op :put
   :key :user-123
   :value {:name "John" :age 30}
   :ttl 5000})
;; => {:entries {:user-123 {:value {:name "John" :age 30}
;;                          :expires-at 6000
;;                          :created-at 1000}}
;;     :dependencies {}
;;     :access-order [:user-123]
;;     :current-time 1000
;;     :max-size 100}
```

### Example 2: Get with LRU update
```clojure
(manage-cache
  {:entries {:user-123 {:value "data1" :expires-at 9999999}
             :user-456 {:value "data2" :expires-at 9999999}}
   :dependencies {}
   :access-order [:user-123 :user-456]
   :current-time 1000
   :max-size 100}
  {:op :get
   :key :user-123})
;; => {:entries {...}
;;     :access-order [:user-456 :user-123]  ; user-123 moved to end (most recent)
;;     :result "data1"
;;     ...}
```

### Example 3: Dependency-based invalidation
```clojure
(manage-cache
  {:entries {:orders {:value [...] :expires-at 9999}
             :order-summary {:value {...} :expires-at 9999}
             :order-stats {:value {...} :expires-at 9999}}
   :dependencies {:orders #{:order-summary :order-stats}}
   :access-order [:orders :order-summary :order-stats]
   :current-time 1000
   :max-size 100}
  {:op :invalidate
   :key :orders})
;; => {:entries {}
;;     :dependencies {}
;;     :access-order []
;;     :invalidated [:orders :order-summary :order-stats]
;;     ...}
```

### Example 4: TTL expiration
```clojure
(manage-cache
  {:entries {:user-123 {:value "data" :expires-at 2000}}
   :dependencies {}
   :access-order [:user-123]
   :current-time 3000
   :max-size 100}
  {:op :get
   :key :user-123})
;; => {:entries {:user-123 {...}}  ; Still in cache but expired
;;     :result nil                   ; Returns nil (expired)
;;     ...}
```

## Tips

- Use atoms or refs for mutable state in production
- This challenge focuses on pure transformation functions
- Consider using assoc-in and update-in for nested updates
- Track dependencies bidirectionally for efficient invalidation
- LRU: move accessed items to end of order vector
- Expired items can stay in cache until cleaned or accessed

## Testing your solution

```bash
cd challenges/081-distributed-cache-manager/
clj -M solution.clj
```
Then in the REPL:
```clojure
(require 'challenge-081.solution)
(challenge-081.solution/-test)
```
