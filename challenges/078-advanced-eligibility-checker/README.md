# 078 - Advanced Eligibility Checker

**Level**: 16/18
**Type**: Adapter
**Concepts**: Multi-format normalization, Conditional fields, Complex parsing, Data validation

## Context

Modern applications often receive user data from multiple sources (web forms, mobile apps, API integrations, legacy systems) in different formats. Before processing eligibility checks, this heterogeneous data must be normalized into a consistent internal format. Some fields are optional depending on the data source, and different naming conventions must be reconciled.

## Objective

Implement an adapter that normalizes multi-source user eligibility data into a standardized internal format, handling conditional fields, type coercion, and format variations.

## Specification

### Input

- `source-data` (map): Raw data from external source with keys:
  - `:source` (keyword): One of `:web`, `:mobile`, `:api`, `:legacy`
  - `:data` (map): The actual user data (format varies by source)

### Output

- (map): Normalized eligibility record with standardized keys:
  - `:user-id` (string): Unique identifier
  - `:full-name` (string): Complete name
  - `:birth-date` (string): ISO format "YYYY-MM-DD"
  - `:annual-income` (number): Yearly income in dollars
  - `:credit-score` (number): Credit score (300-850)
  - `:employment-status` (keyword): One of `:employed`, `:self-employed`, `:unemployed`, `:retired`
  - `:requested-amount` (number): Loan amount requested
  - `:has-collateral` (boolean): Whether collateral is offered
  - `:source-system` (keyword): Original source

### Rules

- Web format uses camelCase keys, dates as "MM/DD/YYYY"
- Mobile format uses snake_case keys, dates as Unix timestamps
- API format uses kebab-case keys, dates as ISO strings
- Legacy format uses UPPERCASE keys, dates as "DDMMYYYY"
- Missing optional fields should have sensible defaults
- All monetary values must be converted to numbers
- Employment status must be normalized to standard keywords
- Invalid data should include `:validation-errors` vector

## Examples

### Example 1: Web Source
```clojure
(normalize-eligibility-data
  {:source :web
   :data {:userId "W123"
          :firstName "John"
          :lastName "Smith"
          :birthDate "03/15/1985"
          :annualIncome "75000"
          :creditScore "720"
          :employmentStatus "employed"
          :requestedAmount "25000"}})
;; => {:user-id "W123"
;;     :full-name "John Smith"
;;     :birth-date "1985-03-15"
;;     :annual-income 75000
;;     :credit-score 720
;;     :employment-status :employed
;;     :requested-amount 25000
;;     :has-collateral false
;;     :source-system :web}
```

### Example 2: Mobile Source
```clojure
(normalize-eligibility-data
  {:source :mobile
   :data {:user_id "M456"
          :full_name "Jane Doe"
          :birth_date 512611200
          :annual_income 95000
          :credit_score 780
          :employment_status "self_employed"
          :requested_amount 40000
          :has_collateral true}})
;; => {:user-id "M456"
;;     :full-name "Jane Doe"
;;     :birth-date "1986-03-28"
;;     :annual-income 95000
;;     :credit-score 780
;;     :employment-status :self-employed
;;     :requested-amount 40000
;;     :has-collateral true
;;     :source-system :mobile}
```

### Example 3: Invalid Data
```clojure
(normalize-eligibility-data
  {:source :web
   :data {:userId "W789"
          :creditScore "invalid"}})
;; => {:user-id "W789"
;;     :validation-errors ["Missing required field: firstName"
;;                        "Missing required field: lastName"
;;                        "Invalid credit score format"
;;                        "Missing required field: annualIncome"]}
```

## Tips

- Use multi-methods or case statements to dispatch on `:source`
- Create helper functions for date format conversions
- Use `clojure.string` for name concatenation
- Provide sensible defaults for optional fields
- Validate data after normalization
- Consider using schema validation libraries in production

## Testing your solution

```bash
cd challenges/078-advanced-eligibility-checker/
clj -M solution.clj
```
Then in the REPL:
```clojure
(require 'challenge-078.solution)
(challenge-078.solution/-test)
```
