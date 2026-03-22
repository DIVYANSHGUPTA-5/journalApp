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

    // ✅ CREATE ENTRY (FINAL FIX 🔥)
    @Transactional
    public void saveEntry(JournalEntry journalEntry, String userName) {

        // 🔥 ALWAYS FETCH FRESH USER
        User freshUser = userService.findByUserName(userName);

        if (freshUser == null) {
            throw new RuntimeException("User not found");
        }

        journalEntry.setUserName(userName);
        journalEntry.setDate(LocalDateTime.now());

        if (journalEntry.getContent() != null) {
            Sentiment sentiment = sentimentAnalysisService
                    .analyzeSentiment(journalEntry.getContent());
            journalEntry.setSentiment(sentiment);
        }

        JournalEntry savedEntry = journalEntryRepository.save(journalEntry);

        // 🔥 LINK ONLY TO CORRECT USER
        freshUser.getJournalEntries().add(savedEntry);
        userService.saveUser(freshUser);
    }

    // ✅ UPDATE ENTRY
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

            Sentiment sentiment = sentimentAnalysisService
                    .analyzeSentiment(newEntry.getContent());

            existing.setSentiment(sentiment);
        }

        return journalEntryRepository.save(existing);
    }

    // ✅ GET ALL
    public List<JournalEntry> getAll() {
        return journalEntryRepository.findAll();
    }

    // ✅ FIND BY ID
    public Optional<JournalEntry> findById(ObjectId id) {
        return journalEntryRepository.findById(id);
    }

    // ✅ DELETE (CONSISTENT)
    @Transactional
    public boolean deleteById(ObjectId id, String userName) {

        User freshUser = userService.findByUserName(userName);

        if (freshUser == null) return false;

        Optional<JournalEntry> entry = journalEntryRepository.findById(id);

        if (entry.isPresent() && userName.equals(entry.get().getUserName())) {

            // remove from correct user only
            freshUser.getJournalEntries().removeIf(e -> e.getId().equals(id));
            userService.saveUser(freshUser);

            journalEntryRepository.deleteById(id);

            return true;
        }

        return false;
    }

    // ✅ QUERY
    public List<JournalEntry> findByUserName(String userName) {
        return journalEntryRepository.findByUserName(userName);
    }
}