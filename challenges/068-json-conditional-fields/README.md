# 068 - JSON Conditional Fields

**Level**: 14/18
**Type**: Adapter
**Concepts**: Conditional field inclusion, assoc-some pattern, Optional fields

## Context

When serializing data to JSON for APIs, some fields should only be included if they have meaningful values. For example, include `errorMessage` only if there was an error, or `discountCode` only if a discount was applied. This prevents sending null/nil values and reduces payload size.

## Objective

Implement an adapter that builds JSON response maps conditionally including fields based on whether they have non-nil values.

## Specification

### Input

- `response-data` (map): Raw response data with all fields (some may be nil)
  ```clojure
  {:status :...
   :user-id "..."
   :success-message "..." or nil
   :error-message "..." or nil
   :data {...} or nil
   :metadata {...} or nil}
  ```

### Output

- (map): JSON response with only non-nil fields
  ```clojure
  {:status "..."
   :userId "..."
   :successMessage "..."  ; Only if non-nil
   :errorMessage "..."    ; Only if non-nil
   :data {...}            ; Only if non-nil
   :metadata {...}}       ; Only if non-nil
  ```

### Rules

**Always include:**
- `:status` → `:status` (convert keyword to string)
- `:user-id` → `:userId` (always present)

**Conditionally include (only if non-nil):**
- `:success-message` → `:successMessage`
- `:error-message` → `:errorMessage`
- `:data` → `:data`
- `:metadata` → `:metadata`

**Transformations:**
- kebab-case → camelCase for keys
- Keyword → string for `:status` (`:success` → `"success"`)
- If field is nil, do NOT include in output

## Examples

### Example 1 (success with data)
```clojure
(build-json-response {:status :success :user-id "U123" :success-message "Operation completed" :error-message nil :data {:result "OK"} :metadata nil})
;; => {:status "success" :userId "U123" :successMessage "Operation completed" :data {:result "OK"}}
```

### Example 2 (error with message)
```clojure
(build-json-response {:status :error :user-id "U456" :success-message nil :error-message "Invalid input" :data nil :metadata {:timestamp "2024-01-15"}})
;; => {:status "error" :userId "U456" :errorMessage "Invalid input" :metadata {:timestamp "2024-01-15"}}
```

### Example 3 (minimal - only required fields)
```clojure
(build-json-response {:status :pending :user-id "U789" :success-message nil :error-message nil :data nil :metadata nil})
;; => {:status "pending" :userId "U789"}
```

## Tips

- Use helper function to conditionally add fields
- Pattern: `(defn assoc-some [m k v] (if (some? v) (assoc m k v) m))`
- `some?` returns true for non-nil values
- Thread through transformations with `->`
- Start with required fields, conditionally add optional ones
- Convert `:status` keyword to string with `name`

## Testing your solution

```bash
cd challenges/068-json-conditional-fields/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-068.solution)
(challenge-068.solution/-test)
```
