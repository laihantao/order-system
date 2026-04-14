package com.order_system.order.dto;
import java.util.List;



public class CreateOrderRequestDTO {
    public int userId;

    public List<OrderItemRequest> items;

    public static class OrderItemRequest {
        public int foodId;
        public int quantity;
    }

    public String idempotencyKey; // for idempotency control
}