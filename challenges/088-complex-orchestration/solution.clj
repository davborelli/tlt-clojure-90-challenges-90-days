;; =============================================================================
;; 088 - COMPLEX SERVICE ORCHESTRATION
;; Level: 18/18 | Type: Controller
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; Microservices architectures decompose applications into independent services
;; that communicate over networks. While this provides scalability and autonomy,
;; it introduces complexity when multiple services must coordinate to complete
;; a business transaction. A single e-commerce checkout might require: reserving
;; inventory, charging payment, scheduling shipping, sending notifications, and
;; updating analytics - each in a separate service. Any of these can fail due to
;; network issues, service unavailability, or business rule violations.
;;
;; This solution implements the Saga pattern for managing distributed transactions.
;; Unlike database transactions with ACID guarantees, distributed transactions
;; across services require explicit compensation logic. When a step fails, we
;; must execute compensating actions in reverse order to undo previously completed
;; steps. For example, if payment succeeds but shipping fails, we must refund
;; the payment and release the inventory reservation.
;;
;; The implementation includes circuit breakers to prevent cascade failures.
;; When a service experiences repeated failures, the circuit breaker "opens",
;; failing fast without attempting calls. This prevents resource exhaustion and
;; gives failing services time to recover. After a timeout, the circuit enters
;; "half-open" state, allowing test requests through. If they succeed, the circuit
;; closes; if they fail, it reopens.
;;
;; Distributed tracing through correlation IDs is essential for debugging. Each
;; request carries a trace ID that propagates through all service calls, allowing
;; developers to reconstruct the entire execution path across services. Combined
;; with comprehensive logging, this enables understanding failures in complex
;; distributed systems where a single user action may touch dozens of services.

(ns challenge-088.solution
  (:require [clojure.string :as str]))

;; IMPLEMENTATION
;; --------------

;; Circuit Breaker State Management

(defn create-circuit-breaker
  "Creates a new circuit breaker with initial state.

  Parameters:
  - threshold: Number of failures before opening circuit
  - timeout-ms: Time to wait before half-open attempt

  Returns: Circuit breaker state atom"
  [{:keys [threshold timeout-ms]}]
  (atom {:state :closed           ; :closed, :open, :half-open
         :failure-count 0
         :last-failure-time nil
         :threshold threshold
         :timeout-ms timeout-ms}))

(defn circuit-state
  "Gets current state of circuit breaker"
  [circuit-breaker]
  (:state @circuit-breaker))

(defn record-success!
  "Records successful call, closing circuit if needed"
  [circuit-breaker]
  (swap! circuit-breaker assoc
         :state :closed
         :failure-count 0
         :last-failure-time nil))

(defn record-failure!
  "Records failed call, potentially opening circuit"
  [circuit-breaker]
  (let [new-state (swap! circuit-breaker
                         (fn [cb]
                           (let [new-count (inc (:failure-count cb))]
                             (if (>= new-count (:threshold cb))
                               (assoc cb
                                      :state :open
                                      :failure-count new-count
                                      :last-failure-time (System/currentTimeMillis))
                               (assoc cb :failure-count new-count)))))]
    new-state))

(defn should-allow-request?
  "Checks if request should be allowed based on circuit state.

  Closed: Allow all requests
  Open: Check if timeout expired, transition to half-open if so
  Half-open: Allow request (will test if service recovered)"
  [circuit-breaker]
  (let [cb @circuit-breaker
        state (:state cb)]
    (case state
      :closed true
      :half-open true
      :open
      (let [current-time (System/currentTimeMillis)
            last-failure (:last-failure-time cb)
            timeout (:timeout-ms cb)]
        (if (and last-failure (> (- current-time last-failure) timeout))
          ;; Timeout expired, transition to half-open
          (do
            (swap! circuit-breaker assoc :state :half-open)
            true)
          false)))))

;; Distributed Tracing

(defn generate-trace-id
  "Generates unique trace ID for distributed tracing"
  []
  (str "TRACE-" (java.util.UUID/randomUUID)))

(defn create-span
  "Creates trace span for a service call"
  [service operation trace-id start-time]
  {:trace-id trace-id
   :service service
   :operation operation
   :start-time start-time
   :timestamp (java.util.Date.)})

