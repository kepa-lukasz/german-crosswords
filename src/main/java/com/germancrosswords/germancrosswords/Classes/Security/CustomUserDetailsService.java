package com.germancrosswords.germancrosswords.Classes.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        
        // Szukamy po loginie, a jak nie znajdziemy - po emailu
        User user = userRepo.findByUsername(usernameOrEmail)
                .orElseGet(() -> userRepo.findByEmail(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika: " + usernameOrEmail)));

        // Tłumaczymy naszego Usera na obiekt, który rozumie Spring Security
        return org.springframework.security.core.userdetails.User.builder()
                .username(usernameOrEmail) // Spring musi dostać to, co wpisał użytkownik
                .password(user.getPassword())
                .authorities(Collections.emptyList()) // Brak ról na tym etapie
                .build();
    }
}