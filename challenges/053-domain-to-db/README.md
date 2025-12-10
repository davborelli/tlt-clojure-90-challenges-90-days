# 053 - Domain to Database (Part 1 of Bidirectional Pair)

**Level**: 11/18
**Type**: Adapter
**Concepts**: Bidirectional transformation, Domain → Database, snake_case conversion

## Context

Applications often maintain two representations of the same data: a domain model (how the application thinks about data) and a database schema (how data is stored). Domain models use kebab-case keywords and nested structures, while databases use snake_case columns and flat rows. Adapters transform between these representations.

This is Part 1 of a bidirectional pair. Challenge 054 implements the reverse transformation (database → domain).

## Objective

Implement an adapter that transforms a domain user entity into a flat database record with snake_case column names.

## Specification

### Input

- `domain-user` (map): Domain model with kebab-case keys and nested structure
  ```clojure
  {:user-id "..."
   :full-name "..."
   :email-address "..."
   :account-status :...
   :created-at "..."}
  ```

### Output

- (map): Database record with snake_case keys
  ```clojure
  {:user_id "..."
   :full_name "..."
   :email_address "..."
   :account_status "..."  ; keyword → string
   :created_at "..."}
  ```

### Rules

- Transform kebab-case → snake_case:
  - `:user-id` → `:user_id`
  - `:full-name` → `:full_name`
  - `:email-address` → `:email_address`
  - `:account-status` → `:account_status`
  - `:created-at` → `:created_at`
- Convert `:account-status` keyword → string
  - `:active` → `"active"`
  - `:suspended` → `"suspended"`
  - etc.
- Keep values unchanged except status conversion
- Function must be pure

## Examples

### Example 1
```clojure
(domain->db
  {:user-id "USER-123"
   :full-name "Alice Johnson"
   :email-address "alice@example.com"
   :account-status :active
   :created-at "2023-01-15T10:00:00"})
;; => {:user_id "USER-123"
;;     :full_name "Alice Johnson"
;;     :email_address "alice@example.com"
;;     :account_status "active"
;;     :created_at "2023-01-15T10:00:00"}
```

### Example 2
```clojure
(domain->db
  {:user-id "USER-456"
   :full-name "Bob Smith"
   :email-address "bob@example.com"
   :account-status :suspended
   :created-at "2024-01-01T08:30:00"})
;; => {:user_id "USER-456"
;;     :full_name "Bob Smith"
;;     :email_address "bob@example.com"
;;     :account_status "suspended"
;;     :created_at "2024-01-01T08:30:00"}
```

## Tips

- Destructure input with `:keys`
- Build output map with snake_case keys directly
- Convert keyword to string: `(name :active)` → `"active"`
- This transformation is the inverse of kebab-case → snake_case
- Keep it simple: direct map construction works well

## Testing your solution

```bash
cd challenges/053-domain-to-db/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-053.solution)
(challenge-053.solution/-test)
```

## Related Challenge

See Challenge 054 for the reverse transformation (db→domain).
