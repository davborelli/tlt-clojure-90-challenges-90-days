(ns order-fulfillment)

(defn validate-order
  [{:keys [order-id customer-id items shipping-address] :as order}]
  (or
   (when (empty? order-id)         {:status :error :message "Order must have order-id"})
   (when (empty? customer-id)      {:status :error :message "Order must have customer-id"})
   (when (empty? items)            {:status :error :message "Order must have items"})
   (when (empty? shipping-address) {:status :error :message "Order must have shipping-address"})
   (assoc order :validated true)))

(defn check-inventory
  [order]
  (assoc order :inventory-reserved true))

(defn calculate-subtotal
  [{:keys [items] :as order}]
  (let [subtotal (reduce #(+ % (* (:quantity %2) (:unit-price %2))) 0.0 items)]
    (assoc order :subtotal subtotal)))

(defn calculate-shipping
  [{:keys [subtotal] :as order}]
  (let [shipping-cost (if (< subtotal 100) 10.0 0.0)]
    (assoc order :shipping-cost shipping-cost)))

(defn calculate-total
  [{:keys [subtotal shipping-cost] :as order}]
  (assoc order :total (+ subtotal shipping-cost)))

(defn generate-invoice
  [{:keys [order-id] :as order}]
  (assoc order :invoice-id (str "INV-" order-id)))

(defn mark-fulfilled
  [order]
  (assoc order :status :fulfilled))

(defn fulfill-order
  [order-request]
  (let [validated-order (validate-order order-request)]
    (if (= (:status validated-order) :error)
      validated-order
      (-> validated-order
          (check-inventory)
          (calculate-subtotal)
          (calculate-shipping)
          (calculate-total)
          (generate-invoice)
          (mark-fulfilled)))))

(defn- tst []
  (assert (=
(fulfill-order
  {:order-id "ORD-001"
   :customer-id "CUST-123"
   :items [{:product-id "PROD-A" :quantity 2 :unit-price 25.00}
           {:product-id "PROD-B" :quantity 1 :unit-price 60.00}]
   :shipping-address {:street "123 Main St" :city "NYC" :zip "10001"}})
{:order-id "ORD-001"
     :customer-id "CUST-123"
     :items [{:product-id "PROD-A" :quantity 2 :unit-price 25.00}
             {:product-id "PROD-B" :quantity 1 :unit-price 60.00}]
     :shipping-address {:street "123 Main St" :city "NYC" :zip "10001"}
     :validated true
     :inventory-reserved true
     :subtotal 110.0
     :shipping-cost 0.0
     :total 110.0
     :invoice-id "INV-ORD-001"
     :status :fulfilled}))

(assert (=
(fulfill-order
  {:order-id "ORD-002"
   :customer-id "CUST-456"
   :items []
   :shipping-address {:street "456 Oak Ave"}})
{:status :error :message "Order must have items"}))

  "SUCCESS")

(tst)
