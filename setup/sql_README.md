# SQL Table Structure

## Food Table
    CREATE TABLE food (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price NUMERIC(6,2),
    category VARCHAR(50)
    );

## Orders
    CREATE TABLE orders (
        order_id VARCHAR(36) PRIMARY key not NULL,
        user_id INT NOT NULL,
        status VARCHAR(20) NOT NULL,
        total_price NUMERIC(10,2) DEFAULT 0,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        processing_at TIMESTAMP null,
        completed_at TIMESTAMP null,
        cancelled_at TIMESTAMP null,
        CONSTRAINT fk_user
            FOREIGN KEY (user_id)
            REFERENCES users(id)
    );

## Order Items
    CREATE TABLE order_items (
        id SERIAL PRIMARY KEY,
        order_id VARCHAR(36) NOT NULL,
        food_id INT NOT NULL,
        quantity INT NOT NULL CHECK (quantity > 0),
        price_at_purchase NUMERIC(6,2) NOT NULL,
        FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
        FOREIGN KEY (food_id) REFERENCES food(id)
    );

## Logging
    CREATE TABLE logging (
        log_id SERIAL PRIMARY key,
        order_id VARCHAR(36) not null,
        user_id INT NOT NULL,
        message VARCHAR(2000) NOT NULL,
        remark VARCHAR(1000) not NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT fk_user
            FOREIGN KEY (user_id)
            REFERENCES users(id)
    );

## Users Table
    CREATE TABLE users (
        id SERIAL PRIMARY KEY,
        name VARCHAR(100),
        email VARCHAR(100)
    );

### Insert dummy users

    insert into users (name, email) values
    ('HTLAI', 'laihantao26@gmail.com')

    INSERT INTO users (name, email)
    SELECT 
    'user' || i,
    'user' || i || '@mailinator.com'
    FROM generate_series(2, 5000) AS i;



# Demostrate high concurrent API call
### 1. JMeter call API (100 threads, 5 ramp-up sec, 5 loop)
    ~ Expected 500 rows 

### 2. Statistic of order_id being used, success and failed
    SELECT
        A.order_id,
        A.user_id,
        COUNT(DISTINCT A.order_id) AS success_count,
        COUNT(B.order_id) AS failed_count,
        COUNT(DISTINCT A.order_id) + COUNT(B.order_id) as total_count
    FROM orders A
    LEFT JOIN logging B
        ON B.order_id = A.order_id
    GROUP BY A.order_id, A.user_id
    order by failed_count desc