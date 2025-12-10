# 019 - Convert Snake Case to Kebab Case

**Level**: 4/18
**Type**: Adapter
**Concepts**: Key transformation, String manipulation, update-keys

## Context

Different systems use different naming conventions: databases often use snake_case (user_name), while Clojure idiomatically uses kebab-case (user-name). When adapting data between systems, we need to transform key naming conventions while preserving data values.

## Objective

Implement an adapter function that transforms all keys in a map from snake_case to kebab-case.

## Specification

### Input

- `data` (map): A map with snake_case keyword keys

### Output

- (map): Same map with keys transformed to kebab-case

### Rules

- Convert all keys from snake_case to kebab-case
- Replace underscores (_) with hyphens (-)
- Preserve all values unchanged
- Work with any number of keys
- Function must be pure

## Examples

### Example 1
```clojure
(snake->kebab {:first_name "John" :last_name "Doe"})
;; => {:first-name "John" :last-name "Doe"}
```

### Example 2
```clojure
(snake->kebab {:user_id 123 :email_address "test@example.com"})
;; => {:user-id 123 :email-address "test@example.com"}
```

### Example 3
```clojure
(snake->kebab {:name "Alice"})
;; => {:name "Alice"}  ; no underscore, unchanged
```

### Example 4
```clojure
(snake->kebab {})
;; => {}
```

## Tips

- Use `clojure.string/replace` to replace underscores with hyphens
- Convert keyword to string with `name`, then back to keyword with `keyword`
- Consider using `update-keys` (Clojure 1.11+) or manual approach
- Process: keyword → string → replace _ with - → keyword
- Example: `:first_name` → "first_name" → "first-name" → `:first-name`

## Testing your solution

```bash
cd challenges/019-snake-to-kebab/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-019.solution)
(challenge-019.solution/-test)
```
