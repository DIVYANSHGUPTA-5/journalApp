package net.engineeringdigest.journalApp.service;

import net.engineeringdigest.journalApp.api.response.WeatherResponse;
import net.engineeringdigest.journalApp.cache.AppCache;
import net.engineeringdigest.journalApp.constants.Placeholders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

    @Value("${weather.api.key}")
    private String apiKey;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AppCache appCache;

    @Autowired
    private RedisService redisService;

    public WeatherResponse getWeather(String city) {

        String redisKey = "weather_of_" + city;

        // 1️⃣ Check Redis Cache First
        WeatherResponse cachedResponse =
                redisService.get(redisKey, WeatherResponse.class);

        if (cachedResponse != null
                && cachedResponse.getCurrent() != null) {

            System.out.println("Returning weather from Redis cache");
            return cachedResponse;
        }

        // 2️⃣ Get API template from Mongo cache
        String apiTemplate = appCache.get(AppCache.Keys.WEATHER_API);

        if (apiTemplate == null || apiTemplate.isBlank()) {
            throw new RuntimeException("WEATHER_API config missing in DB");
        }

        // 3️⃣ Build final API URL
        String finalAPI = apiTemplate
                .replace(Placeholders.CITY, city)
                .replace(Placeholders.API_KEY, apiKey);

        if (!finalAPI.startsWith("http")) {
            throw new RuntimeException("Generated API URL invalid: " + finalAPI);
        }

        try {
            // 4️⃣ Call Weather API
            ResponseEntity<WeatherResponse> response =
                    restTemplate.exchange(
                            finalAPI,
                            HttpMethod.GET,
                            null,
                            WeatherResponse.class
                    );

            WeatherResponse body = response.getBody();

            if (body == null) {
                System.out.println("Weather API returned null body");
                return null;
            }

            if (body.getCurrent() == null) {
                System.out.println("Weather API returned no 'current' object");
                return null;
            }

            // 5️⃣ Save only valid response in Redis (5 minutes)
            redisService.set(redisKey, body, 300L);

            return body;

        } catch (Exception e) {
            System.out.println("Weather API call failed: " + e.getMessage());
            return null;
        }
    }
}