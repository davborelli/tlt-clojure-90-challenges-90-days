(ns validate-process-order)

(defn process-order
  [{:keys [customer-id product-id quantity total]}]
  (or
   (when (<= customer-id 0)                           {:status :error :message "Invalid customer ID"})
   (when (<= product-id 0)                            {:status :error :message "Invalid product ID"})
   (when (not (and (> quantity 0) (<= quantity 100))) {:status :error :message "Invalid quantity"})
   (when (< total 10)                                 {:status :error :message "Order total too low"})
   {:status :success :order-id (str "ORD-" customer-id "-" product-id)}))

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
