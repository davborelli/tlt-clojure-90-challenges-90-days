;; =============================================================================
;; 065 - FEATURE FLAG WORKFLOW
;; Level: 13/18 | Type: Controller
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This controller demonstrates feature flag-driven workflow using threading
;; macros. Feature flags enable runtime control of application behavior without
;; code deployments, supporting A/B testing, gradual rollouts, and emergency
;; feature disabling.
;;
;; We use `cond->` which conditionally applies transformations based on predicates.
;; This makes the workflow explicit: each step clearly shows its condition and
;; transformation. The alternative (nested ifs) would obscure the data flow.
;;
;; This pattern is fundamental in modern applications where features must be
;; controllable at runtime for testing, rollout safety, and operational flexibility.

(ns challenge-065.solution)

;; HELPER FUNCTIONS
;; ----------------

(defn basic-validation
  "Performs basic validation on request.

  Parameters:
  - request: Request map

  Returns: Request with :validation-result \"basic\""
  [request]
  (assoc request :validation-result "basic"))

(defn enhanced-validation
  "Performs enhanced validation (upgrades from basic).

  Parameters:
  - request: Request map

  Returns: Request with :validation-result \"enhanced\""
  [request]
  (assoc request :validation-result "enhanced"))

(defn standard-processing
  "Performs standard request processing.

  Parameters:
  - request: Request map

  Returns: Request with :processing-result \"standard\""
  [request]
  (assoc request :processing-result "standard"))

(defn premium-processing
  "Performs premium processing (upgrades from standard).

  Parameters:
  - request: Request map

  Returns: Request with :processing-result \"premium\""
  [request]
  (assoc request :processing-result "premium"))

(defn track-analytics
  "Tracks analytics for request.

  Parameters:
  - request: Request map

  Returns: Request with :tracked true"
  [request]
  (assoc request :tracked true))

(defn mark-completed
  "Marks request as completed.

  Parameters:
  - request: Request map

  Returns: Request with :status :completed"
  [request]
  (assoc request :status :completed))

;; MAIN CONTROLLER
;; ---------------

(defn process-request
  "Processes request through feature-flag driven workflow.

  Workflow:
  1. Basic validation (always)
  2. Enhanced validation (if :enhanced-validation flag)
  3. Standard processing (always)
  4. Premium processing (if :premium-processing flag)
  5. Analytics tracking (if :analytics-tracking flag)
  6. Mark completed (always)

  Parameters:
  - request: Request map with :features containing feature flags

  Returns: Processed request with added fields

  Uses cond-> for conditional transformations based on feature flags."
  [request]
  (cond-> request
    ;; Always run basic validation
    true
    basic-validation

    ;; Enhanced validation if flag enabled
    (get-in request [:features :enhanced-validation])
    enhanced-validation

    ;; Always run standard processing
    true
    standard-processing

    ;; Premium processing if flag enabled
    (get-in request [:features :premium-processing])
    premium-processing

    ;; Track analytics if flag enabled
    (get-in request [:features :analytics-tracking])
    track-analytics

    ;; Always mark completed
    true
    mark-completed))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Feature Flags (Feature Toggles)
