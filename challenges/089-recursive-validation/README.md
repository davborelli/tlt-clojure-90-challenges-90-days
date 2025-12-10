# 089 - Recursive Schema Validation

**Level**: 18/18
**Type**: Pure Function
**Concepts**: Recursive validation, Schema definition, Custom validators, Error accumulation, Nested structures

## Context

Complex data structures require sophisticated validation: nested objects, arrays of objects, recursive structures (trees, graphs), custom validation rules, and cross-field dependencies. Schema validation systems like JSON Schema, Clojure Spec, and Plumatic Schema provide declarative ways to validate complex data with clear error messages.

## Objective

Implement a recursive schema validation system that validates deeply nested structures, supports custom validators, accumulates all errors (not just first), handles recursive schemas, and provides detailed error paths.

## Specification

### Input

- `schema` (map): Validation schema with:
  - `:type` (keyword): :map, :vector, :string, :number, :boolean, :custom
  - `:required` (set): Required keys (for maps)
  - `:fields` (map): Schema for each field (for maps)
  - `:items` (schema): Schema for vector items
  - `:validators` (vector): Custom validator functions
  - `:recursive` (boolean): Indicates recursive schema
- `data`: Data to validate

### Output

- (map): Validation result with:
  - `:valid` (boolean): Whether data is valid
  - `:errors` (vector): All validation errors with paths
  - `:warnings` (vector): Non-critical issues
  - `:error-count` (number): Total error count

### Rules

- Validate type matches schema type
- Check required fields present
- Recursively validate nested structures
- Apply custom validators
- Accumulate ALL errors, don't stop at first
- Provide error paths (e.g., [:user :address :zip-code])
- Handle circular references in recursive schemas
- Support conditional validation (if field X present, validate Y)

## Examples

### Example 1: Simple validation success
```clojure
(validate-schema
  {:type :map
   :required #{:name :email}
   :fields {:name {:type :string}
           :email {:type :string :validators [email-validator]}}}
  {:name "John" :email "john@example.com"})
;; => {:valid true :errors [] :error-count 0}
```

### Example 2: Nested validation with errors
```clojure
(validate-schema
  {:type :map
   :fields {:user {:type :map
                  :required #{:name :age}
                  :fields {:name {:type :string}
                          :age {:type :number}}}}}
  {:user {:name "John"}})  ; Missing age
;; => {:valid false
;;     :errors [{:path [:user :age] :message "Required field missing"}]
;;     :error-count 1}
```

### Example 3: Recursive schema (tree)
```clojure
(validate-schema
  {:type :map
   :fields {:value {:type :number}
           :children {:type :vector
                     :items {:type :map :recursive true}}}
   :recursive true}
  {:value 10
   :children [{:value 5 :children []}
             {:value 15 :children [{:value 12}]}]})
;; => {:valid true :errors [] :error-count 0}
```

## Tips

- Use depth-first traversal for recursive validation
- Track visited nodes to detect cycles
- Build error paths as you recurse
- Use transducers for efficient collection processing
- Consider clojure.spec for production use
- Provide suggestions for fixing errors

## Testing your solution

```bash
cd challenges/089-recursive-validation/
clj -M solution.clj
```
Then in the REPL:
```clojure
(require 'challenge-089.solution)
(challenge-089.solution/-test)
```
