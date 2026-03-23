package net.engineeringdigest.journalApp.service;

import net.engineeringdigest.journalApp.entity.JournalEntry;
import net.engineeringdigest.journalApp.entity.User;
import net.engineeringdigest.journalApp.enums.Sentiment;
import net.engineeringdigest.journalApp.repository.JournalEntryRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class JournalEntryService {

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private SentimentAnalysisService sentimentAnalysisService;

    @Transactional
    public void saveEntry(JournalEntry journalEntry, String userName) {

        User freshUser = userService.findByUserName(userName);

        if (freshUser == null) {
            throw new RuntimeException("User not found");
        }

        journalEntry.setUserName(userName);
        journalEntry.setDate(LocalDateTime.now());

        // 🔥 SAFE SENTIMENT (NO NEUTRAL)
        try {
            if (journalEntry.getContent() != null && !journalEntry.getContent().isEmpty()) {
                Sentiment sentiment = sentimentAnalysisService
                        .analyzeSentiment(journalEntry.getContent());

                if (sentiment != null) {
                    journalEntry.setSentiment(sentiment);
                }
            }
        } catch (Exception e) {
            System.out.println("Sentiment failed: " + e.getMessage());
        }

        JournalEntry savedEntry = journalEntryRepository.save(journalEntry);

        freshUser.getJournalEntries().add(savedEntry);
        userService.saveUser(freshUser);
    }

    @Transactional
    public JournalEntry updateEntry(ObjectId id, JournalEntry newEntry, String userName) {

        JournalEntry existing = journalEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found"));

        if (!userName.equals(existing.getUserName())) {
            throw new RuntimeException("Unauthorized");
        }

        if (newEntry.getTitle() != null && !newEntry.getTitle().isEmpty()) {
            existing.setTitle(newEntry.getTitle());
        }

        if (newEntry.getContent() != null && !newEntry.getContent().isEmpty()) {
            existing.setContent(newEntry.getContent());

            try {
                Sentiment sentiment = sentimentAnalysisService
                        .analyzeSentiment(newEntry.getContent());

                if (sentiment != null) {
                    existing.setSentiment(sentiment);
                }
            } catch (Exception e) {
                System.out.println("Sentiment failed");
            }
        }

        return journalEntryRepository.save(existing);
    }

    public List<JournalEntry> getAll() {
        return journalEntryRepository.findAll();
    }

    public Optional<JournalEntry> findById(ObjectId id) {
        return journalEntryRepository.findById(id);
    }

    @Transactional
    public boolean deleteById(ObjectId id, String userName) {

        User freshUser = userService.findByUserName(userName);

        if (freshUser == null) return false;

        Optional<JournalEntry> entry = journalEntryRepository.findById(id);

        if (entry.isPresent() && userName.equals(entry.get().getUserName())) {

            freshUser.getJournalEntries().removeIf(e -> e.getId().equals(id));
            userService.saveUser(freshUser);

            journalEntryRepository.deleteById(id);

            return true;
        }

        return false;
    }

    public List<JournalEntry> findByUserName(String userName) {
        return journalEntryRepository.findByUserName(userName);
    }
}