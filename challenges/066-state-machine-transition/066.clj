(ns state-machine-transition)

(defn next-state
  [current-state event context]
  )

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
