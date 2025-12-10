;; =============================================================================
;; 057 - EXTRACT NESTED METRICS
;; Level: 12/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This solution demonstrates advanced destructuring to extract deeply nested
;; metrics (3 levels deep) and calculate aggregate statistics. We use nested
;; destructuring to pull out all required values in one operation, then perform
;; calculations to derive additional metrics.
;;
;; The approach destructures the complex nested structure step-by-step: first
;; extract top-level keys, then metrics, then engagement/revenue, then
;; daily/weekly. After extraction, we calculate totals and percentages.
;;
;; This pattern is common in analytics and reporting systems where raw data
;; is deeply nested by category and time period, but reports need flattened
;; summaries with calculated KPIs.

(ns challenge-057.solution)

;; HELPER FUNCTIONS
;; ----------------

(defn round-2-decimals
  "Rounds number to 2 decimal places.

  Parameters:
  - value: Number to round

  Returns: Number rounded to 2 decimals"
  [value]
  (/ (Math/round (* value 100.0)) 100.0))

;; IMPLEMENTATION
;; --------------

(defn extract-metrics
  "Extracts nested metrics and calculates summaries.

  Parameters:
  - analytics-report: Deeply nested analytics data

  Returns: Flattened metrics with calculated fields"
  [analytics-report]
  (let [;; Extract top-level user-id
        {:keys [user-id metrics]} analytics-report
        ;; Extract engagement and revenue
        {:keys [engagement revenue]} metrics
        ;; Extract daily and weekly engagement
        daily-eng (:daily engagement)
        weekly-eng (:weekly engagement)
        ;; Extract daily and weekly revenue
        daily-rev (:daily revenue)
        weekly-rev (:weekly revenue)
        ;; Extract individual engagement metrics
        daily-views (:views daily-eng)
        daily-clicks (:clicks daily-eng)
        daily-shares (:shares daily-eng)
        weekly-views (:views weekly-eng)
        weekly-clicks (:clicks weekly-eng)
        weekly-shares (:shares weekly-eng)
        ;; Extract revenue metrics
        daily-amount (:amount daily-rev)
        daily-txns (:transactions daily-rev)
        weekly-amount (:amount weekly-rev)
        weekly-txns (:transactions weekly-rev)
        ;; Calculate totals
        total-daily-eng (+ daily-views daily-clicks daily-shares)
        total-weekly-eng (+ weekly-views weekly-clicks weekly-shares)
        ;; Calculate click rates (percentage)
        daily-click-rate (round-2-decimals (* (/ daily-clicks daily-views) 100.0))
        weekly-click-rate (round-2-decimals (* (/ weekly-clicks weekly-views) 100.0))]
    ;; Assemble result
    {:user-id user-id
     :daily-views daily-views
     :daily-clicks daily-clicks
     :daily-shares daily-shares
     :weekly-views weekly-views
     :weekly-clicks weekly-clicks
     :weekly-shares weekly-shares
     :daily-revenue daily-amount
     :daily-transactions daily-txns
     :weekly-revenue weekly-amount
     :weekly-transactions weekly-txns
     :total-daily-engagement total-daily-eng
     :total-weekly-engagement total-weekly-eng
     :daily-click-rate daily-click-rate
     :weekly-click-rate weekly-click-rate}))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Advanced Nested Destructuring
