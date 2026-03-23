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

    // every 5 minutes
   // @Scheduled(cron = "0 */1 * * * *")
   // @Scheduled(cron = "0 0/30 * * * *")

    @Scheduled(cron = "0 0 9 ? * SUN")
    public void sendWeeklySentimentEmail() {

        System.out.println("CRON IS RUNNING...");
        System.out.println("TIME: " + LocalDateTime.now());

        Map<Sentiment, String> emailMessages = Map.of(
                Sentiment.SAD, "We noticed you're feeling low 💙 Take care ❤️",
                Sentiment.ANXIOUS, "Try to relax. You’re doing great 💛",
                Sentiment.HAPPY, "Keep shining 🌟"
        );

        List<User> users = userService.getUsersForSentimentAnalysis();

        System.out.println("USERS SIZE: " + users.size());

        for (User user : users) {

            List<JournalEntry> entries =
                    journalEntryService.findByUserName(user.getUserName());

            System.out.println("USER: " + user.getUserName());
            System.out.println("ALL ENTRIES: " + entries);

            if (entries == null || entries.isEmpty()) {
                System.out.println("No entries for user: " + user.getUserName());
                continue;
            }

            // 🔥🔥🔥 FIX: STRICT SORT (LATEST FIRST)
            entries = entries.stream()
                    .filter(e -> e.getDate() != null)
                    .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                    .toList();

            // 🔥 ALWAYS PICK LATEST (INDEX 0)
            JournalEntry latestEntry = entries.get(0);

            System.out.println("LATEST ENTRY: " + latestEntry.getContent());
            System.out.println("LATEST DATE: " + latestEntry.getDate());

            if (latestEntry.getSentiment() == null) {
                System.out.println("No sentiment for user: " + user.getUserName());
                continue;
            }

            Sentiment currentSentiment = latestEntry.getSentiment();

            System.out.println("CURRENT SENTIMENT: " + currentSentiment);

            if (emailMessages.containsKey(currentSentiment)) {

                String message = emailMessages.get(currentSentiment);

                System.out.println("EMAIL METHOD CALLED TO: " + user.getEmail());

                emailService.sendEmail(
                        user.getEmail(),
                        "Your Current Mood Insight",
                        message
                );

                System.out.println("Email sent for CURRENT mood: "
                        + currentSentiment + " → " + user.getEmail());
            }
        }
    }
}