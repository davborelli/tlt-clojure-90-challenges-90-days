;; =============================================================================
;; 066 - STATE MACHINE TRANSITION
;; Level: 14/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This function implements a state machine with guarded transitions for order
;; lifecycle management. State machines enforce business rules by limiting
;; which states can follow which, and under what conditions.
;;
;; We use cond to match state/event pairs and check guard conditions. Guards
;; are predicates that must be satisfied for a transition to occur (e.g., payment
;; must be confirmed before confirming an order).
;;
;; This pattern is fundamental in workflow systems, UI flows, and business
;; process automation where state must change according to strict rules.

(ns challenge-066.solution)

;; IMPLEMENTATION
;; --------------

(defn next-state
  "Determines next state in order state machine based on current state, event, and guards.

  State machine:
  - pending → confirmed (if :confirm event + payment + stock)
  - pending → cancelled (if :cancel event)
  - confirmed → shipped (if :ship event + valid address)
  - confirmed → cancelled (if :cancel event)
  - shipped → delivered (if :deliver event)
  - delivered → (terminal state)
  - cancelled → (terminal state)

  Parameters:
  - current-state: Current state keyword
  - event: Triggering event keyword
  - context: Map with guard conditions

  Returns: Next state keyword, or :invalid-transition"
  [current-state event context]
  (cond
    ;; From :pending
    (and (= current-state :pending)
         (= event :confirm)
         (:payment-confirmed context)
         (:items-in-stock context))
    :confirmed

    (and (= current-state :pending)
         (= event :cancel))
    :cancelled

    ;; From :confirmed
    (and (= current-state :confirmed)
         (= event :ship)
         (:address-valid context))
    :shipped

    (and (= current-state :confirmed)
         (= event :cancel))
    :cancelled

    ;; From :shipped
    (and (= current-state :shipped)
         (= event :deliver))
    :delivered

    ;; Terminal states (:delivered, :cancelled) have no transitions
    ;; All other combinations are invalid
    :else
    :invalid-transition))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. State Machines
;;    A state machine consists of:
;;    - States: Possible conditions (:pending, :confirmed, :shipped, etc.)
;;    - Events: Triggers that cause transitions (:confirm, :ship, :cancel)
;;    - Transitions: Valid state changes (pending + confirm → confirmed)
;;    - Guards: Conditions that must be true for transition
;;
;;    Benefits:
;;    - Enforces valid workflows (can't deliver before confirming)
;;    - Makes state changes explicit and auditable
;;    - Prevents invalid states (no "shipped but not confirmed")
;;
;; 2. Guarded Transitions
;;    Not all events cause transitions. Guards add conditions:
;;      pending + confirm → confirmed IF payment confirmed AND items in stock
;;    Without guards met:
;;      pending + confirm → invalid-transition
;;
;;    Guards encode business rules:
;;    - Can't confirm without payment
;;    - Can't ship to invalid address
;;    - Can't cancel after shipping (customer protection)
;;
;; 3. Terminal States
;;    Some states have no outgoing transitions:
;;    - :delivered (order complete)
;;    - :cancelled (order aborted)
;;    Any event from these states → :invalid-transition
;;    This prevents resurrection of completed/cancelled orders.
;;
;; 4. Pattern Matching State/Event Pairs
;;    We use cond to match combinations:
;;      (and (= state :pending) (= event :confirm) guards...)
;;    Each branch represents one valid transition.
;;    This is more maintainable than nested case statements.
;;
;; 5. Default to Invalid
;;    The :else clause returns :invalid-transition.
;;    This is defensive: undefined combinations are rejected.
;;    Explicit is better than implicit (no silent failures).

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo3.md
;;
;; Pattern used: Guarded state machine transitions
;;
;; Real-world usage: The reference shows similar state transitions in workflows:
;;   (defn process-status-change [current-status event context]
;;     (cond
;;       (and (= current-status :draft) (= event :submit) (:all-required-fields? context))
;;       :pending-review
;;
;;       (and (= current-status :pending-review) (= event :approve))
;;       :approved
;;
;;       :else
;;       :invalid))
;;
;; Production systems use state machines for:
;; - Order processing (pending → confirmed → shipped → delivered)
;; - Payment flows (initiated → authorized → captured → settled)
;; - Approval workflows (draft → review → approved → published)
;; - User onboarding (registered → verified → active)

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Valid transition - confirm order with guards satisfied
  (next-state :pending :confirm
              {:payment-confirmed true :items-in-stock true :address-valid true})
  ;; => :confirmed

  ;; Example 2: Invalid - confirm without payment
  (next-state :pending :confirm
              {:payment-confirmed false :items-in-stock true :address-valid true})
  ;; => :invalid-transition

  ;; Example 3: Valid transition - cancel pending order
  (next-state :pending :cancel
              {:payment-confirmed false :items-in-stock false :address-valid false})
  ;; => :cancelled

  ;; Example 4: Valid transition - ship with valid address
  (next-state :confirmed :ship
              {:payment-confirmed true :items-in-stock true :address-valid true})
  ;; => :shipped

  ;; Example 5: Invalid - ship without valid address
  (next-state :confirmed :ship
              {:payment-confirmed true :items-in-stock true :address-valid false})
  ;; => :invalid-transition

  ;; Example 6: Invalid - cannot cancel after shipping
  (next-state :shipped :cancel
              {:payment-confirmed true :items-in-stock true :address-valid true})
  ;; => :invalid-transition

  ;; Example 7: Valid - deliver shipped order
  (next-state :shipped :deliver
              {:payment-confirmed true :items-in-stock true :address-valid true})
  ;; => :delivered

  ;; Example 8: Invalid - no transitions from delivered
  (next-state :delivered :ship
              {:payment-confirmed true :items-in-stock true :address-valid true})
  ;; => :invalid-transition
)

