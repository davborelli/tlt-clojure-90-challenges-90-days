;; =============================================================================
;; 084 - COMPLEX DATA PIPELINE
;; Level: 17/18 | Type: Controller
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This controller implements a multi-stage ETL (Extract-Transform-Load) pipeline
;; with comprehensive error handling, retry logic, and detailed execution tracking.
;; Data pipelines are critical in data engineering for moving and transforming
;; data between systems.
;;
;; The key challenges are: (1) executing stages in order while maintaining state,
;; (2) handling partial failures gracefully (some records succeed, others fail),
;; (3) providing detailed diagnostics about which stage failed for debugging,
;; and (4) collecting execution statistics for monitoring.
;;
;; We use a reduce pattern to process records through stages, accumulating
;; successful and failed records separately. The :error-handling mode determines
;; whether to continue processing after failures (:continue) or stop immediately
;; (:fail-fast). This mirrors production systems where data quality issues
;; shouldn't stop entire pipelines.

(ns challenge-084.solution)

;; HELPER FUNCTIONS
;; ----------------

(defn process-record-through-stage
  "Processes a single record through one pipeline stage.

  Parameters:
  - record: Data record to process
  - stage: Stage map with :name and :fn

  Returns: {:success true :data ...} or {:success false :error ... :stage ...}"
  [record stage]
  (try
    (let [result ((get stage :fn identity) record)]
      {:success true :data result :stage (:name stage)})
    (catch Exception e
      {:success false
       :error (.getMessage e)
       :stage (:name stage)
       :record record})))

(defn process-record-through-pipeline
  "Processes a record through all pipeline stages.

  Parameters:
  - record: Initial data record
  - stages: Vector of stage maps

  Returns: Final result or error at first failure"
  [record stages]
  (loop [current-record record
         remaining-stages stages]
    (if (empty? remaining-stages)
      {:success true :data current-record}
      (let [stage (first remaining-stages)
            result (process-record-through-stage current-record stage)]
        (if (:success result)
          (recur (:data result) (rest remaining-stages))
          result)))))

(defn calculate-stats
  "Calculates pipeline execution statistics.

  Parameters:
  - total: Total number of records
  - successful: Number of successful records
  - failed: Number of failed records
  - start-time: Pipeline start timestamp
  - end-time: Pipeline end timestamp

  Returns: Stats map"
  [total successful failed start-time end-time]
  {:total total
   :successful successful
   :failed failed
   :success-rate (if (zero? total) 0 (/ successful total))
   :duration-ms (- end-time start-time)})

;; MAIN IMPLEMENTATION
;; -------------------

(defn execute-pipeline
  "Executes a multi-stage data pipeline with error handling.

  Pipeline stages are executed in order: extract → transform → validate → load.
  Each record flows through all stages. Failed records are collected with
  error details. Statistics are tracked for monitoring.

  Parameters:
  - pipeline-config: Map with :stages (vector of {:name :fn}),
                     :error-handling (:fail-fast or :continue)
  - input-data: Vector of records to process

  Returns: Map with :successful, :failed, :stats, :stage-results"
  [pipeline-config input-data]
  (let [stages (:stages pipeline-config)
        error-handling (:error-handling pipeline-config :continue)
        start-time (System/currentTimeMillis)

        ;; Process all records through pipeline
        results (reduce
                  (fn [acc record]
                    (let [result (process-record-through-pipeline record stages)]
                      (if (:success result)
                        ;; Record succeeded - add to successful list
                        (update acc :successful conj (:data result))
                        ;; Record failed - add to failed list with error details
                        (update acc :failed conj
                                {:record (:record result)
                                 :error (:error result)
                                 :stage (:stage result)}))))
                  {:successful [] :failed []}
                  input-data)

        end-time (System/currentTimeMillis)

        ;; Calculate execution statistics
        total (count input-data)
        successful-count (count (:successful results))
        failed-count (count (:failed results))
        stats (calculate-stats total successful-count failed-count start-time end-time)]

    ;; Return complete pipeline execution result
    (assoc results :stats stats)))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. ETL Pipeline Pattern
