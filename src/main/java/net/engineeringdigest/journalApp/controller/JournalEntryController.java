package net.engineeringdigest.journalApp.controller;

import net.engineeringdigest.journalApp.entity.JournalEntry;
import net.engineeringdigest.journalApp.service.JournalEntryService;

import org.bson.types.ObjectId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/journal")
public class JournalEntryController {

    @Autowired
    private JournalEntryService journalEntryService;

    // ✅ TEST
    @GetMapping("/ping")
    public String ping() {
        return "OK";
    }

    // ✅ GET ALL USER ENTRIES
    @GetMapping
    public ResponseEntity<?> getALLJournalEntriesOfUser() {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        List<JournalEntry> entries = journalEntryService.findByUserName(userName);

        if (!entries.isEmpty()) {
            return new ResponseEntity<>(entries, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // ✅ CREATE ENTRY
    @PostMapping
    public ResponseEntity<?> createEntry(@RequestBody JournalEntry myEntry) {

        try {
            String userName = SecurityContextHolder.getContext().getAuthentication().getName();

            journalEntryService.saveEntry(myEntry, userName);

            return new ResponseEntity<>(myEntry, HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>("Error creating entry", HttpStatus.BAD_REQUEST);
        }
    }

    // ✅ GET BY ID
    @GetMapping("/id/{myId}")
    public ResponseEntity<?> getJournalEntryById(@PathVariable ObjectId myId) {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<JournalEntry> entry = journalEntryService.findById(myId);

        if (entry.isPresent() && userName.equals(entry.get().getUserName())) {
            return new ResponseEntity<>(entry.get(), HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // ✅ DELETE
    @DeleteMapping("/id/{myId}")
    public ResponseEntity<?> deleteJournalEntryById(@PathVariable ObjectId myId) {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        boolean removed = journalEntryService.deleteById(myId, userName);

        if (removed) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // ✅ UPDATE (FIXED PROPERLY 🔥)
    @PutMapping("/id/{myId}")
    public ResponseEntity<?> updateJournalById(@PathVariable ObjectId myId,
                                               @RequestBody JournalEntry newEntry) {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        System.out.println("AUTH USER: " + userName);

        try {
            JournalEntry updated = journalEntryService.updateEntry(myId, newEntry, userName);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}