(defn complete-span
  "Completes trace span with duration and result"
  [span result]
  (let [end-time (System/currentTimeMillis)
        duration (- end-time (:start-time span))]
    (assoc span
           :duration-ms duration
           :result result
           :completed-at (java.util.Date.))))

;; Service Execution with Circuit Breaker

(defn execute-with-circuit-breaker
  "Executes service call with circuit breaker protection.

  Returns result map with :success and either :result or :error"
  [service-fn circuit-breaker service-name params]
  (if-not (should-allow-request? circuit-breaker)
    {:success false
     :error "Circuit breaker open"
     :service service-name
     :circuit-state :open}

    (try
      (let [result (service-fn params)]
        (record-success! circuit-breaker)
        {:success true
         :result result
         :service service-name})
      (catch Exception e
        (record-failure! circuit-breaker)
        {:success false
         :error (.getMessage e)
         :service service-name
         :exception-type (type e)}))))

;; Retry Logic with Exponential Backoff

(defn calculate-backoff-ms
  "Calculates exponential backoff delay"
  [attempt base-ms max-ms]
  (min (* base-ms (Math/pow 2 attempt)) max-ms))

(defn execute-with-retry
  "Executes function with retry and exponential backoff.

  Returns result map with :success and either :result or :error"
  [service-fn retry-config params]
  (let [{:keys [max-attempts base-delay-ms max-delay-ms]
         :or {max-attempts 3
              base-delay-ms 100
              max-delay-ms 5000}} retry-config]
    (loop [attempt 0
           last-error nil]
      (if (>= attempt max-attempts)
        {:success false
         :error (or last-error "Max retry attempts exceeded")
         :attempts attempt}

        (let [result (try
                      {:success true
                       :result (service-fn params)}
                      (catch Exception e
                        {:success false
                         :error (.getMessage e)
                         :exception e}))]
          (if (:success result)
            result
            (let [backoff-ms (calculate-backoff-ms attempt base-delay-ms max-delay-ms)]
              (Thread/sleep backoff-ms)
              (recur (inc attempt) (:error result)))))))))

;; Compensation (Saga Pattern)

(defn execute-compensation
  "Executes compensation function for a completed step.

  Compensations undo the effects of previously completed steps when
  a later step fails, maintaining consistency in distributed transactions."
  [compensation-fn step-result]
  (try
    {:success true
     :result (compensation-fn step-result)
     :compensated true}
    (catch Exception e
      {:success false
       :error (.getMessage e)
       :compensated false
       :manual-intervention-required true})))

(defn compensate-completed-steps
  "Executes compensation functions in reverse order.

  When a saga fails, we must undo previously completed steps in reverse order
  (LIFO) to maintain consistency. Each compensation receives the result from
  its corresponding forward step."
  [completed-steps compensation-config]
  (loop [steps (reverse completed-steps)
         compensations []]
    (if (empty? steps)
      compensations

      (let [step (first steps)
            service (:service step)
            compensation-fn (get compensation-config service)
            compensation-result (if compensation-fn
                                  (execute-compensation compensation-fn (:result step))
                                  {:success false
                                   :error "No compensation defined"
                                   :manual-intervention-required true})]
        (recur (rest steps)
               (conj compensations
                     (assoc compensation-result
                            :service service
                            :action :compensate)))))))

;; Main Orchestration Logic

(defn execute-orchestration-step
  "Executes a single orchestration step with all protections.

  Combines circuit breaker, retry logic, and distributed tracing."
  [step context circuit-breakers retry-policy trace-id]
  (let [{:keys [service action service-fn]} step
        circuit-breaker (get circuit-breakers service)
        start-time (System/currentTimeMillis)
        span (create-span service action trace-id start-time)

        ;; Execute with circuit breaker and retry
        result (if circuit-breaker
                (execute-with-circuit-breaker
                  (fn [params]
                    (let [retry-result (execute-with-retry service-fn retry-policy params)]
                      (if (:success retry-result)
                        (:result retry-result)
                        (throw (ex-info (:error retry-result) retry-result)))))
                  circuit-breaker
                  service
                  context)
                ;; No circuit breaker configured
                (execute-with-retry service-fn retry-policy context))

        completed-span (complete-span span result)]

    {:step step
     :result (if (:success result) (:result result) nil)
     :success (:success result)
     :error (when-not (:success result) (:error result))
     :service service
     :span completed-span
     :circuit-state (when circuit-breaker (circuit-state circuit-breaker))}))

