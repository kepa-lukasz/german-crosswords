package com.germancrosswords.germancrosswords.Classes.Ranking;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.germancrosswords.germancrosswords.Classes.Security.User;

@Entity
@Table(name = "ranking_games")
public class RankingGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private int wordCount;
    private String seed;
    private String difficulty;
    private int potentialPoints;
    private int hints =0;

    // Na początku będzie tu 0, wypełnimy to dopiero po zakończeniu gry
    private int finalPoints = 0;

    // Flaga statusu gry
    private boolean isFinished = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    public RankingGame() {
    }

    // Konstruktor używany przy starcie gry
    public RankingGame(User user, int wordCount, String seed, String difficulty, int potentialPoints, int hints) {
        this.user = user;
        this.seed = seed;
        this.difficulty = difficulty;
        this.potentialPoints = potentialPoints;
        this.wordCount = wordCount;
    }

    public LocalDateTime getCreatedAt() {return createdAt;}
    // Gettery i Settery
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getSeed() {
        return seed;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public int getPotentialPoints() {
        return potentialPoints;
    }

    public int getWordCount() {
        return wordCount;
    }

    public int getFinalPoints() {
        return finalPoints;
    }

    public void setFinalPoints(int finalPoints) {
        this.finalPoints = finalPoints;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    public int getHintsCount() {
        return hints;
    }

    public void setHintsCount(int hintsCount) {
        this.hints = hintsCount;
    }
}