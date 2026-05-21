(ns build-nested-report)

(defn build-report
  [transaction-data]
  {:id (:transaction-id transaction-data)
   :financial {:amount (:amount transaction-data)
               :fee    (:fee transaction-data)
               :net    (- (:amount transaction-data) (:fee transaction-data))}
   :user {:name  (:user-name transaction-data)
          :email (:user-email transaction-data)}
   :merchant {:name     (:merchant-name transaction-data)
              :category (:merchant-category transaction-data)}})

(defn- tst []
  (assert (=
(build-report 
  {:transaction-id "TXN-123" 
   :amount 100.00 
   :fee 2.50 
   :user-name "John Doe" 
   :user-email "john@example.com" 
   :merchant-name "Coffee Shop" 
   :merchant-category "dining"})
{:id "TXN-123" 
 :financial {:amount 100.0 :fee 2.5 :net 97.5} 
 :user {:name "John Doe" :email "john@example.com"} 
 :merchant {:name "Coffee Shop" :category "dining"}}))

(assert (=
(build-report 
  {:transaction-id "TXN-456" 
   :amount 1500.00 
   :fee 45.00 
   :user-name "Jane Smith" 
   :user-email "jane@example.com" 
   :merchant-name "Electronics Store" 
   :merchant-category "retail"})
{:id "TXN-456" 
 :financial {:amount 1500.0 :fee 45.0 :net 1455.0} 
 :user {:name "Jane Smith" :email "jane@example.com"} 
 :merchant {:name "Electronics Store" :category "retail"}}))

  "SUCCESS")

(tst)
