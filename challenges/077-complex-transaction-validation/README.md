# 077 - Complex Transaction Validation

**Level**: 16/18
**Type**: Pure Function
**Concepts**: Multi-layered validation, Error accumulation, Business rule validation, Data integrity

## Context

Financial transaction systems must validate transactions against multiple rules: amount limits, account status, balance availability, velocity checks (transaction frequency), geographic restrictions, and compliance requirements. A robust validator must check all rules and provide detailed error information.

## Objective

Implement a pure function that validates a transaction against multiple business rules and returns either success or a detailed list of validation errors.

## Specification

### Input

- `transaction` (map): Transaction to validate
  ```clojure
  {:transaction-id string
   :from-account-id string
   :to-account-id string
   :amount number
   :currency keyword
   :country-code string
   :transaction-count-24h number
   :account-status keyword (:active | :suspended | :frozen | :closed)
   :available-balance number
   :account-age-days number
   :is-international boolean
   :requires-2fa boolean
   :has-2fa-verified boolean}
  ```

### Output

- Success: `{:status :valid :transaction-id string}`
- Failure: `{:status :invalid :transaction-id string :errors [strings]}`

### Rules

**Validation Rules (all must pass):**

1. **Account Status**: Account must be :active
2. **Same Account**: from-account-id ≠ to-account-id
3. **Amount Positive**: amount > 0
4. **Amount Maximum**: amount ≤ 50000 (single transaction limit)
5. **Currency Support**: currency must be in #{:USD :EUR :GBP :BRL}
6. **Balance Sufficient**: amount ≤ available-balance
7. **New Account Limit**: If account-age-days < 30, amount ≤ 5000
8. **Velocity Check**: If transaction-count-24h > 10, reject
9. **High Velocity Warning**: If transaction-count-24h > 5 AND amount > 10000, reject
10. **Country Restrictions**: Restricted countries: #{:KP :IR :SY :CU}
11. **International 2FA**: If is-international AND requires-2fa, must have has-2fa-verified
12. **Large International**: If is-international AND amount > 10000, requires 2FA verification

## Examples

### Example 1: Valid Transaction
```clojure
(validate-transaction {:transaction-id "TX123"
                       :from-account-id "ACC001"
                       :to-account-id "ACC002"
                       :amount 5000
                       :currency :USD
                       :country-code "US"
                       :transaction-count-24h 2
                       :account-status :active
                       :available-balance 10000
                       :account-age-days 100
                       :is-international false
                       :requires-2fa false
                       :has-2fa-verified false})
;; => {:status :valid :transaction-id "TX123"}
```

### Example 2: Multiple Validation Errors
```clojure
(validate-transaction {:transaction-id "TX124"
                       :from-account-id "ACC001"
                       :to-account-id "ACC001"
                       :amount 60000
                       :currency :JPY
                       :country-code "US"
                       :transaction-count-24h 11
                       :account-status :suspended
                       :available-balance 50000
                       :account-age-days 15
                       :is-international false
                       :requires-2fa false
                       :has-2fa-verified false})
;; => {:status :invalid
;;     :transaction-id "TX124"
;;     :errors ["Account status must be active"
;;              "Cannot transfer to same account"
;;              "Amount exceeds single transaction limit of 50000"
;;              "Currency JPY is not supported"
;;              "New accounts (< 30 days) limited to 5000 per transaction"
;;              "Transaction velocity limit exceeded (11 in 24h)"]}
```

## Tips

- Build errors list incrementally with `cond->` or manual accumulation
- Check each rule independently to collect ALL errors
- Use helper functions for complex checks (velocity, international, etc.)
- Return early with success if no errors found
- Provide clear, actionable error messages

## Testing your solution

```bash
cd challenges/077-complex-transaction-validation/
clj -M solution.clj
```
