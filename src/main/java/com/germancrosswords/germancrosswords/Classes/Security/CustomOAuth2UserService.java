package com.germancrosswords.germancrosswords.Classes.Security;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    // Tworzymy instancję loggera SLF4J dla tej klasy
    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    @Autowired
    private UserRepo userRepo;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.print("---- OAUTH2 DEBUG: Rozpoczęto autoryzację z Google ----");
        
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        logger.info("---- OAUTH2 DEBUG: Zalogowano z użyciem e-mail: {}", email);

        Optional<User> userOptional = userRepo.findByEmail(email);

        if (userOptional.isEmpty()) {
            logger.info("---- OAUTH2 DEBUG: Brak usera w bazie. Przystępuję do tworzenia...");
            
            try {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setUsername(name != null ? name : email); 
                newUser.setProvider("GOOGLE");
                newUser.setPoints(0);
                
                userRepo.save(newUser);
                logger.info("---- OAUTH2 DEBUG: SUKCES! Użytkownik zapisany w bazie.");
                
            } catch (Exception e) {
                // To wyłapie wszelkie błędy bazy danych (np. null constraint)
                logger.error("---- OAUTH2 BŁĄD KRYTYCZNY: Nie udało się zapisać użytkownika!", e);
            }
        } else {
            logger.info("---- OAUTH2 DEBUG: Użytkownik istnieje już w bazie. Pomijam tworzenie.");
        }

        return oAuth2User;
    }
}