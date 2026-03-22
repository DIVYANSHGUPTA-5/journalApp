package net.engineeringdigest.journalApp.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("External service - skipping for now")
public class EmailServiceTests {

    @Autowired
    private EmailService emailService;

    @MockBean
    private JavaMailSender javaMailSender;

    @Test
    void testSendMail() {
        emailService.sendEmail(
                "test@gmail.com",
                "Testing Java mail sender",
                "Hi, this is a test email"
        );
    }
}