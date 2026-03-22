package net.engineeringdigest.journalApp.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherResponse {

    private Current current;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Current {

        private int temperature;

        private int feelslike;

        @JsonProperty("weather_descriptions")
        private List<String> weatherDescriptions;
    }
}