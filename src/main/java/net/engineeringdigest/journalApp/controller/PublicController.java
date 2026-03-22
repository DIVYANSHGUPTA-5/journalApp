package net.engineeringdigest.journalApp.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import net.engineeringdigest.journalApp.dto.UserDTO;
import net.engineeringdigest.journalApp.entity.User;
import net.engineeringdigest.journalApp.service.EmailService;
import net.engineeringdigest.journalApp.service.UserDetailsServiceImpl;
import net.engineeringdigest.journalApp.service.UserService;
import net.engineeringdigest.journalApp.utilis.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public")
@Slf4j
@Tag(name = "Public APIs")
public class PublicController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private UserService userService;
    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;

    // ✅ Health check
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.info("Health is OK");
        return ResponseEntity.ok("OK");
    }



    @GetMapping("/test-mail")
    public String sendTestMail() {
        emailService.sendEmail(
                "dpkwork123@gmail.com",  // 👈 put your real email here
                "Test Mail",
                "Email working 🚀"
        );
        return "Mail sent";
    }


    // ✅ Signup
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserDTO userDTO) {

        User newUser = new User();
        newUser.setEmail(userDTO.getEmail());
        newUser.setUserName(userDTO.getUserName());
        newUser.setPassword(userDTO.getPassword());

        // ✅ FIXED LINE


        boolean saved = userService.saveNewUser(newUser);

        if (saved) {
            return ResponseEntity.ok("User created successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User creation failed");
        }
    }

    // ✅ Login → returns JWT
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUserName(),
                            user.getPassword()
                    )
            );

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(user.getUserName());

            String jwt = jwtUtil.generateToken(userDetails.getUsername());

            return ResponseEntity.ok(jwt);

        } catch (Exception e) {
            log.error("Login failed", e);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Incorrect username or password");
        }
    }
}