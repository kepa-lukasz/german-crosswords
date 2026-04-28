package com.germancrosswords.germancrosswords.Classes.Settings;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.germancrosswords.germancrosswords.Classes.Security.User;
import com.germancrosswords.germancrosswords.Classes.Security.UserRepo;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@RestController
public class SettingsController {

    @Autowired
    private UserRepo userRepo;

    // 1. Zmiana Username
    @PutMapping("/update-username")
    @Transactional
    public ResponseEntity<?> updateUsername(@RequestParam String newUsername, Authentication auth) {
        User user = getAuthenticatedUser(auth);
        if (user == null) return ResponseEntity.status(401).build();

        if (newUsername == null || newUsername.trim().length() < 3) {
            return ResponseEntity.badRequest().body("Nazwa musi mieć min. 3 znaki");
        }

        user.setUsername(newUsername.trim());
        userRepo.save(user);

        return ResponseEntity.ok(Map.of("message", "Username zaktualizowany", "newName", user.getUsername()));
    }

    // 2. Usunięcie konta
    @DeleteMapping("/delete-account")
    @Transactional
    public ResponseEntity<?> deleteAccount(Authentication auth, HttpServletRequest request) {
        User user = getAuthenticatedUser(auth);
        if (user == null) return ResponseEntity.status(401).build();
        System.err.println(user);
        userRepo.delete(user);

        // Wylogowanie użytkownika po usunięciu (wyczyszczenie sesji)
        try {
            request.logout();
        } catch (ServletException e) {
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok(Map.of("message", "Konto usunięte pomyślnie"));
    }

    // Metoda pomocnicza (taka sama jak wcześniej)
    private User getAuthenticatedUser(Authentication auth) {
        if (auth == null) return null;
        if (auth.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User oAuth2User) {
            return userRepo.findByEmail(oAuth2User.getAttribute("email")).orElse(null);
        }
        return userRepo.findByEmail(auth.getName()).orElseGet(() -> userRepo.findByUsername(auth.getName()).orElse(null));
    }
}