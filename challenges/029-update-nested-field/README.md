# 029 - Update Nested Field

**Level**: 6/18
**Type**: Adapter
**Concepts**: update-in, Nested map transformation, Deep updates

## Context

When working with nested data structures, we often need to update values deep within the structure. The `update-in` function allows us to apply transformations at specific paths without manually reconstructing the entire structure.

## Objective

Implement an adapter function that increments the age field in a nested user profile structure.

## Specification

### Input

- `profile` (map): Nested map with structure:
  ```clojure
  {:user {:name "..." :details {:age ...}}}
  ```

### Output

- (map): Same structure with age incremented by 1

### Rules

- Age is at path `[:user :details :age]`
- Increment age by 1
- Preserve all other fields and structure
- Function must be pure

## Examples

### Example 1
```clojure
(increment-age {:user {:name "John" :details {:age 25}}})
;; => {:user {:name "John" :details {:age 26}}}
```

### Example 2
```clojure
(increment-age {:user {:name "Jane" :details {:age 30}}})
;; => {:user {:name "Jane" :details {:age 31}}}
```

## Tips

- Use `update-in` with path `[:user :details :age]`
- Apply `inc` function to increment
- Pattern: `(update-in data path fn)`
- update-in preserves all other nested structure

## Testing your solution

```bash
cd challenges/029-update-nested-field/
clj -M solution.clj
```
