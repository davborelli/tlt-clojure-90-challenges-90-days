;; =============================================================================
;; 081 - DISTRIBUTED CACHE MANAGER
;; Level: 17/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; Caching is one of the most important performance optimizations in software.
;; However, naive caching leads to stale data, memory leaks, and consistency
;; issues. A production-grade cache must handle: TTL (time-based expiration),
;; capacity management (LRU eviction), and dependency tracking (invalidate
;; related entries when source data changes).
;;
;; This implementation models a cache as a pure data structure with explicit
;; time tracking. Each operation returns a new cache state, making it fully
;; testable and predictable. In production, this would be wrapped with atoms
;; or refs for concurrency, but the core logic remains pure.
;;
;; The challenge demonstrates several advanced patterns: dependency graphs for
;; cascading invalidation (when order data changes, invalidate order summaries
;; and statistics), LRU tracking using vectors (most recent at end), TTL
;; enforcement at read time (lazy expiration), and capacity management with
;; eviction policies. These patterns are essential in distributed systems,
;; CDNs, and high-traffic web applications.

(ns challenge-081.solution
  (:require [clojure.set :as set]))

;; IMPLEMENTATION
;; --------------

(defn- expired?
  "Checks if cache entry has expired based on current time"
  [entry current-time]
  (and (:expires-at entry)
       (>= current-time (:expires-at entry))))

