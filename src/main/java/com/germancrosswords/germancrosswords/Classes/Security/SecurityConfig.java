package com.germancrosswords.germancrosswords.Classes.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        
        

        // .authorizeHttpRequests(auth -> auth
        //     .requestMatchers("/GetWords", "/api/ranking", "/GetCrossWords", "/api/user/me", "/api/register", "/api/login").permitAll()
        //     .anyRequest().authenticated()
        // )

        // 2. FORMULARZ (tylko endpoint API)
        .formLogin(form -> form
            .loginProcessingUrl("/api/login")
            // Usunięcie .loginPage() przy obecnym EntryPoint jest bezpieczniejsze, 
            // bo EntryPoint i tak przejmuje kontrolę.
            .successHandler((req, res, auth) -> {
                res.setStatus(200);
                res.getWriter().write("{\"status\": \"ok\"}");
            })
            .failureHandler((req, res, err) -> {
                res.setStatus(401);
                res.getWriter().write("{\"status\": \"error\"}");
            }).permitAll()
        )
        .oauth2Login(oauth2 -> oauth2
            .defaultSuccessUrl("http://localhost:3000/#/", true)
        )
        .logout(logout -> logout.logoutSuccessUrl("http://localhost:3000/#/").permitAll())
        
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint((req, res, err) -> {
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            })
        )

        .authorizeHttpRequests(auth -> auth
            // Używamy samych Stringów - to najbezpieczniejsza opcja w Spring 6
            .requestMatchers("/error").permitAll()
            .requestMatchers("/GetCrossWords").permitAll()
            .requestMatchers("/api/ranking").permitAll()
            .requestMatchers("/api/user/me").permitAll()
            .requestMatchers("/api/register").permitAll()
            .requestMatchers("/api/login").permitAll()
            .requestMatchers("/").permitAll()
            
            // Jeśli to wciąż nie działa, dodajmy to jako "ostatnią deskę ratunku":
            .requestMatchers("/favicon.ico", "/static/**", "/css/**", "/js/**").permitAll()
            
            .anyRequest().authenticated()
        );

    return http.build();
}

    // Konfiguracja CORS - kluczowa dla Reacta
    @Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    // Musi być konkretny adres, NIE "*"
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000")); 
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
    configuration.setAllowCredentials(true); // To wymaga konkretnego Origin powyżej
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
