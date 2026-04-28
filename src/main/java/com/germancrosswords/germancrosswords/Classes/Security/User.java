package com.germancrosswords.germancrosswords.Classes.Security;

import java.util.List;

import com.germancrosswords.germancrosswords.Classes.Ranking.RankingGame;

import jakarta.persistence.*;

@Entity
@Table(name = "users") // Zmieniamy nazwę tabeli, bo "user" to słowo zastrzeżone w SQL
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username; // Unikalna nazwa w grze
    @Column(unique = true, nullable = false)
    private String email;

    private String password; // Zabezpieczone hasło (będzie null dla kont z Google)

    private int points = 0; // Punkty w rankingu

    @Column(nullable = false)
    private String provider; // Skąd jest to konto? ("LOCAL" lub "GOOGLE")

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RankingGame> games;

    // --- Konstruktory ---
    public User() {
    }

    // --- Gettery i Settery ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
