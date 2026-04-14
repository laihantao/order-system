package com.order_system.food.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.order_system.food.mapper.FoodMapper;
import com.order_system.food.service.FoodServices;

@Service
public class FoodServiceImpl implements FoodServices {
    
    @Autowired
    private FoodMapper foodMapper;

    @Override
    public List<Map<String, Object>> selectAllFood() {
        return foodMapper.selectAllFood();
    }

    @Override
    public Map<String, Object> selectFoodById(int foodId) {

        Map<String, Object> response = new HashMap<String, Object>();

        Map<String, Object> food = foodMapper.selectFoodById(foodId);

        if (food == null || food.get("id") == null) {
            response.put("status", 404);
            response.put("data", null);
            response.put("message", "Food not found");
            return response;
        }

        response.put("status", 200);
        response.put("data", food);
        response.put("message", null);

        return response;
    }
}
