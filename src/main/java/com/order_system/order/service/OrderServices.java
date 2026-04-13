package com.order_system.order.service;

import java.util.UUID;

import com.order_system.order.dto.CreateOrderRequestDTO;
import com.order_system.order.dto.OrderResponseDTO;

public interface OrderServices {
    OrderResponseDTO createOrder(CreateOrderRequestDTO request);
    OrderResponseDTO getOrder(UUID orderId);
}