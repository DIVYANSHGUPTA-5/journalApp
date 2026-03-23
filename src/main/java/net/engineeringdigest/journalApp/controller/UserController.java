package net.engineeringdigest.journalApp.controller;

import net.engineeringdigest.journalApp.api.response.WeatherResponse;
import net.engineeringdigest.journalApp.entity.User;
import net.engineeringdigest.journalApp.repository.UserRepository;
import net.engineeringdigest.journalApp.service.UserService;
import net.engineeringdigest.journalApp.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WeatherService weatherService;

    // ✅ COMMON METHOD (IMPORTANT)
    private String getLoggedInEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth.getPrincipal() instanceof OAuth2User) {
            OAuth2User oAuth2User = (OAuth2User) auth.getPrincipal();
            return oAuth2User.getAttribute("email");
        } else {
            return auth.getName();
        }
    }

    // CREATE USER
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        userService.saveNewUser(user);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    // UPDATE LOGGED-IN USER
    @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody User user) {

        String userName = getLoggedInEmail();

        User userInDb = userService.findByUserName(userName);

        if (userInDb == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        userInDb.setUserName(user.getUserName());
        userInDb.setPassword(user.getPassword());

        userService.saveNewUser(userInDb);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // DELETE LOGGED-IN USER
    @DeleteMapping
    public ResponseEntity<?> deleteUser() {

        String userName = getLoggedInEmail();

        userRepository.deleteByUserName(userName);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // GREETING WITH WEATHER
    @GetMapping
    public ResponseEntity<?> greeting() {

        String userName = getLoggedInEmail();

        WeatherResponse weatherResponse =
                weatherService.getWeather("Mumbai");

        String greeting = "";

        if (weatherResponse != null &&
                weatherResponse.getCurrent() != null) {

            greeting = ", Weather feels like "
                    + weatherResponse.getCurrent().getFeelslike();
        }

        return new ResponseEntity<>(
                "Hi " + userName + greeting,
                HttpStatus.OK
        );
    }
}