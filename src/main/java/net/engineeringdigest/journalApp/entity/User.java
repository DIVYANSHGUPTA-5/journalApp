package net.engineeringdigest.journalApp.entity;

import lombok.Data;
import lombok.NonNull;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private ObjectId id;

    // ✅ Username (for JWT + normal login)
    @Indexed(unique = true)
    @NonNull
    private String userName;

    // ✅ Email (for Google OAuth)
    @Indexed(unique = true) // 🔥 IMPORTANT
    private String email;

    // ❌ REMOVE this (not needed)
    // private String sentimentAnalysis;

    // ✅ Password (can be null for Google users)
    private String password;

    // ✅ Roles
    private List<String> roles = new ArrayList<>();

    // ✅ OAuth provider (VERY IMPORTANT)
    private String provider; // "GOOGLE" or "LOCAL"

    @DBRef
    private List<JournalEntry> journalEntries = new ArrayList<>();
}