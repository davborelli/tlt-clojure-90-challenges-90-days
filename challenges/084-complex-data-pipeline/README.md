# 084 - Complex Data Pipeline

**Level**: 17/18
**Type**: Controller
**Concepts**: ETL pipeline, Error handling, Data validation, Transformation stages, Retry logic

## Context

Data pipelines are the backbone of data engineering, moving data from sources through transformation stages to destinations. Production pipelines must handle: multi-stage transformations, validation at each stage, comprehensive error handling with retry logic, partial failure recovery, and detailed logging for debugging. These are critical in analytics platforms, data warehouses, and integration systems.

## Objective

Implement a multi-stage data pipeline with validation, error handling, retry logic, and comprehensive result tracking.

## Specification

### Input

- `pipeline-config` (map): Pipeline configuration with:
  - `:stages` (vector): Ordered transformation stages
  - `:validation-rules` (map): Validation rules per stage
  - `:retry-policy` (map): Retry configuration
  - `:error-handling` (keyword): `:fail-fast` or `:continue`
- `input-data` (vector): Data records to process

### Output

- (map): Pipeline execution result with:
  - `:successful` (vector): Successfully processed records
  - `:failed` (vector): Failed records with error details
  - `:stats` (map): Execution statistics
  - `:stage-results` (vector): Results from each stage

### Rules

- Execute stages in order: extract → transform → validate → load
- Validate data at each stage
- Retry failed records according to policy
- Track which stage failed for each record
- Continue processing other records on failure (if :continue mode)
- Collect comprehensive execution statistics

## Examples

### Example 1: Successful pipeline
```clojure
(execute-pipeline
  {:stages [{:name :extract :fn extract-fn}
            {:name :transform :fn transform-fn}
            {:name :validate :fn validate-fn}
            {:name :load :fn load-fn}]
   :error-handling :continue}
  [{:id 1 :data "raw1"} {:id 2 :data "raw2"}])
;; => {:successful [{:id 1 :data "processed1"} {:id 2 :data "processed2"}]
;;     :failed []
;;     :stats {:total 2 :successful 2 :failed 0 :duration-ms 150}}
```

### Example 2: Partial failure with continue mode
```clojure
(execute-pipeline
  {:stages [...]
   :error-handling :continue}
  [{:id 1 :data "valid"} {:id 2 :data "invalid"} {:id 3 :data "valid"}])
;; => {:successful [{:id 1 ...} {:id 3 ...}]
;;     :failed [{:id 2 :error "Validation failed" :stage :validate}]
;;     :stats {:total 3 :successful 2 :failed 1}}
```

## Tips

- Use transducers for efficient data transformation
- Implement circuit breaker pattern for external calls
- Log stage transitions for debugging
- Consider backpressure for large datasets
- Use specs or schema for validation
- Track timing for each stage

## Testing your solution

```bash
cd challenges/084-complex-data-pipeline/
clj -M solution.clj
```
Then in the REPL:
```clojure
(require 'challenge-084.solution)
(challenge-084.solution/-test)
```
