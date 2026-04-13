package com.order_system.order.service.impl;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.order_system.order.dto.CreateOrderRequestDTO;
import com.order_system.order.dto.OrderResponseDTO;
import com.order_system.order.mapper.OrderMapper;
import com.order_system.order.model.Order;
import com.order_system.order.service.OrderServices;



@Service
public class OrderServiceImpl implements OrderServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public OrderResponseDTO createOrder(CreateOrderRequestDTO request) {

        LOGGER.debug("Creating order for user: {}", request.userId);

        // 1. create order (CREATED)
        Order order = new Order();
        String order_id = "ORDER" + System.currentTimeMillis(); // simplified order ID generation
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