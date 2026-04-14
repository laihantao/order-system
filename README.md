# 🛒 Order System (Race Condition & Redis Idempotency Demo)

## 📌 Project Overview

This project demonstrates a real-world backend concurrency problem: **race conditions in order creation**, and how they can be mitigated using:

- Traditional database approach
- Redis-based idempotency + distributed lock

It simulates high-concurrency order creation scenarios and compares system behavior with and without Redis.

## 🧰 Prerequisites setup
- Java 17
- Spring Boot
- Redis
- PostgreSQL
- Maven
- Docker




## 🏗️ System Design

The project contains two main implementations:

### 1. Standard Order Creation (Without Redis)

- Directly inserts order into database
- No protection against duplicate requests
- Race condition may occur under high concurrency

### Flow:
Client → API → Calculate Total → DB Insert → Response


### Issue:
- Duplicate requests may create duplicate orders (if order_id is not unique-safe)
- High DB load under concurrent traffic


---
### 2. Order Creation with Redis Idempotency

This version introduces Redis to handle concurrency and duplicate requests.

#### Key Concepts:
- Distributed lock using Redis (`SETNX`)
- Idempotency key per user request
- Cached result storage

### Flow:
```
Client → API → Redis Lock Check
↓
(If locked → return 409 or cached result)
↓
Process Order (DB Insert)
↓
Store Result in Redis Cache
```




## 🔐 Redis Idempotency Design

### Keys Used:
```
LOCK KEY:
IDEMPOTENCY:LOCK:USERID:{userId}:{idempotencyKey}

RESULT KEY:
IDEMPOTENCY:RESULT:USERID:{userId}:{idempotencyKey}
```


---

### State Flow:

| State | Meaning |
|------|--------|
| PROCESSING | Request is currently being processed |
| COMPLETED | Order has been successfully created |
| RESULT CACHE | Stores final API response |

---

### Behavior:

#### 🟡 First Request:
- Redis lock acquired (`PROCESSING`)
- Order is created in DB
- Result stored in Redis
- Lock marked as `COMPLETED`

---

#### 🔴 Duplicate Request (during processing):
- Returns `409 - Request is being processed`

---

#### 🟢 Duplicate Request (after completion):
- Returns cached response from Redis



## ⚠️ Important Limitation

Even with Redis, there is still a small edge case:

> If Redis key expires before processing completes, duplicate execution may occur.

### Solution:
To guarantee consistency, a database-level constraint is used:

```sql
UNIQUE(user_id, idempotency_key)
```

This ensures:
- No duplicate order even if Redis fails
- DB acts as final source of truth

## 🧠 Key Concepts Demonstrated

1. Race Condition Problem
- Without synchronization, multiple threads can insert duplicate orders.

2. Redis Distributed Lock
- Prevents multiple threads from processing same request
- Reduces unnecessary DB load

3. Idempotency Design
- Ensure Same Request → Same result (no duplicate orders)

4. Database Consistency
- Redis is optimization layer, but DB ensures final correctness.


## 📦 Order Creation Logic

Main Steps:
- Generate Order ID
- Validate request items
- Fetch food details from service
- Calculate total price
- Insert order into database
- Store result in Redis (if Redis version)


## 🧪 Testing Scenarios
### Scenario 1: Without Redis
High concurrency requests (500 threads)

Result:
- Race conditions occur
- Duplicate insert attempts
- Lower success rate under load

### Scenario 2: With Redis
High concurrency requests (500 threads)

Result:
- Duplicate requests blocked or cached
- Improved consistency
- Reduced DB pressure