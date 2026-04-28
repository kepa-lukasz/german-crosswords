package com.germancrosswords.germancrosswords.Classes.Security;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${FRONTEND_URL:http://localhost:3000}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form
                .loginProcessingUrl("/api/login")
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
                .defaultSuccessUrl(frontendUrl + "/#/", true)
            )
            .logout(logout -> logout.logoutSuccessUrl(frontendUrl + "/#/").permitAll())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, err) -> {
                    res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                })
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/error").permitAll()
                .requestMatchers("/GetCrossWords").permitAll()
                .requestMatchers("/api/ranking").permitAll()
                .requestMatchers("/api/user/me").permitAll()
                .requestMatchers("/api/register").permitAll()
                .requestMatchers("/api/login").permitAll()
                .requestMatchers("/").permitAll()
                .requestMatchers("/favicon.ico", "/static/**", "/css/**", "/js/**").permitAll()
                .anyRequest().authenticated()
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            frontendUrl
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}