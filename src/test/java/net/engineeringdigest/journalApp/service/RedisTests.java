package net.engineeringdigest.journalApp.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("External service - skipping for now")
public class RedisTests {

    @MockBean
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void testRedisOperations() {
        // Mock ValueOperations
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Mock behavior
        doNothing().when(valueOperations).set("email", "gmail@email.com");
        when(valueOperations.get("email")).thenReturn("gmail@email.com");

        // Perform operations
        redisTemplate.opsForValue().set("email", "gmail@email.com");
        String result = redisTemplate.opsForValue().get("email");

        // Verify
        assert result.equals("gmail@email.com");
    }
}