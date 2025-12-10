# 057 - Extract Nested Metrics

**Level**: 12/18
**Type**: Pure Function
**Concepts**: Advanced destructuring, Nested extraction, Metric calculations

## Context

Analytics systems store metrics in deeply nested structures organized by category and time period. Extracting specific metrics and calculating aggregates (totals, averages, growth rates) requires navigating multiple nesting levels and performing calculations on the extracted values.

## Objective

Implement a function that uses advanced destructuring to extract nested metrics from an analytics report and calculate summary statistics.

## Specification

### Input

- `analytics-report` (map): Deeply nested analytics data
  ```clojure
  {:user-id "..."
   :metrics {:engagement {:daily {:views ... :clicks ... :shares ...}
                          :weekly {:views ... :clicks ... :shares ...}}
             :revenue {:daily {:amount ... :transactions ...}
                       :weekly {:amount ... :transactions ...}}}}
  ```

### Output

- (map): Extracted and calculated metrics
  ```clojure
  {:user-id "..."
   :daily-views ...
   :daily-clicks ...
   :daily-shares ...
   :weekly-views ...
   :weekly-clicks ...
   :weekly-shares ...
   :daily-revenue ...
   :daily-transactions ...
   :weekly-revenue ...
   :weekly-transactions ...
   :total-daily-engagement ...  ; views + clicks + shares
   :total-weekly-engagement ...
   :daily-click-rate ...  ; clicks / views (as percentage)
   :weekly-click-rate ...}
  ```

### Rules

- Extract all nested metrics (engagement and revenue, daily and weekly)
- Calculate total engagement: views + clicks + shares (for both daily and weekly)
- Calculate click rate: (clicks / views) * 100 (for both daily and weekly)
- Round click rates to 2 decimal places
- Use nested destructuring to extract values
- Function must be pure

## Examples

### Example 1
```clojure
(extract-metrics
  {:user-id "USER-123"
   :metrics {:engagement {:daily {:views 1000 :clicks 50 :shares 10}
                          :weekly {:views 7000 :clicks 350 :shares 70}}
             :revenue {:daily {:amount 250.00 :transactions 25}
                       :weekly {:amount 1750.00 :transactions 175}}}})
;; => {:user-id "USER-123"
;;     :daily-views 1000
;;     :daily-clicks 50
;;     :daily-shares 10
;;     :weekly-views 7000
;;     :weekly-clicks 350
;;     :weekly-shares 70
;;     :daily-revenue 250.0
;;     :daily-transactions 25
;;     :weekly-revenue 1750.0
;;     :weekly-transactions 175
;;     :total-daily-engagement 1060
;;     :total-weekly-engagement 7420
;;     :daily-click-rate 5.0
;;     :weekly-click-rate 5.0}
```

## Tips

- Use nested destructuring: `{:keys [user-id] {:keys [engagement revenue]} :metrics ...}`
- Or destructure in let: `(let [{:keys [user-id]} report {:keys [daily weekly]} (:engagement (:metrics report))])`
- Calculate totals: `(+ views clicks shares)`
- Calculate percentages: `(* (/ clicks views) 100.0)`
- Round to 2 decimals: `(/ (Math/round (* value 100)) 100.0)` or use format
- Pattern: Extract → Calculate → Assemble result

## Testing your solution

```bash
cd challenges/057-extract-nested-metrics/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-057.solution)
(challenge-057.solution/-test)
```
