(ns state-machine-transition)

(defn next-state
  [current-state event {:keys [payment-confirmed items-in-stock address-valid]}]
  (case current-state
    :pending    (cond
                  (and (= event :confirm)
                       payment-confirmed
                       items-in-stock) :confirmed
                  (= event :cancel)    :cancelled
                  :else                :invalid-transition)
    :confirmed  (cond
                  (and (= event :ship) address-valid) :shipped
                  (= event :cancel)                   :cancelled
                  :else                               :invalid-transition)
    :shipped    (if (= event :deliver)
                  :delivered
                  :invalid-transition)
    :invalid-transition))


(defn- tst []
  (assert (=
(next-state :pending :confirm {:payment-confirmed true :items-in-stock true :address-valid true})
:confirmed))

(assert (=
(next-state :pending :confirm {:payment-confirmed false :items-in-stock true :address-valid true})
:invalid-transition))

(assert (=
(next-state :confirmed :ship {:payment-confirmed true :items-in-stock true :address-valid true})
:shipped))

(assert (=
(next-state :shipped :cancel {:payment-confirmed true :items-in-stock true :address-valid true})
:invalid-transition))

  "SUCCESS")

(tst)
