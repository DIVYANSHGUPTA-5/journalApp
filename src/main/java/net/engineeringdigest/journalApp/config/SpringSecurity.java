package net.engineeringdigest.journalApp.config;

import net.engineeringdigest.journalApp.filter.JwtFilter;
import net.engineeringdigest.journalApp.service.UserDetailsServiceImpl;
import net.engineeringdigest.journalApp.service.UserService;
import net.engineeringdigest.journalApp.utilis.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.oauth2.core.user.OAuth2User;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SpringSecurity {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth

                        // PUBLIC
                        .requestMatchers("/public/**").permitAll()

                        // OAUTH
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                        // SWAGGER
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui.html"
                        ).permitAll()

                        // ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // USER
                        .requestMatchers("/user/**").authenticated()

                        // EVERYTHING ELSE
                        .anyRequest().permitAll()
                )

                // RETURN 401 INSTEAD OF REDIRECT
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Unauthorized\"}");
                        })
                )

                // SESSION REQUIRED FOR OAUTH
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                // 🔥 FIXED OAUTH CONFIG
                .oauth2Login(oauth -> oauth

                        // ✅ FORCE CORRECT BASE URI WITH CONTEXT PATH
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/journal/oauth2/authorization")
                        )

                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/journal/login/oauth2/code/*")
                        )

                        .successHandler((request, response, authentication) -> {

                            System.out.println("🔥 GOOGLE LOGIN SUCCESS");

                            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

                            String email = oAuth2User.getAttribute("email");
                            String name = oAuth2User.getAttribute("name");

                            System.out.println("EMAIL: " + email);
                            System.out.println("NAME: " + name);

                            // SAVE USER
                            userService.processOAuthPostLogin(email, name);

                            // GENERATE JWT
                            String token = jwtUtil.generateToken(email);

                            System.out.println("🔥 JWT TOKEN: " + token);

                            // RETURN TOKEN
                            response.setContentType("application/json");
                            response.getWriter().write("{\"token\": \"" + token + "\"}");
                        })
                )

                .authenticationProvider(authenticationProvider())

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}