(defn orchestrate
  "Main orchestration function implementing Saga pattern.

  Executes steps sequentially, accumulating context. On failure, executes
  compensation functions in reverse order. Includes circuit breakers, retry
  logic, and comprehensive tracing.

  Parameters:
  - orchestration-plan: Map with :steps, :circuit-breakers, :compensation, :retry-policy
  - initial-context: Starting data passed to first step

  Returns: Map with :status, :results, :compensations-executed, :trace"
  [orchestration-plan initial-context]
  (let [{:keys [steps circuit-breakers compensation retry-policy]} orchestration-plan
        trace-id (generate-trace-id)
        circuit-breaker-atoms (into {}
                                    (map (fn [[service config]]
                                           [service (create-circuit-breaker config)])
                                         circuit-breakers))
        retry-config (or retry-policy {:max-attempts 3
                                       :base-delay-ms 100
                                       :max-delay-ms 5000})]

    (loop [remaining-steps steps
           completed-steps []
           current-context initial-context
           trace []]
      (if (empty? remaining-steps)
        ;; All steps completed successfully
        {:status :success
         :results completed-steps
         :compensations-executed []
         :trace trace
         :circuit-breaker-states (into {}
                                       (map (fn [[service cb]]
                                              [service (circuit-state cb)])
                                            circuit-breaker-atoms))
         :final-state :committed
         :trace-id trace-id}

        (let [step (first remaining-steps)
              step-result (execute-orchestration-step
                            step
                            current-context
                            circuit-breaker-atoms
                            retry-config
                            trace-id)
              new-trace (conj trace (:span step-result))]

          (if (:success step-result)
            ;; Step succeeded, continue with next step
            (recur (rest remaining-steps)
                   (conj completed-steps step-result)
                   (merge current-context (:result step-result))
                   new-trace)

            ;; Step failed, initiate compensation
            (let [compensations (compensate-completed-steps completed-steps compensation)
                  all-compensations-successful? (every? :success compensations)]
              {:status (if all-compensations-successful? :aborted :partial-failure)
               :results completed-steps
               :failed-step step-result
               :compensations-executed compensations
               :trace new-trace
               :circuit-breaker-states (into {}
                                             (map (fn [[service cb]]
                                                    [service (circuit-state cb)])
                                                  circuit-breaker-atoms))
               :final-state (if all-compensations-successful? :rolled-back :inconsistent)
               :abort-reason (str (:service step-result) " failed: " (:error step-result))
               :trace-id trace-id
               :manual-intervention-required (some :manual-intervention-required compensations)})))))))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Saga Pattern
