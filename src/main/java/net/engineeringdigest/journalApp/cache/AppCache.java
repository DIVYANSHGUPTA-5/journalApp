package net.engineeringdigest.journalApp.cache;

import jakarta.annotation.PostConstruct;
import net.engineeringdigest.journalApp.entity.ConfigJournalAppEntity;
import net.engineeringdigest.journalApp.repository.ConfigJournalAppRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AppCache {

    public enum Keys {
        WEATHER_API
    }

    @Autowired
    private ConfigJournalAppRepository configJournalAppRepository;

    private Map<String, String> cacheMap;

    @PostConstruct
    public void init() {
        cacheMap = new HashMap<>();
        List<ConfigJournalAppEntity> all = configJournalAppRepository.findAll();

        for (ConfigJournalAppEntity entity : all) {
            cacheMap.put(entity.getKey(), entity.getValue());
        }
    }

    // ✅ PUBLIC METHOD (important)
    public String get(Keys key) {
        return cacheMap.get(key.toString());
    }
}