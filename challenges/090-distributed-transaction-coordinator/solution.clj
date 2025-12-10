;; =============================================================================
;; 090 - DISTRIBUTED TRANSACTION COORDINATOR (2PC)
;; Level: 18/18 | Type: Controller
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; Distributed transactions are one of the hardest problems in computer science.
;; When a single business operation spans multiple databases or services, ensuring
;; atomicity (all succeed or all fail) requires coordination across systems that
;; may be on different networks, in different data centers, running different
;; software. The Two-Phase Commit (2PC) protocol is the classic solution, providing
;; ACID-like guarantees across distributed systems.
;;
;; The protocol works in two phases: (1) Prepare - the coordinator asks all
;; participants "can you commit this transaction?" Each participant prepares
;; (locks resources, validates constraints) and votes YES or NO. (2) Commit/Abort -
;; if ALL participants voted YES, the coordinator tells everyone to commit; if
;; ANY voted NO or timed out, it tells everyone to abort. This ensures atomicity:
;; either all participants commit or all abort, never a mix.
;;
;; The implementation must handle numerous failure scenarios: participants may
;; crash, networks may partition, messages may be lost or delayed. Durable logging
;; is critical - the coordinator logs its decision before sending commit/abort
;; messages. If the coordinator crashes and restarts, it can read the log and
;; complete the transaction. Participants must also log their votes and wait for
;; the coordinator's decision, maintaining locks until they receive it.
;;
;; Timeouts prevent indefinite blocking. If a participant doesn't respond to
;; prepare within the timeout, the coordinator aborts the transaction. If the
;; coordinator crashes during commit phase, participants must timeout and contact
;; the new coordinator or abort. This can lead to blocking - participants holding
;; locks waiting for a crashed coordinator - which is why 2PC is not always
;; appropriate. Modern systems often prefer saga patterns or eventual consistency,
;; reserving 2PC for operations where strong consistency is absolutely required.

(ns challenge-090.solution
  (:require [clojure.string :as str]))

;; IMPLEMENTATION
;; --------------

;; Transaction Log Management

(defn create-log-entry
  "Creates a durable log entry for transaction decision.

  Logging decisions before sending messages enables recovery if coordinator crashes."
  [transaction-id phase decision & [metadata]]
  {:transaction-id transaction-id
   :timestamp (java.util.Date.)
   :phase phase
   :decision decision
   :metadata (or metadata {})})

(defn append-to-log
  "Appends entry to transaction log.

  In production, this would write to durable storage (disk, database, replicated log).
  For this implementation, we accumulate in memory."
  [log entry]
  (conj log entry))

;; Phase 1: Prepare

(defn send-prepare-request
  "Sends prepare request to a participant.

  Returns participant's vote: :yes, :no, or :timeout.
  In production, this would be a network call with actual timeout handling."
  [participant operations timeout-ms responses]
  (let [response (get-in responses [participant :prepare])]
    (cond
      ;; Simulated timeout - in production, use futures with timeout
      (and (map? response) (contains? response :delay-ms)
           (> (:delay-ms response) timeout-ms))
      {:vote :timeout
       :participant participant
       :reason "Prepare request timed out"}

      ;; No vote means failure
      (= response :no)
      {:vote :no
       :participant participant
       :reason (get-in responses [participant :reason] "Participant voted NO")}

      ;; Successful prepare
      (= response :yes)
      {:vote :yes
       :participant participant}

      ;; Unexpected response
      :else
      {:vote :timeout
       :participant participant
       :reason "Invalid or missing response"})))

