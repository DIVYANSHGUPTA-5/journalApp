package net.engineeringdigest.journalApp.controller;

import net.engineeringdigest.journalApp.entity.JournalEntry;
import net.engineeringdigest.journalApp.service.JournalEntryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@RestController
@RequestMapping("/journal")
public class JournalEntryController {

    @Autowired
    private JournalEntryService journalEntryService;

    @GetMapping("/ping")
    public String ping() {
        return "OK";
    }

    // ✅ GET ALL
    @GetMapping
    public ResponseEntity<?> getALLJournalEntriesOfUser() {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        List<JournalEntry> entries = journalEntryService.findByUserName(userName);

        return new ResponseEntity<>(entries, HttpStatus.OK);
    }

    // ✅ CREATE
    @PostMapping
    public ResponseEntity<?> createEntry(@RequestBody JournalEntry myEntry) {

        try {
            String userName = SecurityContextHolder.getContext().getAuthentication().getName();

            JournalEntry saved = journalEntryService.saveEntry(myEntry, userName);

            return new ResponseEntity<>(saved, HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>("Error creating entry", HttpStatus.BAD_REQUEST);
        }
    }

    // ✅ GET BY ID
    @GetMapping("/id/{myId}")
    public ResponseEntity<?> getJournalEntryById(
            @Parameter(
                    description = "Journal Entry ID",
                    schema = @Schema(type = "string")
            )
            @PathVariable String myId) {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        return journalEntryService.getEntryByIdForUser(myId, userName)
                .map(entry -> new ResponseEntity<>(entry, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // ✅ DELETE
    @DeleteMapping("/id/{myId}")
    public ResponseEntity<?> deleteJournalEntryById(
            @Parameter(
                    description = "Journal Entry ID",
                    schema = @Schema(type = "string")
            )
            @PathVariable String myId) {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        boolean removed = journalEntryService.deleteById(myId, userName);

        if (removed) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // ✅ UPDATE
    @PutMapping("/id/{myId}")
    public ResponseEntity<?> updateJournalById(
            @Parameter(
                    description = "Journal Entry ID",
                    schema = @Schema(type = "string")
            )
            @PathVariable String myId,
            @RequestBody JournalEntry newEntry) {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            JournalEntry updated = journalEntryService.updateEntry(myId, newEntry, userName);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Update failed", HttpStatus.BAD_REQUEST);
        }
    }
}