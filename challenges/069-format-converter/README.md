# 069 - Format Converter

**Level**: 14/18
**Type**: Adapter
**Concepts**: Multi-format conversion, Pattern matching, Format detection

## Context

Systems often need to convert data between multiple formats: internal domain model, database records, API JSON, and CSV exports. A format converter provides a unified interface to transform data to/from various representations.

## Objective

Implement an adapter that converts user records between different formats (domain, database, API, CSV) based on format specification.

## Specification

### Input

- `user-record` (map): User data in some format
- `source-format` (keyword): Source format (`:domain`, `:database`, `:api`, or `:csv`)
- `target-format` (keyword): Target format (`:domain`, `:database`, `:api`, or `:csv`)

### Output

- (map or string): Converted record in target format

### Format Specifications

**Domain format (internal):**
```clojure
{:user-id "..." :full-name "..." :email "..." :active true/false}
```

**Database format:**
```clojure
{:id "..." :name "..." :email_address "..." :is_active 1/0}
```

**API format:**
```clojure
{:userId "..." :fullName "..." :email "..." :status "active"/"inactive"}
```

**CSV format (string):**
```clojure
"user_id,full_name,email,active"
```

### Rules

- Convert from source format to target format
- If source = target, return unchanged
- Transform field names and values according to format specs:
  - Domain: `:user-id`, `:full-name`, `:email`, `:active` (boolean)
  - Database: `:id`, `:name`, `:email_address`, `:is_active` (1/0)
  - API: `:userId`, `:fullName`, `:email`, `:status` ("active"/"inactive")
  - CSV: String with fields separated by commas
- Go through domain format as intermediate if needed

## Examples

### Example 1 (database → domain)
```clojure
(convert-format {:id "U123" :name "John Doe" :email_address "john@example.com" :is_active 1} :database :domain)
;; => {:user-id "U123" :full-name "John Doe" :email "john@example.com" :active true}
```

### Example 2 (domain → API)
```clojure
(convert-format {:user-id "U456" :full-name "Jane Smith" :email "jane@example.com" :active false} :domain :api)
;; => {:userId "U456" :fullName "Jane Smith" :email "jane@example.com" :status "inactive"}
```

### Example 3 (domain → CSV)
```clojure
(convert-format {:user-id "U789" :full-name "Bob Wilson" :email "bob@example.com" :active true} :domain :csv)
;; => "U789,Bob Wilson,bob@example.com,true"
```

## Tips

- Create helper functions for each format conversion
- Pattern: `database->domain`, `domain->api`, `domain->csv`, etc.
- For complex conversions (database → API), go through domain as intermediate
- Active field conversions: boolean ↔ 1/0 ↔ "active"/"inactive"
- CSV: use `str` to build comma-separated string
- Use `case` or `cond` to route to appropriate converter

## Testing your solution

```bash
cd challenges/069-format-converter/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-069.solution)
(challenge-069.solution/-test)
```
