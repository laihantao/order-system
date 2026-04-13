package com.order_system.logging.mapper;

import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoggingMapper {
    void insertLogging(Map<String, Object> log);
}
