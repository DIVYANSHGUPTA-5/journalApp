package net.engineeringdigest.journalApp.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

@DataMongoTest
@ActiveProfiles("test")
public class UserRepositoryImplTests {

    @Autowired
    private UserRepositoryImpl userRepository;

    @Test
    void testSaveNewUser() {
        Assertions.assertNotNull(userRepository.getUserForSA());
    }
}