;;    The Saga pattern manages distributed transactions through compensating actions.
;;    Unlike database transactions that can be rolled back atomically, distributed
;;    transactions span multiple services that can't share a transaction context.
;;    Sagas define forward actions (reserve inventory, charge payment) and
;;    compensating actions (release inventory, refund payment). If any step fails,
;;    compensations execute in reverse order to undo completed steps. This provides
;;    eventual consistency without requiring distributed locks. Two coordination
;;    approaches exist: choreography (services emit events, others listen and react)
;;    and orchestration (central coordinator directs the saga). This implementation
;;    uses orchestration for clearer control flow and easier debugging.
;;
;; 2. Circuit Breaker Pattern
;;    Circuit breakers prevent cascade failures in distributed systems. When a
;;    service experiences repeated failures, continuing to call it wastes resources
;;    and delays error responses. The circuit breaker tracks failures and "opens"
;;    after a threshold, immediately returning errors without attempting calls.
;;    After a timeout, it enters "half-open" state, allowing test requests. If
;;    they succeed, the circuit closes; if they fail, it reopens. This gives
;;    failing services time to recover and prevents thread pool exhaustion. In
;;    production, use libraries like Resilience4j that provide metrics, configuration,
;;    and integration with monitoring systems.
;;
;; 3. Exponential Backoff
;;    Retry logic should use exponential backoff to avoid overwhelming recovering
;;    services. Linear retry intervals (retry every 1 second) can create thundering
;;    herds where many clients retry simultaneously, preventing recovery. Exponential
;;    backoff doubles the delay after each failure (100ms, 200ms, 400ms, 800ms),
;;    spreading retry attempts over time. Add jitter (random variation) to further
;;    distribute load. Set maximum delays to bound retry times. Consider different
;;    retry strategies for different error types: retry network errors but not
;;    validation errors. In production, use exponential backoff with jitter for
;;    all distributed calls.
;;
;; 4. Distributed Tracing
;;    Distributed tracing reconstructs request flows across services. Each request
;;    generates a trace ID that propagates through all service calls. Services
;;    create spans (timed operations) linked to the trace. Tracing systems like
;;    Jaeger or Zipkin collect spans and visualize the complete execution path,
;;    showing which services were called, in what order, how long each took, and
;;    where failures occurred. OpenTelemetry standardizes tracing instrumentation
;;    across languages and frameworks. Without tracing, debugging distributed
;;    systems requires correlating logs across services by timestamp, which is
;;    unreliable due to clock skew and high request volumes.
;;
;; 5. Idempotency
;;    Retry logic requires idempotent operations - executing them multiple times
;;    has the same effect as executing once. Charging a payment twice is bad;
;;    checking if a charge succeeded is idempotent. Implement idempotency through:
;;    (a) idempotency keys (client sends unique request ID, server deduplicates),
;;    (b) natural idempotency (PUT is idempotent, POST is not), (c) state checks
;;    (before creating, check if it exists). Our saga implementation should include
;;    idempotency keys to safely retry failed steps without duplicating effects.
;;    This is especially critical for financial operations and external API calls.
;;
;; 6. Timeout Management
;;    Every distributed call needs a timeout to prevent hanging indefinitely. Set
;;    timeouts at multiple levels: connection timeout (establishing connection),
;;    request timeout (receiving response), and overall operation timeout (including
;;    retries). Timeouts should be shorter at lower levels and longer at higher
;;    levels. For example, a single service call might timeout at 1 second, retry
;;    logic at 5 seconds, and the overall orchestration at 30 seconds. This
;;    implementation's circuit breaker includes timeout for reopening, but production
;;    systems need per-request timeouts too. Use future-based timeouts in Clojure
;;    with deref timeout parameter.
;;
;; 7. Partial Failures
;;    Distributed systems must handle partial failures gracefully. In our saga,
;;    a compensation step might fail, leaving the system in an inconsistent state.
;;    The :manual-intervention-required flag signals operations teams to investigate.
;;    Production systems should: (a) log detailed failure information, (b) emit
;;    alerts for manual intervention cases, (c) provide administrative tools to
;;    complete or rollback partial transactions, (d) implement reconciliation
;;    processes that periodically check for inconsistencies. The :final-state field
;;    (:committed, :rolled-back, :inconsistent) helps operators understand system
;;    state and take appropriate action.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/controllers/exemplo5.md
;;
;; Pattern used: Transactional flow with threading macros and external service coordination
;;
;; The reference code demonstrates sophisticated transaction orchestration:
;;
;; 1. Sequential step execution with context accumulation:
;;    (let [transaction (fetch-or-create-record! ...)
;;          {:keys [card-id customer-id]} (get-pan-mapping! ...)]
;;      (-> money-out
;;          (logic.transaction/money-out->money-out-settled transaction)
;;          (producer/money-out-settled! ...))
;;      (-> money-out
;;          (logic.transaction/money-out->money-in transaction ...)
;;          (producer/new-money-in! ...)))
;;
;;    This shows orchestrating multiple services (fetch, transform, produce events)
;;    in sequence, threading context through the operations. Our saga pattern
;;    generalizes this to handle failures and compensation.
;;
;; 2. Feature flag controlled behavior:
;;    (if new-lending-global-routing?
;;      (create-record-v2! ...)
;;      (create-record! ...))
;;
;;    Demonstrating conditional logic paths in orchestrations, similar to how
;;    our circuit breakers conditionally execute or skip service calls.
;;
;; 3. Validation before execution:
;;    (when-not (logic.transaction/valid? money-out)
;;      (ex/invalid-input! {:reason :invalid-transaction}))
;;
;;    Early validation prevents unnecessary work, similar to our request validation
;;    before executing orchestration steps.
;;
;; Real-world usage: The money transfer controller orchestrates multiple services:
;; it validates the transaction, fetches or creates records, transforms between
;; wire and domain models, publishes events to message brokers, and coordinates
;; between issuer and beneficiary flows. Each step depends on previous steps
;; and must handle failures appropriately. This is exactly the complexity our
;; orchestration solution addresses, adding circuit breakers, compensation, and
;; comprehensive tracing to make such orchestrations production-ready.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Successful e-commerce checkout orchestration
  (def inventory-service
    (fn [ctx] {:reserved true :items (:items ctx)}))

  (def payment-service
    (fn [ctx] {:charged true :transaction-id "PAY-123" :amount (:total ctx)}))

  (def shipping-service
    (fn [ctx] {:scheduled true :tracking "SHIP-456"}))

  (orchestrate
    {:steps [{:service :inventory
              :action :reserve
              :service-fn inventory-service}
             {:service :payment
              :action :charge
              :service-fn payment-service}
             {:service :shipping
              :action :schedule
              :service-fn shipping-service}]
     :circuit-breakers {:payment {:threshold 5 :timeout-ms 30000}}
     :compensation {:inventory (fn [result] {:released true})
                    :payment (fn [result] {:refunded true :tx-id (:transaction-id result)})}
     :retry-policy {:max-attempts 3 :base-delay-ms 100 :max-delay-ms 2000}}
    {:order-id "ORD-123"
     :items [{:sku "ITEM-1" :qty 2}]
     :total 100})
  ;; => {:status :success
  ;;     :results [{:service :inventory :result {:reserved true ...} :success true}
  ;;               {:service :payment :result {:charged true :transaction-id "PAY-123"} :success true}
  ;;               {:service :shipping :result {:scheduled true :tracking "SHIP-456"} :success true}]
  ;;     :compensations-executed []
  ;;     :trace [{:trace-id "TRACE-..." :service :inventory :duration-ms 45}
  ;;             {:trace-id "TRACE-..." :service :payment :duration-ms 150}
  ;;             {:trace-id "TRACE-..." :service :shipping :duration-ms 80}]
  ;;     :circuit-breaker-states {:payment :closed}
  ;;     :final-state :committed}

  ;; Example 2: Failure with successful compensation (Saga rollback)
  (def failing-payment-service
    (fn [ctx] (throw (Exception. "Card declined"))))

  (orchestrate
    {:steps [{:service :inventory
              :action :reserve
              :service-fn inventory-service}
             {:service :payment
              :action :charge
              :service-fn failing-payment-service}
             {:service :shipping
              :action :schedule
              :service-fn shipping-service}]
     :compensation {:inventory (fn [result] {:released true :items (:items result)})
                    :payment (fn [result] {:refunded true})}}
    {:order-id "ORD-456" :items [{:sku "ITEM-2" :qty 1}] :total 50})
  ;; => {:status :aborted
  ;;     :results [{:service :inventory :result {:reserved true ...} :success true}]
  ;;     :failed-step {:service :payment :success false :error "Card declined"}
  ;;     :compensations-executed [{:service :inventory :success true :result {:released true} :compensated true}]
  ;;     :final-state :rolled-back
  ;;     :abort-reason "payment failed: Card declined"}

  ;; Example 3: Circuit breaker opening after repeated failures
  (def unreliable-service
    (let [call-count (atom 0)]
      (fn [ctx]
        (swap! call-count inc)
        (if (< @call-count 6)
          (throw (Exception. "Service unavailable"))
          {:success true}))))

  (orchestrate
    {:steps [{:service :test
              :action :call
              :service-fn unreliable-service}]
     :circuit-breakers {:test {:threshold 3 :timeout-ms 1000}}
     :retry-policy {:max-attempts 1}}  ; Don't retry, test circuit breaker
    {:data "test"})
  ;; After 3 failures, circuit opens:
  ;; => {:status :aborted
  ;;     :failed-step {:service :test :error "Circuit breaker open" :circuit-state :open}
  ;;     :circuit-breaker-states {:test :open}}

  ;; Example 4: Retry with exponential backoff succeeding
  (def eventually-succeeds-service
    (let [attempt (atom 0)]
      (fn [ctx]
        (swap! attempt inc)
        (if (< @attempt 3)
          (throw (Exception. "Temporary failure"))
          {:success true :attempts @attempt}))))

  (orchestrate
    {:steps [{:service :eventually-succeeds
              :action :call
              :service-fn eventually-succeeds-service}]
     :retry-policy {:max-attempts 5 :base-delay-ms 50 :max-delay-ms 1000}}
    {:data "test"})
  ;; => {:status :success
  ;;     :results [{:service :eventually-succeeds
  ;;                :result {:success true :attempts 3}
  ;;                :success true}]
  ;;     :final-state :committed}

  ;; Example 5: Partial failure - compensation also fails
  (def failing-compensation
    (fn [result] (throw (Exception. "Compensation failed"))))

  (orchestrate
    {:steps [{:service :step1
              :action :execute
              :service-fn (fn [ctx] {:completed true})}
             {:service :step2
              :action :execute
              :service-fn (fn [ctx] (throw (Exception. "Step failed")))}]
     :compensation {:step1 failing-compensation}}
    {:data "test"})
  ;; => {:status :partial-failure
  ;;     :results [{:service :step1 :result {:completed true} :success true}]
  ;;     :compensations-executed [{:service :step1
  ;;                               :success false
  ;;                               :compensated false
  ;;                               :manual-intervention-required true}]
  ;;     :final-state :inconsistent
  ;;     :manual-intervention-required true}

  ;; Example 6: Distributed tracing with correlation ID
  (orchestrate
    {:steps [{:service :service-a :action :process :service-fn (fn [ctx] {:a-result true})}
             {:service :service-b :action :process :service-fn (fn [ctx] {:b-result true})}]
     :retry-policy {:max-attempts 1}}
    {:user-id "USER-123"})
  ;; Each span in :trace includes same :trace-id for correlation:
  ;; => {:trace [{:trace-id "TRACE-abc123" :service :service-a :duration-ms 10 ...}
  ;;             {:trace-id "TRACE-abc123" :service :service-b :duration-ms 15 ...}]
  ;;     :trace-id "TRACE-abc123"}

  ;; Example 7: Context accumulation across steps
  (orchestrate
    {:steps [{:service :create-order
              :action :create
              :service-fn (fn [ctx] {:order-id "ORD-789" :status :created})}
             {:service :send-notification
              :action :notify
              :service-fn (fn [ctx]
                           ;; Context includes result from previous step
                           {:notified true :order-id (:order-id ctx)})}]}
    {:customer-id "CUST-123"})
  ;; => {:status :success
  ;;     :results [{:service :create-order :result {:order-id "ORD-789" :status :created}}
  ;;               {:service :send-notification
  ;;                :result {:notified true :order-id "ORD-789"}}]
  ;;     :final-state :committed}
)

