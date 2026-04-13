package com.order_system.order.mapper;

import java.util.UUID;

import org.apache.ibatis.annotations.Mapper;

import com.order_system.order.model.Order;

@Mapper
public interface OrderMapper {
    void insertOrder(Order order);
    Order selectOrderById(UUID orderId);
}