;; TESTS
;; -----

(defn -test []
  ;; Test valid transitions from :pending
  (assert (= (next-state :pending :confirm
                         {:payment-confirmed true :items-in-stock true :address-valid true})
             :confirmed)
          "Should transition to confirmed when guards satisfied")

  (assert (= (next-state :pending :cancel
                         {:payment-confirmed false :items-in-stock false :address-valid false})
             :cancelled)
          "Should allow cancellation from pending")

  ;; Test invalid transitions from :pending (failed guards)
  (assert (= (next-state :pending :confirm
                         {:payment-confirmed false :items-in-stock true :address-valid true})
             :invalid-transition)
          "Should reject confirm without payment")

  (assert (= (next-state :pending :confirm
                         {:payment-confirmed true :items-in-stock false :address-valid true})
             :invalid-transition)
          "Should reject confirm without stock")

  ;; Test valid transitions from :confirmed
  (assert (= (next-state :confirmed :ship
                         {:payment-confirmed true :items-in-stock true :address-valid true})
             :shipped)
          "Should transition to shipped when address valid")

  (assert (= (next-state :confirmed :cancel
                         {:payment-confirmed true :items-in-stock true :address-valid true})
             :cancelled)
          "Should allow cancellation from confirmed")

  ;; Test invalid transition from :confirmed (failed guard)
  (assert (= (next-state :confirmed :ship
                         {:payment-confirmed true :items-in-stock true :address-valid false})
             :invalid-transition)
          "Should reject ship without valid address")

  ;; Test transitions from :shipped
  (assert (= (next-state :shipped :deliver
                         {:payment-confirmed true :items-in-stock true :address-valid true})
             :delivered)
          "Should transition to delivered")

  (assert (= (next-state :shipped :cancel
                         {:payment-confirmed true :items-in-stock true :address-valid true})
             :invalid-transition)
          "Should not allow cancellation after shipping")

  ;; Test terminal states
  (assert (= (next-state :delivered :ship
                         {:payment-confirmed true :items-in-stock true :address-valid true})
             :invalid-transition)
          "Should reject all transitions from delivered")

  (assert (= (next-state :cancelled :confirm
                         {:payment-confirmed true :items-in-stock true :address-valid true})
             :invalid-transition)
          "Should reject all transitions from cancelled")

  (println "✓ All tests passed! The next-state function works correctly."))

;; Run: (-test)
