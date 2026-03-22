package net.engineeringdigest.journalApp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RedisService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public <T> T get(String key, Class<T> entityClass) {
        try {
            String value = redisTemplate.opsForValue().get(key);

            if (value == null) {
                return null;
            }

            return objectMapper.readValue(value, entityClass);

        } catch (Exception e) {
            log.error("Redis GET Exception", e);
            return null;
        }
    }

    public void set(String key, Object value, Long ttl) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);

            redisTemplate.opsForValue()
                    .set(key, jsonValue, ttl, TimeUnit.SECONDS);

        } catch (Exception e) {
            log.error("Redis SET Exception", e);
        }
    }
}