(defn execute-prepare-phase
  "Executes Phase 1: Prepare.

  Sends prepare requests to all participants in parallel. Waits for all responses
  or timeout. Returns map with votes from each participant.

  Decision rule: All must vote YES to proceed to commit. Any NO or timeout triggers abort."
  [transaction participants timeout-ms responses log]
  (let [;; Send prepare to all participants
        prepare-results (into {}
                             (map (fn [participant]
                                    [participant
                                     (send-prepare-request
                                       participant
                                       (get-in transaction [:operations participant])
                                       timeout-ms
                                       responses)])
                                  participants))

        ;; Check if all voted YES
        all-yes? (every? #(= :yes (:vote %)) (vals prepare-results))

        ;; Determine decision
        decision (if all-yes? :can-commit :must-abort)

        ;; Find abort reason if any
        abort-reason (when (not all-yes?)
                      (let [failed (first (filter #(not= :yes (:vote %))
                                                 (vals prepare-results)))]
                        (str (:participant failed) " voted "
                             (name (:vote failed))
                             (when (:reason failed)
                               (str ": " (:reason failed))))))

        ;; Log prepare phase result
        log-entry (create-log-entry
                    (:id transaction)
                    :prepare
                    decision
                    {:votes prepare-results
                     :abort-reason abort-reason})

        new-log (append-to-log log log-entry)]

    {:decision decision
     :votes prepare-results
     :abort-reason abort-reason
     :log new-log
     :all-participants-ready? all-yes?}))

;; Phase 2: Commit or Abort

(defn send-commit-request
  "Sends commit request to participant.

  Returns :success if participant successfully committed, :failure otherwise."
  [participant responses]
  (let [response (get-in responses [participant :commit])]
    (if (= response :success)
      {:participant participant
       :result :success
       :message "Transaction committed"}
      {:participant participant
       :result :failure
       :message (str "Commit failed: " response)})))

(defn send-abort-request
  "Sends abort request to participant.

  Tells participant to roll back any prepared changes."
  [participant responses]
  (let [response (get-in responses [participant :abort] :aborted)]
    {:participant participant
     :result (if (= response :aborted) :aborted :failure)
     :message (if (= response :aborted)
               "Transaction aborted"
               "Abort failed - manual intervention required")}))

(defn execute-commit-phase
  "Executes Phase 2: Commit.

  After all participants voted YES in prepare phase, tells all to commit.
  Logs decision durably before sending messages (critical for recovery)."
  [transaction participants responses log]
  (let [;; Log commit decision BEFORE sending to participants
        log-entry (create-log-entry
                    (:id transaction)
                    :commit
                    :commit-all
                    {:participants participants})
        new-log (append-to-log log log-entry)

        ;; Send commit to all participants
        commit-results (into {}
                            (map (fn [participant]
                                   [participant
                                    (send-commit-request participant responses)])
                                 participants))

        ;; Check if all committed successfully
        all-success? (every? #(= :success (:result %)) (vals commit-results))]

    {:status (if all-success? :committed :partial-failure)
     :phase-2-results commit-results
     :log new-log
     :all-committed? all-success?}))

(defn execute-abort-phase
  "Executes Phase 2: Abort.

  When prepare phase fails, tells all participants to abort (rollback).
  Logs decision durably before sending messages."
  [transaction participants responses log abort-reason]
  (let [;; Log abort decision BEFORE sending to participants
        log-entry (create-log-entry
                    (:id transaction)
                    :abort
                    :abort-all
                    {:reason abort-reason
                     :participants participants})
        new-log (append-to-log log log-entry)

        ;; Send abort to all participants
        abort-results (into {}
                           (map (fn [participant]
                                  [participant
                                   (send-abort-request participant responses)])
                                participants))

        ;; Check if all aborted successfully
        all-aborted? (every? #(= :aborted (:result %)) (vals abort-results))

        ;; Identify participants needing manual intervention
        failed-aborts (filter #(not= :aborted (:result (val %))) abort-results)
        recovery-actions (when (seq failed-aborts)
                          (map (fn [[participant _]]
                                (str "Retry abort for " (name participant)))
                               failed-aborts))]

    {:status :aborted
     :phase-2-results abort-results
     :log new-log
     :all-aborted? all-aborted?
     :recovery-actions (vec recovery-actions)
     :manual-intervention-required (not all-aborted?)}))

;; Main Coordination Logic

(defn coordinate-2pc
  "Two-Phase Commit coordinator.

  Implements 2PC protocol with durable logging and timeout handling.

  Parameters:
  - transaction: Map with :id, :participants, :operations, :timeout-ms
  - participant-responses: Map simulating participant responses

  Returns: Map with :status, :phase-1-results, :phase-2-results, :log, :trace-id

  Protocol flow:
  1. Phase 1 (Prepare): Ask all participants if they can commit
  2. Decision: If ALL vote YES, proceed to commit. If ANY vote NO/timeout, abort.
  3. Phase 2 (Commit/Abort): Tell all participants the decision
  4. Log all decisions durably for recovery

  Transaction states:
  - :committed - All participants successfully committed
  - :aborted - Transaction aborted (prepare failed or explicit abort)
  - :timeout - Timeout in prepare phase
  - :partial-failure - Some participants failed in commit/abort phase"
  [transaction participant-responses]
  (let [{:keys [id participants timeout-ms]} transaction
        timeout (or timeout-ms 5000)
        initial-log []
        trace-id (str "TXN-" (java.util.UUID/randomUUID))

        ;; Phase 1: Prepare
        prepare-result (execute-prepare-phase
                         transaction
                         participants
                         timeout
                         participant-responses
                         initial-log)

        {:keys [decision votes abort-reason log]} prepare-result

        ;; Phase 2: Commit or Abort based on Phase 1 decision
        phase-2-result (if (= decision :can-commit)
                        ;; All participants ready, execute commit
                        (execute-commit-phase
                          transaction
                          participants
                          participant-responses
                          log)
                        ;; At least one participant not ready, execute abort
                        (execute-abort-phase
                          transaction
                          participants
                          participant-responses
                          log
                          abort-reason))]

    ;; Combine results
    (merge
      {:transaction-id id
       :trace-id trace-id
       :phase-1-results votes
       :abort-reason abort-reason}
      phase-2-result
      {:log (:log phase-2-result)})))

;; Recovery Functions

(defn recover-from-log
  "Recovers coordinator state from transaction log.

  If coordinator crashes, it can read the log to determine transaction state
  and complete any in-progress transactions. This is critical for durability."
  [log transaction-id]
  (let [entries (filter #(= (:transaction-id %) transaction-id) log)
        sorted-entries (sort-by :timestamp entries)
        latest-entry (last sorted-entries)]

    (when latest-entry
      {:transaction-id transaction-id
       :last-phase (:phase latest-entry)
       :last-decision (:decision latest-entry)
       :can-recover? true
       :action (case (:decision latest-entry)
                :can-commit "Proceed with commit phase"
                :commit-all "Verify all participants committed"
                :must-abort "Proceed with abort phase"
                :abort-all "Verify all participants aborted"
                "Unknown state - manual investigation required")})))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Two-Phase Commit Protocol
;;    2PC provides atomicity for distributed transactions through a voting protocol.
;;    Phase 1 (Prepare): The coordinator asks each participant "can you commit?"
;;    Participants prepare (lock resources, validate) and vote YES (ready to commit)
;;    or NO (cannot commit). Phase 2 (Commit/Abort): If all voted YES, coordinator
;;    sends COMMIT to all; if any voted NO, sends ABORT to all. The key property:
;;    once a participant votes YES, it MUST be able to commit when told to, even
;;    after crashes and restarts (achieved through durable logging). This ensures
;;    atomicity - all participants commit or all abort, never a mix. The trade-off
;;    is blocking: participants must hold locks until receiving the commit/abort
;;    decision, which can be long if the coordinator crashes.
;;
;; 2. Durable Logging
;;    The coordinator MUST log its decision before sending commit/abort messages.
;;    If it crashes after deciding but before sending messages, it can recover
;;    from the log and complete the transaction. Without durable logging, the
;;    coordinator might forget its decision after crashing, leaving participants
;;    blocking indefinitely. Participants also log their votes and wait for the
;;    decision. Logs must survive crashes, so they're written to disk or replicated
;;    databases. Write-ahead logging (WAL) ensures durability - log the action
;;    before doing it. In production, use transaction databases or replicated logs
;;    (like Kafka) that guarantee durability across failures.
;;
;; 3. Timeout Handling
;;    Every message in 2PC needs a timeout to prevent indefinite blocking. If a
;;    participant doesn't respond to prepare within the timeout, the coordinator
;;    treats it as a NO vote and aborts. If the coordinator crashes during commit
;;    phase, participants must timeout waiting for the decision. Timeout durations
;;    should account for network latency, processing time, and acceptable blocking
;;    duration. Set them too short and you get false failures; too long and you
;;    block resources unnecessarily. In production, use different timeouts for
;;    different phases and make them configurable. Consider exponential backoff
;;    for retries before final timeout.
;;
;; 4. Blocking and Availability
;;    2PC has a fundamental limitation: it blocks. Once a participant votes YES
;;    in prepare phase, it must hold its locks and wait for the commit/abort
;;    decision. If the coordinator crashes during this window, participants are
;;    stuck holding locks until a new coordinator takes over (which requires
;;    coordinator redundancy and failover). This reduces availability - the system
;;    can't make progress while waiting. Three-Phase Commit (3PC) attempts to
;;    solve this by adding a "pre-commit" phase, but adds complexity and still
;;    has issues with network partitions. Modern systems often prefer compensation-based
;;    approaches (sagas) that don't block, trading strong consistency for availability.
;;
;; 5. Coordinator Redundancy
;;    The coordinator is a single point of failure in 2PC. If it crashes and
;;    stays down, transactions block indefinitely. Production systems need
;;    coordinator redundancy: multiple coordinators with leader election (using
;;    Raft, Paxos, or ZooKeeper). When the primary coordinator fails, a backup
;;    becomes leader, reads the transaction log, and completes in-progress
;;    transactions. This requires shared durable storage (replicated log or
;;    database) accessible to all coordinator replicas. The log enables recovery:
;;    "Transaction X decided to commit but hasn't sent messages yet - send them now."
;;    Without redundancy, coordinator failure means manual intervention.
;;
;; 6. Participant State Machine
;;    Participants in 2PC follow a state machine: INIT → PREPARING → PREPARED
;;    (voted YES) → COMMITTED/ABORTED. They can only commit from PREPARED state
;;    and must be able to abort from any state. After voting YES, they MUST commit
;;    when told to, even after crash and recovery (via log replay). This is the
;;    "prepared" guarantee. They hold locks from PREPARING through COMMITTED/ABORTED.
;;    If they don't receive commit/abort within timeout, they can contact the
;;    coordinator or other participants to learn the decision. In production,
;;    implement participant recovery that checks coordinator state on restart and
;;    completes any prepared transactions.
;;
;; 7. When to Use 2PC
;;    2PC is appropriate when: (a) strong consistency is absolutely required (bank
;;    transfers, inventory management), (b) the number of participants is small
;;    (2-5), (c) all participants are within the same administrative domain (no
;;    untrusted participants), (d) availability can be sacrificed for consistency.
;;    DON'T use 2PC for: (a) high-scale systems (blocking hurts throughput), (b)
;;    cross-organization transactions (can't force external systems to block), (c)
;;    long-running transactions (holding locks too long), (d) network-partitioned
;;    environments. Consider alternatives: sagas for long transactions, eventual
;;    consistency with compensation, or consensus protocols like Raft/Paxos for
;;    replicated state machines.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/controllers/exemplo5.md
;;
;; Pattern used: Transactional coordination with sequential steps and state management
;;
;; The reference code demonstrates transaction management patterns:
;;
;; 1. Multi-step transaction flow:
;;    (let [transaction (fetch-or-create-record! ...)
;;          {:keys [card-id customer-id]} (get-pan-mapping! ...)]
;;      (-> money-out
;;          (logic.transaction/money-out->money-out-settled transaction)
;;          (producer/money-out-settled! ...))
;;      (-> money-out
;;          (logic.transaction/money-out->money-in transaction ...)
;;          (producer/new-money-in! ...)))
;;
;;    This shows orchestrating multiple operations in a transactional flow, similar
;;    to how 2PC coordinates prepare and commit across participants. Each step
;;    depends on previous steps completing successfully.
;;
;; 2. State management and recovery:
;;    (or (db.transaction/fetch tracking-key docstore-transactions)
;;        (create-record! ...))
;;
;;    This pattern of "fetch or create" ensures idempotency and recovery - similar
;;    to how 2PC uses logs to recover coordinator state after crashes. The system
;;    can determine if a transaction already exists and continue from there.
;;
;; 3. Validation before execution:
;;    (when-not (logic.transaction/valid? money-out)
;;      (ex/invalid-input! {:reason :invalid-transaction}))
;;
;;    Early validation prevents starting transactions that will fail, analogous
;;    to the prepare phase checking if participants can commit before proceeding.
;;
;; 4. Feature-flag controlled routing:
;;    (if new-lending-global-routing?
;;      (create-record-v2! ...)
;;      (create-record! ...))
;;
;;    Conditional logic for different transaction paths, similar to how 2PC
;;    branches to commit or abort based on prepare phase results.
;;
;; Real-world usage: The money transfer controller manages complex transactional
;; flows across multiple services and databases. It must ensure consistency when
;; moving money between accounts, handling failures appropriately, and maintaining
;; audit trails. While it uses saga-style compensation rather than 2PC, the
;; coordination patterns are similar: prepare resources, make decisions based on
;; preconditions, execute or rollback, and maintain durable state for recovery.
;; Our 2PC implementation provides stronger consistency guarantees through blocking
;; and two-phase voting, appropriate for operations where atomic commitment is
;; absolutely required.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Successful 2PC - all participants vote YES and commit
  (coordinate-2pc
    {:id "TXN-123"
     :participants [:db-orders :db-inventory :db-payment]
     :operations {:db-orders {:action :insert :data {:order-id "ORD-123"}}
                  :db-inventory {:action :decrement :sku "ITEM-1" :qty 2}
                  :db-payment {:action :charge :amount 100}}
     :timeout-ms 5000}
    {:db-orders {:prepare :yes :commit :success}
     :db-inventory {:prepare :yes :commit :success}
     :db-payment {:prepare :yes :commit :success}})
  ;; => {:transaction-id "TXN-123"
  ;;     :status :committed
  ;;     :phase-1-results {:db-orders {:vote :yes :participant :db-orders}
  ;;                       :db-inventory {:vote :yes :participant :db-inventory}
  ;;                       :db-payment {:vote :yes :participant :db-payment}}
  ;;     :phase-2-results {:db-orders {:participant :db-orders :result :success}
  ;;                       :db-inventory {:participant :db-inventory :result :success}
  ;;                       :db-payment {:participant :db-payment :result :success}}
  ;;     :log [{:phase :prepare :decision :can-commit ...}
  ;;           {:phase :commit :decision :commit-all ...}]
  ;;     :all-committed? true}

  ;; Example 2: Abort due to one NO vote
  (coordinate-2pc
    {:id "TXN-456"
     :participants [:db-orders :db-inventory :db-payment]
     :operations {:db-orders {:action :insert}
                  :db-inventory {:action :decrement}
                  :db-payment {:action :charge}}
     :timeout-ms 5000}
    {:db-orders {:prepare :yes :abort :aborted}
     :db-inventory {:prepare :no :reason "Insufficient stock" :abort :aborted}
     :db-payment {:prepare :yes :abort :aborted}})
  ;; => {:transaction-id "TXN-456"
  ;;     :status :aborted
  ;;     :phase-1-results {:db-orders {:vote :yes ...}
  ;;                       :db-inventory {:vote :no :reason "Insufficient stock" ...}
  ;;                       :db-payment {:vote :yes ...}}
  ;;     :abort-reason "db-inventory voted no: Insufficient stock"
  ;;     :phase-2-results {:db-orders {:result :aborted ...}
  ;;                       :db-inventory {:result :aborted ...}
  ;;                       :db-payment {:result :aborted ...}}
  ;;     :log [{:phase :prepare :decision :must-abort ...}
  ;;           {:phase :abort :decision :abort-all ...}]
  ;;     :all-aborted? true}

  ;; Example 3: Timeout in prepare phase triggers abort
  (coordinate-2pc
    {:id "TXN-789"
     :participants [:db-orders :db-payment]
     :operations {:db-orders {:action :insert}
                  :db-payment {:action :charge}}
     :timeout-ms 1000}
    {:db-orders {:prepare :yes :delay-ms 2000 :abort :aborted}  ; Simulated timeout
     :db-payment {:prepare :yes :abort :aborted}})
  ;; => {:transaction-id "TXN-789"
  ;;     :status :aborted
  ;;     :phase-1-results {:db-orders {:vote :timeout :reason "Prepare request timed out"}
  ;;                       :db-payment {:vote :yes}}
  ;;     :abort-reason "db-orders voted timeout: Prepare request timed out"
  ;;     :phase-2-results {:db-orders {:result :aborted}
  ;;                       :db-payment {:result :aborted}}
  ;;     :all-aborted? true}

  ;; Example 4: Partial failure in abort phase (manual intervention needed)
  (coordinate-2pc
    {:id "TXN-999"
     :participants [:db-orders :db-inventory]
     :operations {:db-orders {:action :insert}
                  :db-inventory {:action :decrement}}
     :timeout-ms 5000}
    {:db-orders {:prepare :yes :abort :failure}  ; Abort fails
     :db-inventory {:prepare :no :abort :aborted}})
  ;; => {:transaction-id "TXN-999"
  ;;     :status :aborted
  ;;     :phase-1-results {:db-orders {:vote :yes}
  ;;                       :db-inventory {:vote :no}}
  ;;     :phase-2-results {:db-orders {:result :failure
  ;;                                   :message "Abort failed - manual intervention required"}
  ;;                       :db-inventory {:result :aborted}}
  ;;     :all-aborted? false
  ;;     :recovery-actions ["Retry abort for db-orders"]
  ;;     :manual-intervention-required true}

  ;; Example 5: Transaction log for recovery
  (def transaction-log
    [{:transaction-id "TXN-123"
      :phase :prepare
      :decision :can-commit
      :timestamp #inst "2024-01-15T10:00:00"}
     {:transaction-id "TXN-123"
      :phase :commit
      :decision :commit-all
      :timestamp #inst "2024-01-15T10:00:01"}])

  (recover-from-log transaction-log "TXN-123")
  ;; => {:transaction-id "TXN-123"
  ;;     :last-phase :commit
  ;;     :last-decision :commit-all
  ;;     :can-recover? true
  ;;     :action "Verify all participants committed"}

  ;; Example 6: Cross-database transaction (bank transfer)
  (coordinate-2pc
    {:id "TRANSFER-555"
     :participants [:account-source :account-destination]
     :operations {:account-source {:action :debit :amount 100}
                  :account-destination {:action :credit :amount 100}}
     :timeout-ms 3000}
    {:account-source {:prepare :yes :commit :success}
     :account-destination {:prepare :yes :commit :success}})
  ;; => {:status :committed
  ;;     :all-committed? true
  ;;     :log [{:phase :prepare :decision :can-commit}
  ;;           {:phase :commit :decision :commit-all}]}

  ;; Example 7: All participants vote YES but one fails to commit (rare but possible)
  (coordinate-2pc
    {:id "TXN-777"
     :participants [:service-a :service-b]
     :operations {:service-a {:action :update}
                  :service-b {:action :update}}
     :timeout-ms 5000}
    {:service-a {:prepare :yes :commit :success}
     :service-b {:prepare :yes :commit :failure}})  ; Prepared but commit failed
  ;; => {:status :partial-failure
  ;;     :phase-1-results {:service-a {:vote :yes} :service-b {:vote :yes}}
  ;;     :phase-2-results {:service-a {:result :success}
  ;;                       :service-b {:result :failure
  ;;                                  :message "Commit failed: failure"}}
  ;;     :all-committed? false
  ;;     ;; This is a serious problem - service-a committed but service-b didn't
  ;;     ;; Manual intervention or compensating transactions required
  ;;     }
)

;; TESTS
;; -----

(defn -test []
  ;; Test 1: Successful 2PC with all participants voting YES
  (let [result (coordinate-2pc
                 {:id "TEST-1"
                  :participants [:db1 :db2]
                  :operations {:db1 {:action :insert}
                               :db2 {:action :update}}
                  :timeout-ms 5000}
                 {:db1 {:prepare :yes :commit :success}
                  :db2 {:prepare :yes :commit :success}})]
    (assert (= (:status result) :committed)
            "Should commit when all participants vote YES")
    (assert (:all-committed? result)
            "All participants should have committed")
    (assert (= (count (:log result)) 2)
            "Log should have prepare and commit entries"))

  ;; Test 2: Abort when one participant votes NO
  (let [result (coordinate-2pc
                 {:id "TEST-2"
                  :participants [:db1 :db2]
                  :operations {:db1 {:action :insert}
                               :db2 {:action :update}}
                  :timeout-ms 5000}
                 {:db1 {:prepare :yes :abort :aborted}
                  :db2 {:prepare :no :reason "Constraint violation" :abort :aborted}})]
    (assert (= (:status result) :aborted)
            "Should abort when any participant votes NO")
    (assert (:all-aborted? result)
            "All participants should have aborted")
    (assert (str/includes? (:abort-reason result) "db2")
            "Abort reason should mention failing participant"))

  ;; Test 3: Timeout handling
  (let [result (coordinate-2pc
                 {:id "TEST-3"
                  :participants [:db1 :db2]
                  :operations {:db1 {:action :insert}
                               :db2 {:action :update}}
                  :timeout-ms 1000}
                 {:db1 {:prepare :yes :abort :aborted}
                  :db2 {:prepare :yes :delay-ms 2000 :abort :aborted}})]
    (assert (= (:status result) :aborted)
            "Should abort on timeout")
    (assert (= (get-in result [:phase-1-results :db2 :vote]) :timeout)
            "Timeout should be detected"))

  ;; Test 4: Transaction log includes all phases
  (let [result (coordinate-2pc
                 {:id "TEST-4"
                  :participants [:db1]
                  :operations {:db1 {:action :test}}
                  :timeout-ms 5000}
                 {:db1 {:prepare :yes :commit :success}})
        log (:log result)]
    (assert (>= (count log) 2)
            "Log should include prepare and commit phases")
    (assert (some #(= (:phase %) :prepare) log)
            "Log should include prepare phase")
    (assert (some #(= (:phase %) :commit) log)
            "Log should include commit phase"))

  ;; Test 5: Trace ID is generated
  (let [result (coordinate-2pc
                 {:id "TEST-5"
                  :participants [:db1]
                  :operations {:db1 {:action :test}}
                  :timeout-ms 5000}
                 {:db1 {:prepare :yes :commit :success}})]
    (assert (contains? result :trace-id)
            "Should include trace ID for distributed tracing")
    (assert (str/starts-with? (:trace-id result) "TXN-")
            "Trace ID should have proper format"))

  ;; Test 6: Recovery from log
  (let [test-log [{:transaction-id "RECOVER-1"
                   :phase :commit
                   :decision :commit-all
                   :timestamp (java.util.Date.)}]
        recovery (recover-from-log test-log "RECOVER-1")]
    (assert (:can-recover? recovery)
            "Should be able to recover from log")
    (assert (= (:last-decision recovery) :commit-all)
            "Should recover last decision"))

  ;; Test 7: Partial failure in abort phase
  (let [result (coordinate-2pc
                 {:id "TEST-7"
                  :participants [:db1 :db2]
                  :operations {:db1 {:action :test}
                               :db2 {:action :test}}
                  :timeout-ms 5000}
                 {:db1 {:prepare :no :abort :failure}
                  :db2 {:prepare :yes :abort :aborted}})]
    (assert (= (:status result) :aborted)
            "Should have aborted status")
    (assert (not (:all-aborted? result))
            "Not all participants should have aborted successfully")
    (assert (:manual-intervention-required result)
            "Should flag need for manual intervention")
    (assert (seq (:recovery-actions result))
            "Should provide recovery actions"))

  (println "✓ All tests passed! The 2PC coordinator works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
