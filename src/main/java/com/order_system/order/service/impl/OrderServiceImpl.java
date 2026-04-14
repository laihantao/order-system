package com.order_system.order.service.impl;


import static java.lang.Thread.sleep;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.order_system.food.service.FoodServices;
import com.order_system.order.dto.CreateOrderRequestDTO;
import com.order_system.order.dto.OrderResponseDTO;
import com.order_system.order.mapper.OrderMapper;
import com.order_system.order.model.Order;
import com.order_system.order.service.OrderServices;
import com.order_system.logging.mapper.LoggingMapper;

@Service
public class OrderServiceImpl implements OrderServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private LoggingMapper loggingMapper;

    @Autowired
    private FoodServices foodServices;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public OrderResponseDTO createOrder(CreateOrderRequestDTO request) {

        // String order_id = "ORD" + "_" + System.currentTimeMillis(); // simplified order ID generation
        String order_id = "ORD" + "_" + String.valueOf(UUID.randomUUID()); // simplified order ID generation

        try {
            LOGGER.debug("Creating order for user: {}", request.userId);

            // 1. create order (CREATED)
            Order order = new Order();
            order.setOrderId(order_id);
            order.setUserId(request.userId);
            order.setStatus("CREATED");

            // 2. calculate total price (simplified)
            double total = 0;

            for (CreateOrderRequestDTO.OrderItemRequest item : request.items) {

                if (item.foodId > 0) {
                    // total += item.quantity * 10;
                    Map<String, Object> foodResponse = foodServices.selectFoodById(item.foodId);
                    
                    if (String.valueOf(foodResponse.get("status")).equals("404")) {
                        continue; // skip this item and continue with the next one
                    }

                    Map<String, Object> foodData = (Map<String, Object>) foodResponse.get("data");
                    double price = Double.parseDouble(String.valueOf(foodData.get("price")));
                    total += item.quantity * price;
                }
                
            }

            order.setTotalPrice(total);

            orderMapper.insertOrder(order);

            OrderResponseDTO response = new OrderResponseDTO();
            response.orderId = order.getOrderId();
            response.status = "CREATED";
            response.totalPrice = total;

            return response;
        }
        catch (Exception e) {
            LOGGER.error("Error creating order: {}", e.getMessage());

            Map<String, Object> logging = new HashMap<>();
            logging.put("order_id", order_id);
            logging.put("user_id", request.userId);
            logging.put("message", "Failed to create order: " + e.getMessage());
            logging.put("remark", "Error occurred while creating order");

            loggingMapper.insertLogging(logging);

            throw new RuntimeException("Failed to create order: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> createOrderRedis(CreateOrderRequestDTO request) {

        Map<String, Object> logging = new HashMap<>();

        String order_id = "ORD" + "_" + String.valueOf(UUID.randomUUID()); // simplified order ID generation

        String key = "IDEMPOTENCY:LOCK:USERID:" + request.userId + ":" + request.idempotencyKey;        
        String resultKey = "IDEMPOTENCY:RESULT:USERID:" + request.userId + ":" + request.idempotencyKey; 

        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "PROCESSING", Duration.ofMinutes(5));
        
        // To test Redis lock expiration, we set it to 5 seconds here. In production, it should be around 5 minutes or more depending on the expected processing time.
        // Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "PROCESSING", Duration.ofSeconds(5));

        // try {
        //     sleep(10000); // simulate processing time
        // } catch (InterruptedException ex) {
        //     System.getLogger(OrderServiceImpl.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        // }

        if (Boolean.FALSE.equals(success)) {

            String existingValue = redisTemplate.opsForValue().get(key);

            if ("PROCESSING".equals(existingValue)) {
                return Map.of(
                    "status", 409,
                    "message", "Request is being processed"
                );
            }

            try {
                String cachedValue = redisTemplate.opsForValue().get(resultKey);

                OrderResponseDTO cachedResponse =
                    objectMapper.readValue(cachedValue, OrderResponseDTO.class);

                return Map.of(
                    "status", 200,
                    "data", cachedResponse,
                    "message", "Order already created"
                );

            } catch (Exception e) {
                return Map.of(
                    "status", 500,
                    "data", existingValue,
                    "message", "Failed to parse cached response" + e.getMessage()
                );
            }
        }

        try {
            LOGGER.debug("Creating order for user: {}", request.userId);

            // 1. create order (CREATED)
            Order order = new Order();
            order.setOrderId(order_id);
            order.setUserId(request.userId);
            order.setStatus("CREATED");
            order.setIdempotencyKey(request.idempotencyKey);

            // 2. calculate total price (simplified)
            double total = 0;

            for (CreateOrderRequestDTO.OrderItemRequest item : request.items) {

                if (item.foodId > 0) {
                    // total += item.quantity * 10;
                    Map<String, Object> foodResponse = foodServices.selectFoodById(item.foodId);
                    
                    if ("404".equals(String.valueOf(foodResponse.get("status")))) {
                        continue; // skip this item and continue with the next one
                    }

                    Map<String, Object> foodData = (Map<String, Object>) foodResponse.get("data");
                    double price = Double.parseDouble(String.valueOf(foodData.get("price")));
                    total += item.quantity * price;
                }
                
            }

            order.setTotalPrice(total);

            orderMapper.insertOrder(order);

            OrderResponseDTO response = new OrderResponseDTO();
            response.orderId = order.getOrderId();
            response.status = "CREATED";
            response.totalPrice = total;

            // 1. Convert response to JSON string (using Jackson/Gson)
            String jsonResponse = objectMapper.writeValueAsString(response);

            // 2. Update Redis key from "1" to the actual result
            // We increase TTL to 24 hours so they get the same result all day
            redisTemplate.opsForValue().set(key, "COMPLETED", Duration.ofMinutes(5));
            redisTemplate.opsForValue().set(resultKey, jsonResponse, Duration.ofHours(3));

            return Map.of(
                "status", 200,
                "data", response,
                "message", "Order created successfully for user: " + request.userId
            );
        }
        catch (Exception e) {
            LOGGER.error("Error creating order: {}", e.getMessage());

            redisTemplate.delete(key);

            logging.put("order_id", order_id);
            logging.put("user_id", request.userId);
            logging.put("message", "Failed to create order: " + e.getMessage());
            logging.put("remark", "Error occurred while creating order");

            loggingMapper.insertLogging(logging);

            throw new RuntimeException("Failed to create order: " + e.getMessage(), e);
            // return Map.of(
            //     "status", 500,
            //     "message", "Failed to create order: " + e.getMessage()
            // );
        }
    }

    @Override
    public OrderResponseDTO createOrderKafkaRedis(CreateOrderRequestDTO request) {

        LOGGER.debug("Creating order with Kafka and Redis for user: {}", request.userId);

        // 1. create order (CREATED)
        Order order = new Order();
        String order_id = "ORD" + "_" + System.currentTimeMillis(); // simplified order ID generation
        order.setOrderId(order_id);
        order.setUserId(request.userId);
        order.setStatus("CREATED");

        // 2. calculate total price (simplified)
        double total = 0;

        for (CreateOrderRequestDTO.OrderItemRequest item : request.items) {
            total += item.quantity * 10; // assume food price = 10 (demo)
        }

        order.setTotalPrice(total);

        orderMapper.insertOrder(order);

        OrderResponseDTO response = new OrderResponseDTO();
        response.orderId = order.getOrderId();
        response.status = "CREATED";
        response.totalPrice = total;

        return response;
    }

    @Override
    public Map<String, Object> getOrderById(String orderId) {

        System.out.println("Fetching order by ID: " + orderId);

        Map<String, Object> response = new HashMap<String, Object>();

        Map<String, Object> order = orderMapper.selectOrderById(orderId);

        if (order == null || order.get("order_id") == null) {
            response.put("error", "Order not found");
            return response;
        }

        System.out.println(order.get("orderId"));
        System.out.println(order.get("status"));

        // OrderResponseDTO response = new OrderResponseDTO();
        response.put("orderId", order.get("order_id"));
        response.put("status", order.get("status"));
        response.put("totalPrice", order.get("total_price"));

        return response;
    }

    @Override
    public List<OrderResponseDTO> getAllOrders() {

        List<Order> order = orderMapper.selectAllOrders();

        LOGGER.debug("Fetching all orders: {}", order.size());

        System.out.println("Fetched orders: " + order.size());

        if (order == null || order.isEmpty()) {
            throw new RuntimeException("No orders found");
        }

        List<OrderResponseDTO> responseList = new ArrayList<>();

        for (Order o : order) {
            System.out.println("Processing order: " + o.getOrderId());
            OrderResponseDTO response = new OrderResponseDTO();
            response.setOrderId(o.getOrderId());
            response.setStatus(o.getStatus());
            response.setTotalPrice(o.getTotalPrice());
            responseList.add(response);
        }

        return responseList;
    }
}