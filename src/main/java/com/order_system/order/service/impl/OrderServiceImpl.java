package com.order_system.order.service.impl;

import java.util.UUID;

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
        order.setOrderId("TESTINGORDERID");
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
        response.orderId = "TESTINGORDERID";
        response.status = "CREATED";
        response.totalPrice = total;

        return response;
    }

    @Override
    public OrderResponseDTO getOrder(UUID orderId) {

        Order order = orderMapper.selectOrderById(orderId);

        if (order == null) {
            throw new RuntimeException("Order not found: " + orderId);
        }

        OrderResponseDTO response = new OrderResponseDTO();
        // response.setOrderId(order.getOrderId());
        // response.setStatus(order.getStatus());
        // response.setTotalPrice(order.getTotalPrice());

        return response;
    }
}