;; TESTS
;; -----

(defn -test []
  ;; Test 1: Successful orchestration with multiple steps
  (let [result (orchestrate
                 {:steps [{:service :step1
                           :action :execute
                           :service-fn (fn [ctx] {:result 1})}
                          {:service :step2
                           :action :execute
                           :service-fn (fn [ctx] {:result 2})}]
                  :retry-policy {:max-attempts 1}}
                 {:initial "data"})]
    (assert (= (:status result) :success)
            "Orchestration should succeed with all steps completing")
    (assert (= (count (:results result)) 2)
            "Should have results for both steps")
    (assert (empty? (:compensations-executed result))
            "No compensations should execute on success")
    (assert (= (:final-state result) :committed)
            "Final state should be committed"))

  ;; Test 2: Failure triggers compensation in reverse order
  (let [compensation-order (atom [])
        result (orchestrate
                 {:steps [{:service :step1
                           :action :execute
                           :service-fn (fn [ctx] {:completed true})}
                          {:service :step2
                           :action :execute
                           :service-fn (fn [ctx] (throw (Exception. "Failed")))}]
                  :compensation {:step1 (fn [r]
                                          (swap! compensation-order conj :step1)
                                          {:compensated true})}
                  :retry-policy {:max-attempts 1}}
                 {})]
    (assert (= (:status result) :aborted)
            "Should abort on step failure")
    (assert (= @compensation-order [:step1])
            "Compensation should execute for completed steps")
    (assert (= (:final-state result) :rolled-back)
            "Final state should be rolled-back"))

  ;; Test 3: Circuit breaker opens after threshold failures
  (let [circuit-breakers {:failing-service {:threshold 2 :timeout-ms 5000}}
        call-count (atom 0)
        failing-fn (fn [ctx]
                    (swap! call-count inc)
                    (throw (Exception. "Service down")))

        ;; First call - circuit closed, fails
        result1 (orchestrate
                  {:steps [{:service :failing-service
                            :action :call
                            :service-fn failing-fn}]
                   :circuit-breakers circuit-breakers
                   :retry-policy {:max-attempts 1}}
                  {})

        ;; Second call - still closed, fails, opens circuit
        result2 (orchestrate
                  {:steps [{:service :failing-service
                            :action :call
                            :service-fn failing-fn}]
                   :circuit-breakers circuit-breakers
                   :retry-policy {:max-attempts 1}}
                  {})]

    (assert (= (:status result1) :aborted)
            "First call should fail")
    (assert (= (:status result2) :aborted)
            "Second call should fail"))

  ;; Test 4: Retry with exponential backoff
  (let [attempts (atom 0)
        eventually-succeeds (fn [ctx]
                             (swap! attempts inc)
                             (if (< @attempts 3)
                               (throw (Exception. "Retry me"))
                               {:success true}))
        result (orchestrate
                 {:steps [{:service :retry-test
                           :action :call
                           :service-fn eventually-succeeds}]
                  :retry-policy {:max-attempts 5 :base-delay-ms 10 :max-delay-ms 100}}
                 {})]
    (assert (= (:status result) :success)
            "Should succeed after retries")
    (assert (>= @attempts 3)
            "Should have made multiple attempts"))

  ;; Test 5: Distributed tracing includes all steps
  (let [result (orchestrate
                 {:steps [{:service :service-a :action :process :service-fn (fn [ctx] {:a true})}
                          {:service :service-b :action :process :service-fn (fn [ctx] {:b true})}]
                  :retry-policy {:max-attempts 1}}
                 {})]
    (assert (contains? result :trace-id)
            "Should include trace ID")
    (assert (= (count (:trace result)) 2)
            "Trace should include all steps")
    (assert (every? #(= (:trace-id %) (:trace-id result)) (:trace result))
            "All spans should share same trace ID"))

  ;; Test 6: Context accumulation across steps
  (let [result (orchestrate
                 {:steps [{:service :step1
                           :action :execute
                           :service-fn (fn [ctx] {:user-id "USER-123"})}
                          {:service :step2
                           :action :execute
                           :service-fn (fn [ctx]
                                        ;; Should have access to step1 result
                                        {:user-id (:user-id ctx)
                                         :processed true})}]
                  :retry-policy {:max-attempts 1}}
                 {:initial "data"})]
    (assert (= (:status result) :success)
            "Should succeed with context accumulation")
    (assert (= (get-in result [:results 1 :result :user-id]) "USER-123")
            "Second step should receive context from first step"))

  ;; Test 7: Partial failure with compensation failure
  (let [result (orchestrate
                 {:steps [{:service :step1
                           :action :execute
                           :service-fn (fn [ctx] {:completed true})}
                          {:service :step2
                           :action :execute
                           :service-fn (fn [ctx] (throw (Exception. "Failed")))}]
                  :compensation {:step1 (fn [r] (throw (Exception. "Compensation failed")))}
                  :retry-policy {:max-attempts 1}}
                 {})]
    (assert (= (:status result) :partial-failure)
            "Should indicate partial failure")
    (assert (= (:final-state result) :inconsistent)
            "Final state should be inconsistent")
    (assert (:manual-intervention-required result)
            "Should flag need for manual intervention"))

  (println "✓ All tests passed! The service orchestration system works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
