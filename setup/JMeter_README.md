
# Scenario 1 (Will have alot of duplicate key on order_id)
Endpoint: /orders/createOrder
Threads: 500
Ramp up sec: 5
Loop: 20

Order Id format: ORD_milisecondTimeStamp


```
Test 1: 6,585/10,000
Test 2: 6,569/10,000
Test 3: 6,576/10,000
Test 4: 6,634/10,000
Test 5: 6,617/10,000

Test 6: 6,677/10,000
Test 7: 6,591/10,000
Test 8: 6,543/10,000
Test 9: 6,573/10,000
Test 10: 6,538/10,000

Test 11: 5,876/10,000

```

Test 11 shown lower success rate, this is because of during the API testing, I tried to query data from orders table, it causing locking contention, result in slower performance.

> From testing 1 ~ 10, the average success rate is 65.90%, the rest of order is missing due to duplicate key.


# Scenario 2 - ORD_ID with UUID (No more happen duplicate key on order_id)
Endpoint: /orders/createOrder
Threads: 500
Ramp up sec: 5
Loop: 20

Order Id format: "ORD_" + UUID.randomUUID

```
Test 1: 10,000 /10,000
Test 2: 10,000 /10,000
Test 3: 10,000 /10,000
Test 4: 10,000 /10,000
Test 5: 10,000 /10,000
```

During this scenario, even though query data during the API spam, it didn't caused duplicate key for order_id.



Payload for testing
```
{
  "userId": ${__Random(1,5000,userId)},
  "items": [
    {
      "foodId": ${__Random(1,30,foodId1)},
      "quantity": ${__Random(1,10,qty1)}
    },
    {
      "foodId": ${__Random(1,30,foodId2)},
      "quantity": ${__Random(1,10,qty2)}
    }
  ]
}
```

```
{
  "userId": 1,
  "items": [
    {
      "foodId": 1,
      "quantity": 2
    },
    {
      "foodId": 2,
      "quantity": 7
    }
  ]
}
```