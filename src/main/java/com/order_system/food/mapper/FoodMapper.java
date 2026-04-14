package com.order_system.food.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FoodMapper {
    
    List<Map<String, Object>> selectAllFood();

    Map<String, Object> selectFoodById(int foodId);
}