;;    The analytics report has 4 levels of nesting:
;;    - Level 1: :user-id, :metrics
;;    - Level 2: :engagement, :revenue
;;    - Level 3: :daily, :weekly
;;    - Level 4: :views, :clicks, :shares, :amount, :transactions
;;
;;    We could destructure in one step:
;;      [{:keys [user-id]
;;        {engagement {:daily {:keys [views clicks shares]}
;;                     :weekly {...}}
;;         revenue {...}} :metrics}]
;;    But this becomes hard to read. Using let with step-by-step extraction
;;    is more maintainable for deep nesting.
;;
;; 2. Calculated Metrics / KPIs
;;    Raw data rarely provides insights directly. We calculate:
;;    - Total engagement (sum of views, clicks, shares)
;;    - Click rate (clicks/views percentage)
;;    These derived metrics are Key Performance Indicators (KPIs) that
;;    business stakeholders care about.
;;
;; 3. Percentage Calculations
;;    Click rate as percentage:
;;      (/ clicks views) => 0.05 (5% as decimal)
;;      (* 0.05 100.0) => 5.0 (as percentage)
;;    Always use 100.0 (float) not 100 (int) to avoid integer division.
;;
;; 4. Rounding Floating Point Numbers
;;    To round to 2 decimals:
;;      (* value 100.0)        => 500.0
;;      (Math/round 500.0)     => 500 (long)
;;      (/ 500 100.0)          => 5.0 (2 decimals)
;;    This avoids floating point precision issues like 5.000000001.
;;
;; 5. Flattening for Reporting
;;    Analytics storage uses nested structure (organized by category).
;;    Reports use flat structure (easy to display in tables/charts).
;;    This transformation is essential for dashboards and BI tools.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo1.md
;;
;; Pattern used: Nested destructuring with calculated metrics
;;
;; Real-world usage: Analytics and reporting extract nested data:
;;   (defn build-dashboard-metrics [raw-analytics]
;;     (let [{:keys [user-id] {...} :metrics} raw-analytics
;;           total-revenue (+ daily-rev weekly-rev monthly-rev)
;;           avg-transaction (/ total-revenue total-txns)
;;           conversion-rate (* (/ conversions visits) 100.0)]
;;       {:user-id user-id
;;        :total-revenue total-revenue
;;        :avg-transaction avg-transaction
;;        :conversion-rate conversion-rate}))
;;
;; The reference shows similar extraction and calculation patterns for
;; transforming raw nested data into actionable metrics.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Complete analytics report
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

  ;; Example 2: Different user with different metrics
  (extract-metrics
    {:user-id "USER-456"
     :metrics {:engagement {:daily {:views 500 :clicks 100 :shares 20}
                            :weekly {:views 3500 :clicks 700 :shares 140}}
               :revenue {:daily {:amount 500.00 :transactions 50}
                         :weekly {:amount 3500.00 :transactions 350}}}})
  ;; => {:daily-click-rate 20.0
  ;;     :weekly-click-rate 20.0
  ;;     :total-daily-engagement 620
  ;;     :total-weekly-engagement 4340}
)

;; TESTS
;; -----

(defn -test []
  (let [result (extract-metrics
                 {:user-id "USER-123"
                  :metrics {:engagement {:daily {:views 1000 :clicks 50 :shares 10}
                                         :weekly {:views 7000 :clicks 350 :shares 70}}
                            :revenue {:daily {:amount 250.00 :transactions 25}
                                      :weekly {:amount 1750.00 :transactions 175}}}})]
    ;; Test user ID extraction
    (assert (= (:user-id result) "USER-123")
            "Should extract user-id")

    ;; Test daily engagement extraction
    (assert (= (:daily-views result) 1000)
            "Should extract daily views")
    (assert (= (:daily-clicks result) 50)
            "Should extract daily clicks")
    (assert (= (:daily-shares result) 10)
            "Should extract daily shares")

    ;; Test weekly engagement extraction
    (assert (= (:weekly-views result) 7000)
            "Should extract weekly views")
    (assert (= (:weekly-clicks result) 350)
            "Should extract weekly clicks")
    (assert (= (:weekly-shares result) 70)
            "Should extract weekly shares")

    ;; Test revenue extraction
    (assert (= (:daily-revenue result) 250.0)
            "Should extract daily revenue")
    (assert (= (:daily-transactions result) 25)
            "Should extract daily transactions")
    (assert (= (:weekly-revenue result) 1750.0)
            "Should extract weekly revenue")
    (assert (= (:weekly-transactions result) 175)
            "Should extract weekly transactions")

    ;; Test calculated metrics
    (assert (= (:total-daily-engagement result) 1060)
            "Should calculate total daily engagement (1000+50+10)")
    (assert (= (:total-weekly-engagement result) 7420)
            "Should calculate total weekly engagement (7000+350+70)")
    (assert (= (:daily-click-rate result) 5.0)
            "Should calculate daily click rate (50/1000 * 100)")
    (assert (= (:weekly-click-rate result) 5.0)
            "Should calculate weekly click rate (350/7000 * 100)"))

  ;; Test different metrics
  (let [result (extract-metrics
                 {:user-id "USER-456"
                  :metrics {:engagement {:daily {:views 500 :clicks 100 :shares 20}
                                         :weekly {:views 3500 :clicks 700 :shares 140}}
                            :revenue {:daily {:amount 500.00 :transactions 50}
                                      :weekly {:amount 3500.00 :transactions 350}}}})]
    (assert (= (:daily-click-rate result) 20.0)
            "Should calculate 20% click rate")
    (assert (= (:total-daily-engagement result) 620)
            "Should calculate total (500+100+20)"))

  (println "✓ All tests passed!"))

;; Run: (-test)
