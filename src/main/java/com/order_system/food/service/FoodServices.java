package com.order_system.food.service;

import java.util.List;
import java.util.Map;

public interface FoodServices {

    List<Map<String, Object>> selectAllFood();

    Map<String, Object> selectFoodById(int foodId);

}
