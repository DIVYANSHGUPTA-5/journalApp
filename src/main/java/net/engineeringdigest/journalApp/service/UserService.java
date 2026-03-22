package net.engineeringdigest.journalApp.service;

import lombok.extern.slf4j.Slf4j;
import net.engineeringdigest.journalApp.entity.JournalEntry;
import net.engineeringdigest.journalApp.entity.User;
import net.engineeringdigest.journalApp.repository.UserRepository;
import net.engineeringdigest.journalApp.repository.UserRepositoryImpl;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRepositoryImpl userRepositoryImpl;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public boolean saveNewUser(User user) {
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(Arrays.asList("USER"));
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            log.error("Error saving user", e);
            return false;
        }
    }

    public void saveAdmin(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Arrays.asList("USER", "ADMIN"));
        userRepository.save(user);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(ObjectId id) {
        return userRepository.findById(id);
    }

    public void deleteById(ObjectId id) {
        userRepository.deleteById(id);
    }

    public User findByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }

    public List<User> getUsersForSentimentAnalysis() {
        return userRepository.findAll();
    }

    public List<JournalEntry> getAllEntriesOfUser(String userName) {
        User user = findByUserName(userName);
        if (user == null) {
            return Collections.emptyList();
        }
        return user.getJournalEntries();
    }

    // 🔥🔥🔥 MOST IMPORTANT METHOD (NEW)
    public User processOAuthPostLogin(String email, String name) {

        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            return existingUser.get(); // ✅ already exists
        }

        // ❗ create new user
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setUserName(email); // use email as username
        newUser.setPassword(passwordEncoder.encode("oauth_dummy_password"));
        newUser.setRoles(Arrays.asList("USER"));

        return userRepository.save(newUser);
    }
}