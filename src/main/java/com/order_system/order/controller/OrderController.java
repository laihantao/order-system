package com.order_system.order.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.order_system.order.dto.CreateOrderRequestDTO;
import com.order_system.order.dto.OrderResponseDTO;
import com.order_system.order.service.OrderServices;


@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderServices orderServices;

    @PostMapping("/create")
    public OrderResponseDTO createOrder(@RequestBody CreateOrderRequestDTO request) {
        return orderServices.createOrder(request);
    }

    @GetMapping("/{orderId}")
    public OrderResponseDTO getOrder(@PathVariable UUID orderId) {
        return orderServices.getOrder(orderId);
    }

    @GetMapping("/test")
    public String test() {
        return "Order module is working!";
    }
}