;;    Feature flags control feature availability at runtime:
;;      {:features {:enhanced-validation true
;;                  :premium-processing false}}
;;    Benefits:
;;    - A/B testing (show feature to 50% of users)
;;    - Gradual rollouts (enable for 1%, then 10%, then 100%)
;;    - Emergency kill switches (disable buggy feature instantly)
;;    - Development (merge code before feature is ready)
;;
;; 2. cond-> Threading Macro
;;    cond-> conditionally applies transformations:
;;      (cond-> initial-value
;;        condition1 (function1)
;;        condition2 (function2))
;;    Evaluates each condition. If truthy, applies the function.
;;    This is cleaner than:
;;      (let [v initial-value
;;            v (if condition1 (function1 v) v)
;;            v (if condition2 (function2 v) v)]
;;        v)
;;
;; 3. Workflow Orchestration
;;    Threading macros make workflows explicit:
;;      request → validate → process → track → complete
;;    Each step is clearly visible. Compare to nested function calls:
;;      (complete (track (process (validate request))))
;;    Threading reads top-to-bottom like a procedure.
;;
;; 4. Conditional vs Always Steps
;;    Some steps always run (validation, completion), others are conditional
;;    (premium features). Pattern:
;;      (cond-> request
;;        true (always-step)
;;        flag? (conditional-step))
;;    This makes the workflow self-documenting.
;;
;; 5. Feature Flag Sources
;;    In production, flags come from:
;;    - Configuration files (static flags)
;;    - Feature flag services (LaunchDarkly, Split.io)
;;    - Database (user-specific flags)
;;    - Experiments platform (A/B test assignments)
;;    This challenge uses request-embedded flags for simplicity.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/controllers/exemplo2.md
;;
;; Pattern used: Feature-flag driven workflow with cond->
;;
;; Real-world usage: The reference shows experiments/flags in request processing:
;;   (defn process-request [req]
;;     (cond-> req
;;       true (validate-request)
;;       (experiment-enabled? :new-flow) (new-processing)
;;       (not (experiment-enabled? :new-flow)) (old-processing)
;;       (feature-on? :analytics) (track-event)
;;       true (respond)))
;;
;; Production systems use this pattern for:
;; - Gradual feature rollouts (percentage-based enabling)
;; - A/B testing (variant assignment)
;; - Premium feature gating (paid vs free tiers)
;; - Emergency rollback (disable without deploy)

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: All flags disabled (basic flow)
  (process-request
    {:user-id "U123"
     :action "submit"
     :data {:amount 100}
     :features {:enhanced-validation false
                :premium-processing false
                :analytics-tracking false}})
  ;; => {:user-id "U123"
  ;;     :action "submit"
  ;;     :data {:amount 100}
  ;;     :features {...}
  ;;     :validation-result "basic"
  ;;     :processing-result "standard"
  ;;     :status :completed}

  ;; Example 2: All flags enabled (premium flow)
  (process-request
    {:user-id "U456"
     :action "submit"
     :data {:amount 500}
     :features {:enhanced-validation true
                :premium-processing true
                :analytics-tracking true}})
  ;; => {:user-id "U456"
  ;;     :action "submit"
  ;;     :data {:amount 500}
  ;;     :features {...}
  ;;     :validation-result "enhanced"
  ;;     :processing-result "premium"
  ;;     :tracked true
  ;;     :status :completed}

  ;; Example 3: Mixed flags (partial features)
  (process-request
    {:user-id "U789"
     :action "update"
     :data {:changes {:status "active"}}
     :features {:enhanced-validation true
                :premium-processing false
                :analytics-tracking true}})
  ;; => {:user-id "U789"
  ;;     :validation-result "enhanced"    ; Enhanced enabled
  ;;     :processing-result "standard"    ; Premium disabled
  ;;     :tracked true                    ; Analytics enabled
  ;;     :status :completed}
)

;; TESTS
;; -----

(defn -test []
  ;; Test all flags disabled (basic flow)
  (let [result (process-request
                 {:user-id "U123"
                  :action "submit"
                  :data {:amount 100}
                  :features {:enhanced-validation false
                             :premium-processing false
                             :analytics-tracking false}})]
    (assert (= (:user-id result) "U123")
            "Should preserve user-id")
    (assert (= (:validation-result result) "basic")
            "Should use basic validation when flag disabled")
    (assert (= (:processing-result result) "standard")
            "Should use standard processing when flag disabled")
    (assert (nil? (:tracked result))
            "Should not track analytics when flag disabled")
    (assert (= (:status result) :completed)
            "Should mark as completed"))

  ;; Test all flags enabled (premium flow)
  (let [result (process-request
                 {:user-id "U456"
                  :action "submit"
                  :data {:amount 500}
                  :features {:enhanced-validation true
                             :premium-processing true
                             :analytics-tracking true}})]
    (assert (= (:validation-result result) "enhanced")
            "Should use enhanced validation when flag enabled")
    (assert (= (:processing-result result) "premium")
            "Should use premium processing when flag enabled")
    (assert (true? (:tracked result))
            "Should track analytics when flag enabled")
    (assert (= (:status result) :completed)
            "Should mark as completed"))

  ;; Test mixed flags
  (let [result (process-request
                 {:user-id "U789"
                  :action "update"
                  :data {:changes {:status "active"}}
                  :features {:enhanced-validation true
                             :premium-processing false
                             :analytics-tracking true}})]
    (assert (= (:validation-result result) "enhanced")
            "Should use enhanced validation")
    (assert (= (:processing-result result) "standard")
            "Should use standard processing")
    (assert (true? (:tracked result))
            "Should track analytics"))

  (println "✓ All tests passed! The process-request function works correctly."))

;; Run: (-test)
