# 036 - Parse Full Name

**Level**: 8/18
**Type**: Pure Function
**Concepts**: String processing, Destructuring, Collection operations, Edge cases

## Context

User registration systems often collect full names as a single field, but internal systems need to separate first names, middle names, and last names for proper formatting, alphabetization, and personalization. This parsing needs to handle various name formats gracefully.

## Objective

Implement a pure function that parses a full name string into first name, last name, and optional middle name components.

## Specification

### Input

- `full-name` (string): Complete name (e.g., "John Michael Doe")

### Output

- (map): Map with `:first-name`, `:last-name`, and optionally `:middle-name`

### Rules

- Split name by spaces
- First word is `:first-name`
- Last word is `:last-name`
- Everything between first and last is `:middle-name` (joined by spaces)
- If only two words, no `:middle-name` key in result
- If only one word, it's both `:first-name` and `:last-name`
- Function must be pure

## Examples

### Example 1
```clojure
(parse-full-name "John Michael Doe")
;; => {:first-name "John" :middle-name "Michael" :last-name "Doe"}
```

### Example 2
```clojure
(parse-full-name "Jane Doe")
;; => {:first-name "Jane" :last-name "Doe"}
```

### Example 3
```clojure
(parse-full-name "Madonna")
;; => {:first-name "Madonna" :last-name "Madonna"}
```

### Example 4
```clojure
(parse-full-name "John Paul George Ringo Starr")
;; => {:first-name "John" :middle-name "Paul George Ringo" :last-name "Starr"}
```

## Tips

- Use `clojure.string/split` with `#"\s+"` to split by whitespace
- Use `first` for first name, `last` for last name
- Use `butlast` and `rest` to get middle parts
- Use `clojure.string/join` to combine middle names
- Consider using `cond` for different cases (1 word, 2 words, 3+ words)
- Use `assoc` conditionally to add `:middle-name` only when it exists

## Testing your solution

```bash
cd challenges/036-parse-full-name/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-036.solution)
(challenge-036.solution/-test)
```
