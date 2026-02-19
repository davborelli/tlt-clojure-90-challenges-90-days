(ns api-to-internal)

(defn api->internal
  [api-response]
  )

(defn- tst []
  (assert (=
(api->internal
  {:productId "PROD-123"
   :productDetails {:productName "Laptop"
                    :category "electronics"
                    :pricing {:basePrice 999.99 :currency "USD"}}
   :inventory {:stockLevel 50
               :warehouse {:id "WH-001" :location "New York"}}
   :metadata {:timestamp "2024-01-15" :version 2}})
{:product-id "PROD-123"
     :name "Laptop"
     :category :electronics
     :price 999.99
     :currency :usd
     :stock 50
     :warehouse-id "WH-001"
     :warehouse-location "New York"}))

  "SUCCESS")

(tst)
