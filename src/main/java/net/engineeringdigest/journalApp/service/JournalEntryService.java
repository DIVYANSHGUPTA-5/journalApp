package net.engineeringdigest.journalApp.service;

import net.engineeringdigest.journalApp.entity.JournalEntry;
import net.engineeringdigest.journalApp.entity.User;
import net.engineeringdigest.journalApp.enums.Sentiment;
import net.engineeringdigest.journalApp.repository.JournalEntryRepository;

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

    // ✅ CREATE ENTRY
    @Transactional
    public JournalEntry saveEntry(JournalEntry journalEntry, String userName) {

        User user = userService.findByUserName(userName);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        journalEntry.setUserName(userName);
        journalEntry.setDate(LocalDateTime.now());

        if (journalEntry.getContent() != null) {
            Sentiment sentiment = sentimentAnalysisService
                    .analyzeSentiment(journalEntry.getContent());
            journalEntry.setSentiment(sentiment);
        }

        JournalEntry saved = journalEntryRepository.save(journalEntry);

        user.getJournalEntries().add(saved);
        userService.saveUser(user);

        return saved;
    }

    // ✅ FIND BY USERNAME
    public List<JournalEntry> findByUserName(String userName) {
        return journalEntryRepository.findByUserName(userName);
    }

    // ✅ SAFE GET BY ID
    public Optional<JournalEntry> getEntryByIdForUser(String id, String userName) {
        Optional<JournalEntry> entry = journalEntryRepository.findById(id);

        if (entry.isPresent() && userName.equals(entry.get().getUserName())) {
            return entry;
        }
        return Optional.empty();
    }

    // ✅ UPDATE
    @Transactional
    public JournalEntry updateEntry(String id, JournalEntry newEntry, String userName) {

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

            Sentiment sentiment = sentimentAnalysisService
                    .analyzeSentiment(newEntry.getContent());

            existing.setSentiment(sentiment);
        }

        return journalEntryRepository.save(existing);
    }

    // ✅ DELETE
    @Transactional
    public boolean deleteById(String id, String userName) {

        Optional<JournalEntry> entry = journalEntryRepository.findById(id);

        if (entry.isPresent() && userName.equals(entry.get().getUserName())) {

            User user = userService.findByUserName(userName);

            if (user != null) {
                user.getJournalEntries().removeIf(e -> e.getId().equals(id));
                userService.saveUser(user);
            }

            journalEntryRepository.deleteById(id);
            return true;
        }

        return false;
    }
}