# 065 - Feature Flag Workflow

**Level**: 13/18
**Type**: Controller
**Concepts**: Threading macros, Feature flags, Conditional workflow, Multi-step orchestration

## Context

Feature flags (also called feature toggles) allow enabling/disabling features without code changes. A request processing workflow checks feature flags to determine which processing path to use. This enables A/B testing, gradual rollouts, and emergency kill switches.

## Objective

Implement a controller that processes requests through different workflows based on feature flags, using threading macros for clear data flow.

## Specification

### Input

- `request` (map): Request with feature flags
  ```clojure
  {:user-id ...
   :action "..."
   :data {...}
   :features {:enhanced-validation boolean
              :premium-processing boolean
              :analytics-tracking boolean}}
  ```

### Output

- (map): Processed request with added fields
  ```clojure
  {:user-id ...
   :action "..."
   :data {...}
   :features {...}
   :validation-result ...
   :processing-result ...
   :tracked boolean
   :status :completed}
  ```

### Rules

**Helper functions to implement:**

1. `basic-validation` - Always runs, adds `:validation-result "basic"`
2. `enhanced-validation` - Runs if `:enhanced-validation` flag is true, updates to `"enhanced"`
3. `standard-processing` - Adds `:processing-result "standard"`
4. `premium-processing` - If `:premium-processing` flag, updates to `"premium"`
5. `track-analytics` - If `:analytics-tracking` flag, adds `:tracked true`
6. `mark-completed` - Adds `:status :completed`

**Main function:**
- `process-request` - Uses `->` to thread request through pipeline
- Apply helpers conditionally based on feature flags
- Always: basic-validation → standard-processing → mark-completed
- Conditional: enhanced-validation, premium-processing, track-analytics

## Examples

### Example 1
```clojure
(process-request
  {:user-id "U123"
   :action "submit"
   :data {:amount 100}
   :features {:enhanced-validation false
              :premium-processing false
              :analytics-tracking false}})
;; => {:user-id "U123"
;;     :action "submit"
;;     :data {:amount 100}
;;     :features {...}
;;     :validation-result "basic"
;;     :processing-result "standard"
;;     :status :completed}
```

### Example 2
```clojure
(process-request
  {:user-id "U456"
   :action "submit"
   :data {:amount 500}
   :features {:enhanced-validation true
              :premium-processing true
              :analytics-tracking true}})
;; => {:user-id "U456"
;;     :action "submit"
;;     :data {:amount 500}
;;     :features {...}
;;     :validation-result "enhanced"
;;     :processing-result "premium"
;;     :tracked true
;;     :status :completed}
```

## Tips

- Use `->` to thread request through transformations
- Pattern for conditional steps: `(cond-> request condition (function))`
- Or use helper: `(defn apply-if [req flag f] (if flag (f req) req))`
- Each helper takes request, returns modified request
- Threading makes workflow explicit and readable
- Feature flags are in `(get-in request [:features :flag-name])`

## Testing your solution

```bash
cd challenges/065-feature-flag-workflow/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-065.solution)
(challenge-065.solution/-test)
```
