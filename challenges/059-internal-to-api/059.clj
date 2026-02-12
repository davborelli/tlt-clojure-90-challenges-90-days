(ns internal-to-api)

(defn internal->api
  [internal-product]
  )

(defn- tst []
  (assert (=
(internal->api
  {:product-id "PROD-123"
   :name "Laptop"
   :category :electronics
   :price 999.99
   :currency :usd
   :stock 50
   :warehouse-id "WH-001"
   :warehouse-location "New York"})
{:productId "PROD-123"
     :productDetails {:productName "Laptop"
                      :category "electronics"
                      :pricing {:basePrice 999.99 :currency "USD"}}
     :inventory {:stockLevel 50
                 :warehouse {:id "WH-001" :location "New York"}}}))

  "SUCCESS")

(tst)
