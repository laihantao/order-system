package com.order_system.order.service;

import java.util.List;
import java.util.Map;

import com.order_system.order.dto.CreateOrderRequestDTO;
import com.order_system.order.dto.OrderResponseDTO;

public interface OrderServices {
    OrderResponseDTO createOrder(CreateOrderRequestDTO request);
    Map<String, Object> getOrderById(String orderId);
    List <OrderResponseDTO> getAllOrders();
}