(defn- update-lru-order
  "Updates LRU access order by moving key to end (most recent)"
  [access-order key]
  (let [without-key (filterv #(not= % key) access-order)]
    (conj without-key key)))

(defn- find-dependent-keys
  "Recursively finds all keys that depend on given key (transitive closure)"
  [dependencies key]
  (loop [to-check #{key}
         result #{}]
    (if (empty? to-check)
      result
      (let [current (first to-check)
            deps (get dependencies current #{})
            new-to-check (set/difference (set/union (rest to-check) deps) result)]
        (recur new-to-check (conj result current))))))

(defn- remove-dependencies
  "Removes all dependency references for given keys"
  [dependencies keys-to-remove]
  (let [key-set (set keys-to-remove)]
    (reduce
      (fn [deps key]
        (dissoc deps key))
      dependencies
      keys-to-remove)))

(defn- evict-lru
  "Evicts least recently used entry if cache is at capacity"
  [cache-state]
  (if (>= (count (:entries cache-state)) (:max-size cache-state))
    (let [lru-key (first (:access-order cache-state))]
      (-> cache-state
          (update :entries dissoc lru-key)
          (update :access-order (fn [order] (vec (rest order))))
          (update :dependencies remove-dependencies [lru-key])
          (assoc :evicted [lru-key])))
    cache-state))

(defn- cache-get
  "Retrieves value from cache if present and not expired"
  [cache-state key]
  (let [entry (get-in cache-state [:entries key])
        current-time (:current-time cache-state)]
    (if (and entry (not (expired? entry current-time)))
      (-> cache-state
          (update :access-order update-lru-order key)
          (assoc :result (:value entry)))
      (assoc cache-state :result nil))))

(defn- cache-put
  "Adds or updates cache entry with TTL and dependencies"
  [cache-state key value ttl depends-on]
  (let [current-time (:current-time cache-state)
        expires-at (when ttl (+ current-time ttl))
        entry {:value value
               :expires-at expires-at
               :created-at current-time}
        ;; Evict LRU if needed before adding new entry
        cache-state (if (and (not (contains? (:entries cache-state) key))
                            (>= (count (:entries cache-state)) (:max-size cache-state)))
                     (evict-lru cache-state)
                     cache-state)]
    (-> cache-state
        (assoc-in [:entries key] entry)
        (update :access-order update-lru-order key)
        (cond->
          depends-on (update :dependencies
                           (fn [deps]
                             (reduce
                               (fn [d dep-key]
                                 (update d dep-key (fnil conj #{}) key))
                               deps
                               depends-on)))))))

(defn- cache-invalidate
  "Invalidates key and all dependent keys (cascading)"
  [cache-state key]
  (let [all-keys (find-dependent-keys (:dependencies cache-state) key)
        remaining-entries (apply dissoc (:entries cache-state) all-keys)
        remaining-order (filterv #(not (contains? all-keys %)) (:access-order cache-state))
        remaining-deps (remove-dependencies (:dependencies cache-state) all-keys)]
    (-> cache-state
        (assoc :entries remaining-entries)
        (assoc :access-order remaining-order)
        (assoc :dependencies remaining-deps)
        (assoc :invalidated (vec all-keys)))))

(defn- cache-clean-expired
  "Removes all expired entries from cache"
  [cache-state]
  (let [current-time (:current-time cache-state)
        expired-keys (reduce-kv
                       (fn [acc k v]
                         (if (expired? v current-time)
                           (conj acc k)
                           acc))
                       []
                       (:entries cache-state))]
    (if (empty? expired-keys)
      cache-state
      (-> cache-state
          (update :entries (fn [entries] (apply dissoc entries expired-keys)))
          (update :access-order (fn [order] (filterv #(not (contains? (set expired-keys) %)) order)))
          (update :dependencies remove-dependencies expired-keys)
          (assoc :evicted expired-keys)))))

(defn manage-cache
  "Manages cache operations with TTL, dependencies, and LRU eviction.

  Parameters:
  - cache-state: Current cache state map
  - operation: Operation map with :op and operation-specific keys

  Returns: New cache state with operation result"
  [cache-state {:keys [op key value ttl depends-on]}]
  (case op
    :get (cache-get cache-state key)
    :put (cache-put cache-state key value ttl depends-on)
    :invalidate (cache-invalidate cache-state key)
    :clean-expired (cache-clean-expired cache-state)
    cache-state))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. TTL (Time To Live) and Lazy Expiration
;;    TTL prevents stale data by expiring entries after a time period. This
;;    implementation uses lazy expiration - entries remain in cache after TTL
;;    expires but are treated as missing when accessed. This is more efficient
;;    than active expiration (background thread checking all entries) for
;;    read-heavy workloads. Expired entries are cleaned on access or explicit
;;    clean operation. Production caches like Redis use similar strategies.
;;
;; 2. LRU (Least Recently Used) Eviction
;;    When cache reaches capacity, we must evict entries. LRU evicts the least
;;    recently accessed item, based on temporal locality principle - recently
;;    accessed data is likely to be accessed again. We track access order using
;;    a vector where most recent is at the end. On access, we move the key to
;;    the end. On eviction, we remove from the front. This is O(n) for updates
;;    but simple to implement; production systems use doubly-linked lists for O(1).
;;
;; 3. Dependency Tracking and Cascading Invalidation
;;    When source data changes, derived data must be invalidated. Example: when
;;    order data changes, order summaries and statistics become stale. We track
;;    dependencies as a graph: {:orders #{:order-summary :order-stats}}. When
;;    invalidating a key, we find all dependent keys recursively (transitive
;;    closure) and remove them. This prevents serving stale derived data.
;;
;; 4. Pure Functional State Management
;;    All cache operations are pure functions that return new state rather than
;;    mutating existing state. This makes the code testable, predictable, and
;;    suitable for time-travel debugging. In production, wrap with atom/ref for
;;    concurrent access: (swap! cache-atom manage-cache operation). The pure core
;;    logic is the same, but concurrency is handled at the edges.
;;
;; 5. Transitive Closure for Dependencies
;;    Finding all dependent keys requires computing transitive closure of the
;;    dependency graph. If A→B and B→C, invalidating A must invalidate B and C.
;;    We use iterative deepening: start with {A}, find dependencies {B}, find
;;    dependencies {C}, until no new dependencies found. This handles arbitrary
;;    dependency chains and cycles (though cycles should be prevented in practice).
;;
;; 6. Capacity Management and Eviction
;;    Unbounded caches lead to OutOfMemoryErrors. We enforce max-size by evicting
;;    before adding new entries. The evict-lru function removes the least recently
;;    used entry and updates all related structures (entries, access-order,
;;    dependencies). This keeps memory usage bounded while retaining hot data.
;;    Production systems may use memory-based limits instead of count-based.
;;
;; 7. Cache Consistency vs Performance Trade-offs
;;    Perfect consistency requires invalidating cache on every write, losing
;;    performance benefits. This implementation provides tools for managing the
;;    trade-off: TTL for time-bounded staleness, dependencies for logical
;;    consistency, and manual invalidation for immediate consistency. Applications
;;    choose the right strategy: financial data needs tight consistency, page
;;    content can tolerate minutes of staleness.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo3.md, exemplo5.md
;;
;; Pattern used: Complex conditional logic with state management
;;
;; Real-world usage: The reference examples show complex business logic with
;; multiple conditions and state tracking. Caching systems use similar patterns
;; for managing cache state, making decisions about eviction and invalidation,
;; and maintaining consistency.
;;
;; Production caching systems at companies like Nubank handle millions of
;; requests per second. Proper cache management with TTL, eviction policies,
;; and dependency tracking is critical for both performance and correctness.
;; The pure functional approach here makes the logic testable and composable.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Basic put and get
  (let [state (manage-cache
                {:entries {}
                 :dependencies {}
                 :access-order []
                 :current-time 1000
                 :max-size 100}
                {:op :put
                 :key :user-123
                 :value {:name "John" :age 30}
                 :ttl 5000})]
    state)
  ;; => {:entries {:user-123 {:value {:name "John" :age 30}
  ;;                          :expires-at 6000
  ;;                          :created-at 1000}}
  ;;     :access-order [:user-123]
  ;;     ...}

  ;; Example 2: Get with LRU update
  (manage-cache
    {:entries {:user-123 {:value "data1" :expires-at 999999}
               :user-456 {:value "data2" :expires-at 999999}}
     :dependencies {}
     :access-order [:user-123 :user-456]
     :current-time 1000
     :max-size 100}
    {:op :get
     :key :user-123})
  ;; => {:access-order [:user-456 :user-123]  ; Moved to end
  ;;     :result "data1"
  ;;     ...}

  ;; Example 3: Dependency-based cascading invalidation
  (manage-cache
    {:entries {:orders {:value [...] :expires-at 999999}
               :order-summary {:value {...} :expires-at 999999}
               :order-stats {:value {...} :expires-at 999999}}
     :dependencies {:orders #{:order-summary :order-stats}}
     :access-order [:orders :order-summary :order-stats]
     :current-time 1000
     :max-size 100}
    {:op :invalidate
     :key :orders})
  ;; => {:entries {}
  ;;     :invalidated [:orders :order-summary :order-stats]
  ;;     ...}

  ;; Example 4: TTL expiration on get
  (manage-cache
    {:entries {:user-123 {:value "data" :expires-at 2000}}
     :dependencies {}
     :access-order [:user-123]
     :current-time 3000
     :max-size 100}
    {:op :get
     :key :user-123})
  ;; => {:result nil  ; Expired, returns nil
  ;;     ...}

  ;; Example 5: LRU eviction at capacity
  (manage-cache
    {:entries {:key1 {:value "v1" :expires-at 999999}
               :key2 {:value "v2" :expires-at 999999}}
     :dependencies {}
     :access-order [:key1 :key2]
     :current-time 1000
     :max-size 2}
    {:op :put
     :key :key3
     :value "v3"
     :ttl 5000})
  ;; => {:entries {:key2 {...} :key3 {...}}
  ;;     :access-order [:key2 :key3]
  ;;     :evicted [:key1]  ; LRU evicted
  ;;     ...}

  ;; Example 6: Clean expired entries
  (manage-cache
    {:entries {:fresh {:value "data1" :expires-at 5000}
               :expired1 {:value "data2" :expires-at 1500}
               :expired2 {:value "data3" :expires-at 1800}}
     :dependencies {}
     :access-order [:fresh :expired1 :expired2]
     :current-time 2000
     :max-size 100}
    {:op :clean-expired})
  ;; => {:entries {:fresh {...}}
  ;;     :access-order [:fresh]
  ;;     :evicted [:expired1 :expired2]
  ;;     ...}

  ;; Example 7: Put with dependencies
  (manage-cache
    {:entries {:orders {:value [...] :expires-at 999999}}
     :dependencies {}
     :access-order [:orders]
     :current-time 1000
     :max-size 100}
    {:op :put
     :key :order-summary
     :value {:total 1000}
     :ttl 3000
     :depends-on #{:orders}})
  ;; => {:entries {:orders {...} :order-summary {...}}
  ;;     :dependencies {:orders #{:order-summary}}
  ;;     :access-order [:orders :order-summary]
  ;;     ...}
)

;; TESTS
;; -----

(defn -test []
  ;; Test basic put
  (let [result (manage-cache
                 {:entries {} :dependencies {} :access-order []
                  :current-time 1000 :max-size 100}
                 {:op :put :key :k1 :value "v1" :ttl 5000})]
    (assert (= (get-in result [:entries :k1 :value]) "v1")
            "Should store value in cache")
    (assert (= (get-in result [:entries :k1 :expires-at]) 6000)
            "Should set expiration time"))

  ;; Test get with valid entry
  (let [result (manage-cache
                 {:entries {:k1 {:value "v1" :expires-at 5000}}
                  :dependencies {}
                  :access-order [:k1]
                  :current-time 1000
                  :max-size 100}
                 {:op :get :key :k1})]
    (assert (= (:result result) "v1")
            "Should retrieve valid entry"))

  ;; Test get with expired entry
  (let [result (manage-cache
                 {:entries {:k1 {:value "v1" :expires-at 2000}}
                  :dependencies {}
                  :access-order [:k1]
                  :current-time 3000
                  :max-size 100}
                 {:op :get :key :k1})]
    (assert (nil? (:result result))
            "Should return nil for expired entry"))

  ;; Test LRU eviction
  (let [result (manage-cache
                 {:entries {:k1 {:value "v1" :expires-at 9999}
                           :k2 {:value "v2" :expires-at 9999}}
                  :dependencies {}
                  :access-order [:k1 :k2]
                  :current-time 1000
                  :max-size 2}
                 {:op :put :key :k3 :value "v3"})]
    (assert (not (contains? (:entries result) :k1))
            "Should evict LRU entry")
    (assert (= (:evicted result) [:k1])
            "Should report evicted key"))

  ;; Test dependency invalidation
  (let [result (manage-cache
                 {:entries {:k1 {:value "v1" :expires-at 9999}
                           :k2 {:value "v2" :expires-at 9999}}
                  :dependencies {:k1 #{:k2}}
                  :access-order [:k1 :k2]
                  :current-time 1000
                  :max-size 100}
                 {:op :invalidate :key :k1})]
    (assert (empty? (:entries result))
            "Should invalidate key and dependents")
    (assert (= (set (:invalidated result)) #{:k1 :k2})
            "Should report all invalidated keys"))

  ;; Test clean expired
  (let [result (manage-cache
                 {:entries {:k1 {:value "v1" :expires-at 5000}
                           :k2 {:value "v2" :expires-at 1500}}
                  :dependencies {}
                  :access-order [:k1 :k2]
                  :current-time 2000
                  :max-size 100}
                 {:op :clean-expired})]
    (assert (contains? (:entries result) :k1)
            "Should keep non-expired entries")
    (assert (not (contains? (:entries result) :k2))
            "Should remove expired entries"))

  ;; Test LRU order update on get
  (let [result (manage-cache
                 {:entries {:k1 {:value "v1" :expires-at 9999}
                           :k2 {:value "v2" :expires-at 9999}}
                  :dependencies {}
                  :access-order [:k1 :k2]
                  :current-time 1000
                  :max-size 100}
                 {:op :get :key :k1})]
    (assert (= (:access-order result) [:k2 :k1])
            "Should move accessed key to end of LRU order"))

  (println "✓ All tests passed! Distributed cache manager works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
