# 054 - Database to Domain (Part 2 of Bidirectional Pair)

**Level**: 11/18
**Type**: Adapter
**Concepts**: Bidirectional transformation, Database → Domain, kebab-case conversion

## Context

This is Part 2 of the bidirectional transformation pair (Challenge 053 implemented domain → database). When loading data from the database, we need to transform it back into the domain model format that the application uses.

## Objective

Implement an adapter that transforms a database record into a domain user entity with kebab-case keys and keyword enums.

## Specification

### Input

- `db-record` (map): Database record with snake_case keys
  ```clojure
  {:user_id "..."
   :full_name "..."
   :email_address "..."
   :account_status "..."  ; string
   :created_at "..."}
  ```

### Output

- (map): Domain entity with kebab-case keys
  ```clojure
  {:user-id "..."
   :full-name "..."
   :email-address "..."
   :account-status :...  ; keyword
   :created-at "..."}
  ```

### Rules

- Transform snake_case → kebab-case:
  - `:user_id` → `:user-id`
  - `:full_name` → `:full-name`
  - `:email_address` → `:email-address`
  - `:account_status` → `:account-status`
  - `:created_at` → `:created-at`
- Convert `:account_status` string → keyword
  - `"active"` → `:active`
  - `"suspended"` → `:suspended`
  - etc.
- Keep values unchanged except status conversion
- Function must be pure

## Examples

### Example 1
```clojure
(db->domain
  {:user_id "USER-123"
   :full_name "Alice Johnson"
   :email_address "alice@example.com"
   :account_status "active"
   :created_at "2023-01-15T10:00:00"})
;; => {:user-id "USER-123"
;;     :full-name "Alice Johnson"
;;     :email-address "alice@example.com"
;;     :account-status :active
;;     :created-at "2023-01-15T10:00:00"}
```

### Example 2
```clojure
(db->domain
  {:user_id "USER-456"
   :full_name "Bob Smith"
   :email_address "bob@example.com"
   :account_status "suspended"
   :created_at "2024-01-01T08:30:00"})
;; => {:user-id "USER-456"
;;     :full-name "Bob Smith"
;;     :email-address "bob@example.com"
;;     :account-status :suspended
;;     :created-at "2024-01-01T08:30:00"}
```

## Tips

- Destructure input, this time using snake_case keys
- Build output map with kebab-case keys
- Convert string to keyword: `(keyword "active")` → `:active`
- This is the exact inverse of Challenge 053
- Round-trip test: `(db->domain (domain->db entity))` should equal `entity`

## Testing your solution

```bash
cd challenges/054-db-to-domain/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-054.solution)
(challenge-054.solution/-test)
```

## Related Challenge

See Challenge 053 for the inverse transformation (domain→db).
