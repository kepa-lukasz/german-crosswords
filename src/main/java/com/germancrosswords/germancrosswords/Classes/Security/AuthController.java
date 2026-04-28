package com.germancrosswords.germancrosswords.Classes.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AuthController {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/api/user/me")
    public Map<String, Object> getCurrentUser(Authentication authentication) {
        // 1. Sprawdzamy, czy w ogóle jest jakaś sesja
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return Map.of("logged", false);
        }

        Object principal = authentication.getPrincipal();

        // 2. SCENARIUSZ: Logowanie przez Google
        if (principal instanceof OAuth2User oAuth2User) {
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");

            // Pobieramy użytkownika z bazy (lub tworzymy jeśli nowy)
            User user = userRepo.findByEmail(email).orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setUsername(name != null ? name : email);
                newUser.setProvider("GOOGLE");
                newUser.setPoints(0);
                return userRepo.save(newUser);
            });

            return Map.of(
                    "logged", true,
                    "name", user.getUsername(), // Używamy nazwy z bazy dla spójności
                    "email", user.getEmail(),
                    "provider", "GOOGLE",
                    "points", user.getPoints() // <--- Punkty dodane
            );
        }

        // 3. SCENARIUSZ: Logowanie klasyczne (Email/Hasło)
        else if (principal instanceof UserDetails userDetails) {
            String identifier = userDetails.getUsername();
            User dbUser = userRepo.findByUsername(identifier)
                    .orElseGet(() -> userRepo.findByEmail(identifier).orElse(null));

            if (dbUser == null)
                return Map.of("logged", false);

            return Map.of(
                    "logged", true,
                    "name", dbUser.getUsername(),
                    "email", dbUser.getEmail(),
                    "points", dbUser.getPoints(), // <--- Punkty dodane
                    "provider", "LOCAL");
        }

        return Map.of("logged", false);
    }

    @PostMapping("/api/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {

        // 1. Sprawdzamy, czy nazwa użytkownika jest wolna
        if (userRepo.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Ta nazwa użytkownika jest już zajęta!"));
        }

        // 2. Sprawdzamy, czy e-mail jest wolny
        if (userRepo.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Ten e-mail jest już przypisany do innego konta!"));
        }

        // 3. Tworzymy użytkownika
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail()); // Zapisujemy e-mail
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setProvider("LOCAL");
        newUser.setPoints(0);

        userRepo.save(newUser);

        return ResponseEntity.ok(Map.of("message", "Konto zostało pomyślnie utworzone!"));
    }

    // Mała klasa pomocnicza (DTO) do odbierania JSONa z Reacta
    public static class RegisterRequest {
        private String username;
        private String password;
        private String email; // Dodajemy to pole

        // Gettery i Settery
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
