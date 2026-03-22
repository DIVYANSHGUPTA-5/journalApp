package net.engineeringdigest.journalApp.service;

import net.engineeringdigest.journalApp.enums.Sentiment;
import org.springframework.stereotype.Service;

@Service
public class SentimentAnalysisService {

    public Sentiment analyzeSentiment(String text) {

        if (text == null || text.isEmpty()) {
            return Sentiment.ANXIOUS; // default fallback
        }

        text = text.toLowerCase();

        // HAPPY
        if (text.contains("happy") || text.contains("good") || text.contains("great") || text.contains("love")) {
            return Sentiment.HAPPY;
        }

        // SAD
        if (text.contains("sad") || text.contains("down") || text.contains("unhappy")) {
            return Sentiment.SAD;
        }

        // ANGRY
        if (text.contains("angry") || text.contains("mad") || text.contains("hate")) {
            return Sentiment.ANGRY;
        }

        // ANXIOUS
        if (text.contains("anxious") || text.contains("worried") || text.contains("stress")) {
            return Sentiment.ANXIOUS;
        }

        return Sentiment.ANXIOUS;
    }
}