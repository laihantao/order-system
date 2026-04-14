package com.order_system.food.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.order_system.food.service.FoodServices;

@RestController
@RequestMapping("/food")
public class FoodController {
    
    @Autowired
    private FoodServices foodServices;

    @GetMapping("/getFoodById/{foodId}")
    public Map<String, Object> getFoodById(@PathVariable int foodId) {
        return foodServices.selectFoodById(foodId);
    }
}
