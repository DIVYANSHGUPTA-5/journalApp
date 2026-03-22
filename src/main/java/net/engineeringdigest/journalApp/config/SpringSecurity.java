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
                        .requestMatchers("/login/**", "/oauth2/**").permitAll()

                        // SWAGGER
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui.html"
                        ).permitAll()

                        // ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // PROTECTED
                        .requestMatchers("/user/**").authenticated()

                        // EVERYTHING ELSE PROTECTED
                        .anyRequest().permitAll()
                )

                // ✅ FIX: Return 401 instead of redirecting to Google
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Unauthorized\"}");
                        })
                )

                // OAuth needs session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                // OAuth2 Login
                .oauth2Login(oauth -> oauth
                        .successHandler((request, response, authentication) -> {

                            System.out.println("🔥 GOOGLE LOGIN SUCCESS");

                            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

                            String email = oAuth2User.getAttribute("email");
                            String name = oAuth2User.getAttribute("name");

                            System.out.println("EMAIL: " + email);
                            System.out.println("NAME: " + name);

                            // Save or fetch user
                            userService.processOAuthPostLogin(email, name);

                            // Generate JWT
                            String token = jwtUtil.generateToken(email);

                            System.out.println("🔥 JWT TOKEN: " + token);

                            // Return token
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