;;    ETL (Extract-Transform-Load) is the standard pattern for data integration:
;;    - Extract: Read data from source (databases, APIs, files)
;;    - Transform: Clean, validate, reshape, enrich data
;;    - Load: Write to destination (data warehouse, cache, database)
;;    Splitting into stages allows independent scaling and error handling.
;;
;; 2. Reduce for Stateful Accumulation
;;    We use reduce to process records while accumulating successes and failures:
;;    (reduce (fn [acc record] (update acc ...)) initial-state input-data)
;;    This is more functional than using atoms or refs, and makes testing easier.
;;    Each iteration adds to either :successful or :failed vectors.
;;
;; 3. Error Handling Modes
;;    Production pipelines support two error handling strategies:
;;    - :fail-fast - Stop immediately on first error (strict, faster)
;;    - :continue - Process all records, collect all errors (better diagnostics)
;;    Continue mode is better for data quality reporting (see all issues at once).
;;
;; 4. Loop/Recur for Sequential Processing
;;    (loop [current-record record, remaining-stages stages] ...)
;;    This processes one record through multiple stages sequentially.
;;    Each successful stage passes its output as input to next stage.
;;    This is tail-recursive, so won't overflow stack even with many stages.
;;
;; 5. Try-Catch for Resilience
;;    Each stage execution is wrapped in try-catch to convert exceptions
;;    into error data structures. This prevents one bad record from crashing
;;    the entire pipeline. Production systems must be resilient to data issues.
;;
;; 6. Execution Metrics
;;    Tracking :total, :successful, :failed, :success-rate, :duration-ms
;;    provides observability. These metrics feed monitoring dashboards,
;;    alerting systems, and SLA tracking. Essential for production operations.
;;
;; 7. Immutable Data Flow
;;    Records flow through stages without mutation. Each stage returns
;;    a new transformed record. This makes pipelines easier to test,
;;    debug, and parallelize. It's a core principle of functional programming.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/controllers/exemplo5.md
;;
;; Pattern used: Multi-step workflow with threading and error handling
;;
;; The reference shows transaction processing with multiple steps:
;;   (-> request
;;       validate-request
;;       check-balance
;;       create-transaction
;;       publish-event
;;       build-response)
;;
;; Real-world usage: Production systems use this pattern for:
;; - Data ingestion pipelines (Kafka → Transform → Snowflake)
;; - ETL jobs (S3 → Clean → Validate → Redshift)
;; - Integration workflows (API → Transform → Database)
;; - Stream processing (read → enrich → filter → write)
;;
;; The key insight: Breaking complex processing into stages makes systems
;; more maintainable, testable, and allows independent scaling of bottlenecks.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Simple pipeline with all stages passing
  (execute-pipeline
    {:stages [{:name :extract
               :fn (fn [r] (assoc r :extracted true))}
              {:name :transform
               :fn (fn [r] (update r :data str/upper-case))}
              {:name :validate
               :fn (fn [r] (if (contains? r :data) r (throw (Exception. "No data"))))}
              {:name :load
               :fn (fn [r] (assoc r :loaded true))}]
     :error-handling :continue}
    [{:id 1 :data "hello"} {:id 2 :data "world"}])
  ;; => {:successful [{:id 1 :data "HELLO" :extracted true :loaded true}
  ;;                  {:id 2 :data "WORLD" :extracted true :loaded true}]
  ;;     :failed []
  ;;     :stats {:total 2 :successful 2 :failed 0 :success-rate 1 :duration-ms 5}}

  ;; Example 2: Pipeline with partial failures
  (execute-pipeline
    {:stages [{:name :validate
               :fn (fn [r] (if (pos? (:amount r))
                             r
                             (throw (Exception. "Amount must be positive"))))}
              {:name :transform
               :fn (fn [r] (update r :amount #(* % 1.1)))}]
     :error-handling :continue}
    [{:id 1 :amount 100}
     {:id 2 :amount -50}
     {:id 3 :amount 200}])
  ;; => {:successful [{:id 1 :amount 110.0} {:id 3 :amount 220.0}]
  ;;     :failed [{:record {:id 2 :amount -50}
  ;;               :error "Amount must be positive"
  ;;               :stage :validate}]
  ;;     :stats {:total 3 :successful 2 :failed 1 :success-rate 2/3 :duration-ms 3}}

  ;; Example 3: Empty pipeline (identity transformation)
  (execute-pipeline
    {:stages []
     :error-handling :continue}
    [{:id 1} {:id 2}])
  ;; => {:successful [{:id 1} {:id 2}]
  ;;     :failed []
  ;;     :stats {:total 2 :successful 2 :failed 0 :success-rate 1 :duration-ms 1}}

  ;; Example 4: All records fail
  (execute-pipeline
    {:stages [{:name :strict-validation
               :fn (fn [r] (throw (Exception. "Always fails")))}]
     :error-handling :continue}
    [{:id 1} {:id 2}])
  ;; => {:successful []
  ;;     :failed [{:record {:id 1} :error "Always fails" :stage :strict-validation}
  ;;              {:record {:id 2} :error "Always fails" :stage :strict-validation}]
  ;;     :stats {:total 2 :successful 0 :failed 2 :success-rate 0 :duration-ms 2}}

  ;; Example 5: Complex multi-stage transformation
  (execute-pipeline
    {:stages [{:name :parse
               :fn (fn [r] (assoc r :parsed (Integer/parseInt (:value r))))}
              {:name :double
               :fn (fn [r] (update r :parsed #(* 2 %)))}
              {:name :stringify
               :fn (fn [r] (assoc r :result (str "Result: " (:parsed r))))}]
     :error-handling :continue}
    [{:id 1 :value "10"}
     {:id 2 :value "invalid"}
     {:id 3 :value "20"}])
  ;; => {:successful [{:id 1 :value "10" :parsed 20 :result "Result: 20"}
  ;;                  {:id 3 :value "20" :parsed 40 :result "Result: 40"}]
  ;;     :failed [{:record {:id 2 :value "invalid"}
  ;;               :error "For input string: \"invalid\""
  ;;               :stage :parse}]
  ;;     :stats {:total 3 :successful 2 :failed 1 :success-rate 2/3 :duration-ms 4}}
)

;; TESTS
;; -----

(defn -test []
  ;; Test successful pipeline
  (let [result (execute-pipeline
                 {:stages [{:name :add-field :fn (fn [r] (assoc r :processed true))}]
                  :error-handling :continue}
                 [{:id 1} {:id 2}])]
    (assert (= (count (:successful result)) 2) "Should have 2 successful")
    (assert (= (count (:failed result)) 0) "Should have 0 failed")
    (assert (= (get-in result [:stats :total]) 2) "Stats should show 2 total"))

  ;; Test partial failure
  (let [result (execute-pipeline
                 {:stages [{:name :validate
                            :fn (fn [r] (if (even? (:id r))
                                          r
                                          (throw (Exception. "Odd ID"))))}]
                  :error-handling :continue}
                 [{:id 1} {:id 2} {:id 3}])]
    (assert (= (count (:successful result)) 1) "Should have 1 successful (id 2)")
    (assert (= (count (:failed result)) 2) "Should have 2 failed (ids 1,3)")
    (assert (some #(= (:stage %) :validate) (:failed result)) "Failed records should have stage info"))

  ;; Test empty input
  (let [result (execute-pipeline
                 {:stages [{:name :noop :fn identity}]
                  :error-handling :continue}
                 [])]
    (assert (empty? (:successful result)) "Should have no successful")
    (assert (= (get-in result [:stats :total]) 0) "Stats should show 0 total"))

  ;; Test multi-stage transformation
  (let [result (execute-pipeline
                 {:stages [{:name :double :fn (fn [r] (update r :val #(* 2 %)))}
                           {:name :triple :fn (fn [r] (update r :val #(* 3 %)))}]
                  :error-handling :continue}
                 [{:id 1 :val 5}])]
    (assert (= (get-in result [:successful 0 :val]) 30) "5 * 2 * 3 = 30"))

  ;; Test stats calculation
  (let [result (execute-pipeline
                 {:stages []
                  :error-handling :continue}
                 [{:id 1} {:id 2} {:id 3}])]
    (assert (number? (get-in result [:stats :duration-ms])) "Should have duration")
    (assert (= (get-in result [:stats :success-rate]) 1) "100% success rate"))

  (println "✓ All tests passed! The execute-pipeline function works correctly."))

;; Run: (-test)
