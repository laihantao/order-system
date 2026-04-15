# 🛒 High-Availability Order System (Redis Idempotency & Sentinel HA)

## 📌 Project Overview
This project demonstrates a production-grade backend architecture designed to solve high-concurrency challenges in order management. It specifically focuses on Race Condition Mitigation, Idempotency, and Infrastructure Resilience using a Java/Spring Boot core and a self-healing Redis cluster.

## 🧰 Tech StackBackend: 
- Java 17, Spring Boot
- MyBatis (Order/Logging Mappers)
- Database: PostgreSQL 15
- Caching & HA: Redis 7 (Master-Replica Configuration)
- Orchestration: Redis Sentinel (3-node Quorum)
- Environment: Docker & Docker Compose

## 🏗️ System Logic & Workflow
The system handles order creation with a focus on data integrity and external service integration.

### Core Order Logic (createOrder)
- Unique Identity: Generates a UUID based Order ID to ensure global uniqueness.
- External Integration: Communicates with foodServices to validate item existence and fetch real-time pricing.
- Fault Tolerance: If a specific food item is not found (404), the system gracefully skips the item rather than failing the entire transaction.
- Persistent Logging: Every failure is captured via loggingMapper into the database, including the Order ID and error details for production debugging.

## Implementation Strategies
| Approach | Mechanism | Client Experience |
|------|--------|--------|
| Standard (DB Only) | Direct INSERT to Postgres | Risk of duplicate orders and high DB load during spikes |
| Redis Optimized | SETNX Distributed Locking + Idempotency Keys | Guaranteed single execution; redundant requests receive cached results |


## 🛡️ Infrastructure: Redis Sentinel High Availability

To prevent the "Single Point of Failure" common in standard cache setups, this project implements Redis Sentinel.

#### Sentinel vs. Single Node

| Feature | Single Node Redis | Redis Sentinel (HA) |
|------|--------|--------|
| Failure Domain | Entire service outage | Isolated node degradation |
| Recovery | Manual intervention | Automatic promotion via Leader Election |
| Awareness | Blind to topology | Continuous distributed monitoring |

#### The Failover & Quorum Theory
A 3-node Sentinel deployment is used to reach a Quorum.
- **SDOWN**: One sentinel perceives the master is down.
- **ODOWN**: The Quorum (majority) agrees the master is down, triggering a Leader Election.
- **Promotion**: The Sentinel leader promotes the healthiest replica to Master, ensuring the Java app can continue processing orders with minimal downtime.

## 🔐 Idempotency Design
The system protects against "Double Tap" (duplicate) requests using a two-tier defense:

1. Redis Layer (The Shield):
- Lock Key: IDEMPOTENCY:LOCK:USERID:{userId}:{key}
- Result Cache: IDEMPOTENCY:RESULT:USERID:{userId}:{key}
- Result: Returns 409 Conflict if processing is in progress, or the cached OrderResponseDTO if already completed.

2. Database Layer (The Anchor):
- Uses a UNIQUE(user_id, idempotency_key) constraint.
- Result: Ensures that even if the Redis lock expires prematurely, the DB remains the final source of truth.

## 📦 Docker Setup Breakdown
The environment is orchestrated via docker-compose.yml:
- App: The Java order system.
- Postgres: Primary relational storage.
- Redis Master/Replicas: 1 Master and 2 Replicas to handle read/write splitting.
- Sentinels (x3): Three separate sentinel containers for resilient monitoring.
- Volumes: Persistent storage for Sentinel configurations to ensure state recovery across container restarts.

## 🧪 Testing Scenarios

#### Scenario 1: High Concurrency (No Redis)
- Load: 500 concurrent threads.
- Result: Race conditions detected. Duplicate database insert attempts lead to inconsistent order states and increased latency.

#### Scenario 2: High Concurrency (With Redis Sentinel)
- Load: 500 concurrent threads.
- Result: Redis locks successfully intercept duplicate requests. Database pressure is significantly reduced, and the system maintains a 100% idempotency rate.Failover Test: Manually stopping the Redis Master container results in an automatic failover; the system resumes order processing within seconds.


## 🚀 How to Run 

### Local
1. Clone the Repo

2. Clean and build target file
.\mvnw clean package -DskipTests

3. Docker compose
> docker-compose up --build

4. Create necessary table in your postgres, refer to ./setip/sql_README.md

5. Proceed API call for testing
> http://localhost:8080/orders/createWithRedis

Sample payload
```sql
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
  ],
  "idempotencyKey": "TEST_KEY_1"
}
```

### Deploy in EC2

#### 1. Repo environment variable

- EC_HOST
- EC2_USER
- EC2_SSH_KEY
- EC2_PROJECT_PATH

If you want to integrate with telegram bot as well:
- TELEGRAM_BOT_TOKEN
- TELEGRRAM_CHAT_ID

#### 2. 

