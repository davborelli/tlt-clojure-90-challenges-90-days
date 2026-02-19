(ns validate-process-order)

(defn process-order
  [order-data]
  )

(defn- tst []
  (assert (=
(process-order {:customer-id 123 :product-id 456 :quantity 5 :total 99.99})
{:status :success :order-id "ORD-123-456"}))

(assert (=
(process-order {:customer-id -1 :product-id 456 :quantity 5 :total 99.99})
{:status :error :message "Invalid customer ID"}))

(assert (=
(process-order {:customer-id 123 :product-id 456 :quantity 150 :total 99.99})
{:status :error :message "Invalid quantity"}))

(assert (=
(process-order {:customer-id 123 :product-id 456 :quantity 1 :total 5.00})
{:status :error :message "Order total too low"}))

  "SUCCESS")

(tst)
