# 033 - Parse Date String

**Level**: 7/18
**Type**: Adapter
**Concepts**: Type coercion, String to map transformation, Date parsing

## Context

APIs and external systems often send dates as strings in various formats (ISO 8601, American format, European format). Before processing these dates in our application, we need to parse them into structured data. A common intermediate step is converting date strings into maps with year, month, and day components.

## Objective

Implement an adapter function that parses ISO date strings (YYYY-MM-DD format) into structured maps with integer components.

## Specification

### Input

- `date-string` (string): Date in format "YYYY-MM-DD" (e.g., "2024-01-15")

### Output

- (map): Map with keys `:year`, `:month`, `:day` containing integer values

### Rules

- Input format is always "YYYY-MM-DD"
- Split the string by hyphen (`-`)
- Convert each part from string to integer
- Return map with `:year`, `:month`, `:day` keys
- Function must be pure

## Examples

### Example 1
```clojure
(parse-date "2024-01-15")
;; => {:year 2024 :month 1 :day 15}
```

### Example 2
```clojure
(parse-date "1999-12-31")
;; => {:year 1999 :month 12 :day 31}
```

### Example 3
```clojure
(parse-date "2000-06-01")
;; => {:year 2000 :month 6 :day 1}
```

## Tips

- Use `clojure.string/split` with `#"-"` to split by hyphen
- Destructure the result: `[year month day]`
- Use `Integer/parseInt` to convert strings to integers
- Build the result map with literal syntax `{:year ... :month ... :day ...}`
- Consider using `let` for clarity

## Testing your solution

```bash
cd challenges/033-parse-date-string/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-033.solution)
(challenge-033.solution/-test)
```
