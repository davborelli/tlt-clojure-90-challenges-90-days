(ns feature-flag-workflow)

(defn apply-if [req flag f] (if flag (f req) req))

(defn basic-validation
  [request]
  (assoc request :validation-result "basic"))

(defn enhanced-validation
  [request]
  (assoc request :validation-result "enhanced"))

(defn standard-processing
  [request]
  (assoc request :processing-result "standard"))

(defn premium-processing
  [request]
  (assoc request :processing-result "premium"))

(defn track-analytics
  [request]
  (assoc request :tracked true))

(defn mark-completed
  [request]
  (assoc request :status :completed))

(defn process-request
  [request]
  (-> request
      (basic-validation)
      (standard-processing)
      (cond->
       (get-in request [:features :enhanced-validation]) (enhanced-validation)
       (get-in request [:features :premium-processing])  (premium-processing)
       (get-in request [:features :analytics-tracking])  (track-analytics)) 
      (mark-completed)))
  

(defn- tst []
  (assert (=
(process-request
  {:user-id "U123"
   :action "submit"
   :data {:amount 100}
   :features {:enhanced-validation false
              :premium-processing false
              :analytics-tracking false}})
{:user-id "U123"
     :action "submit"
     :data {:amount 100}
     :features {:enhanced-validation false
                :premium-processing false
                :analytics-tracking false}
     :validation-result "basic"
     :processing-result "standard"
     :status :completed}))

(assert (=
(process-request
  {:user-id "U456"
   :action "submit"
   :data {:amount 500}
   :features {:enhanced-validation true
              :premium-processing true
              :analytics-tracking true}})
{:user-id "U456"
     :action "submit"
     :data {:amount 500}
     :features {:enhanced-validation true
                :premium-processing true
                :analytics-tracking true}
     :validation-result "enhanced"
     :processing-result "premium"
     :tracked true
     :status :completed}))

  "SUCCESS")

(tst)
