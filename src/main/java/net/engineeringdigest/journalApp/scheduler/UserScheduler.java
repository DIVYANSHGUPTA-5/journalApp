package net.engineeringdigest.journalApp.scheduler;

import net.engineeringdigest.journalApp.entity.JournalEntry;
import net.engineeringdigest.journalApp.entity.User;
import net.engineeringdigest.journalApp.enums.Sentiment;
import net.engineeringdigest.journalApp.service.EmailService;
import net.engineeringdigest.journalApp.service.JournalEntryService;
import net.engineeringdigest.journalApp.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class UserScheduler {

    @Autowired
    private EmailService emailService;

    @Autowired
    private JournalEntryService journalEntryService;

    @Autowired
    private UserService userService;

    //Runs every minute (for testing)
   // @Scheduled(cron = "0 * * * * *")

   @Scheduled(cron = "0 0 9 ? * SUN")// sunday
    public void sendWeeklySentimentEmail() {

        System.out.println("CRON IS RUNNING...");
        System.out.println("TIME: " + LocalDateTime.now());

        // ✅ CONFIG-BASED AUTOMATION
        Map<Sentiment, String> emailMessages = Map.of(
                Sentiment.SAD, "We noticed you're feeling low 💙 Take care ❤️",
                Sentiment.ANXIOUS, "Try to relax. You’re doing great 💛",
                Sentiment.HAPPY, "Keep shining 🌟"
        );

        List<User> users = userService.getUsersForSentimentAnalysis();

        System.out.println("USERS SIZE: " + users.size());

        for (User user : users) {

            List<JournalEntry> entries = journalEntryService.findByUserName(user.getUserName());

            System.out.println("ALL ENTRIES: " + entries);

            if (entries == null || entries.isEmpty()) {
                System.out.println("No entries for user: " + user.getUserName());
                continue;
            }

            // ✅ GET LATEST ENTRY (CURRENT FEELING)
            JournalEntry latestEntry = entries.stream()
                    .filter(entry -> entry.getDate() != null)
                    .max(Comparator.comparing(JournalEntry::getDate))
                    .orElse(null);

            if (latestEntry == null || latestEntry.getSentiment() == null) {
                System.out.println("No valid latest sentiment for user: " + user.getUserName());
                continue;
            }

            Sentiment currentSentiment = latestEntry.getSentiment();

            System.out.println("CURRENT SENTIMENT: " + currentSentiment);

            // ✅ AUTOMATED RULE BASED ON CURRENT FEELING
            if (emailMessages.containsKey(currentSentiment)) {

                String message = emailMessages.get(currentSentiment);

                emailService.sendEmail(
                        user.getEmail(),
                        "Your Current Mood Insight",
                        message
                );

                System.out.println("Email sent for CURRENT mood: " + currentSentiment + " → " + user.getEmail());
            }
        }
    }
}