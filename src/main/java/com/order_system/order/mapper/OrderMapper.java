package com.order_system.order.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.order_system.order.model.Order;

@Mapper
public interface OrderMapper {
    Order insertOrder(Order order);
    Map<String, Object> selectOrderById(String orderId);
    List<Order> selectAllOrders();
}