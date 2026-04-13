package com.order_system.order.controller;

import java.util.List;
import java.util.Map;

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

    @PostMapping("/createWithRedis")
    public OrderResponseDTO createOrderRedis(@RequestBody CreateOrderRequestDTO request) {
        return orderServices.createOrderRedis(request);
    }

    @PostMapping("/createWithKafkaRedis")
    public OrderResponseDTO createOrderKafkaRedis(@RequestBody CreateOrderRequestDTO request) {
        return orderServices.createOrderKafkaRedis(request);
    }

    @GetMapping("/getOrderById/{orderId}")
    public Map<String, Object> getOrderById(@PathVariable String orderId) {
        return orderServices.getOrderById(orderId);
    }

    @GetMapping("/getAllOrders")
    public List<OrderResponseDTO> getAllOrders() {
        return orderServices.getAllOrders();
    }

    @GetMapping("/test")
    public String test() {
        return "Order module is working!";
    }
}