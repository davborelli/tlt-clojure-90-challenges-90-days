(ns internal-to-api 
  (:require
    [clojure.string :as str]))

(defn internal->api
  [internal-product]
  (let [{productId     :product-id
         productName   :name
         category      :category
         basePrice     :price
         currency      :currency
         stockLevel    :stock
         ware-id       :warehouse-id
         ware-location :warehouse-location} internal-product]
    {:productId productId
     :productDetails {:productName productName
                      :category (name category)
                      :pricing {:basePrice basePrice
                                :currency (str/upper-case (name currency))}}
     :inventory      {:stockLevel stockLevel
                      :warehouse {:id ware-id
                                  :location ware-location}}}))

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
