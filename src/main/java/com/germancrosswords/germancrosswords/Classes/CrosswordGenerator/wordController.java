package com.germancrosswords.germancrosswords.Classes.CrosswordGenerator;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.germancrosswords.germancrosswords.Classes.DB.Word;
import com.germancrosswords.germancrosswords.Classes.DB.WordRepo;
import com.germancrosswords.germancrosswords.Classes.Security.UserRepo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;

@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
@RestController
@Validated
public class wordController {
    private final CrosswordService crosswordService;

    public wordController(CrosswordService crosswordService) {
        this.crosswordService = crosswordService;
    }

    private static final int MAX_LENGTH = 30;
    @Autowired
    private WordRepo wordRepo;
    @Autowired
    private UserRepo userRepo;

    @GetMapping("/GetWords")
    public List<Word> getWords(
            @RequestParam(name = "count", defaultValue = "10") int count,
            @RequestParam(name = "seed", required = false) String seed) {

        count = (count > MAX_LENGTH || count < 1) ? MAX_LENGTH : count;

        if (seed == null || seed.trim().isEmpty()) {
            seed = UUID.randomUUID().toString().substring(0, 30).toUpperCase();
        }

        return wordRepo.findWordsBySeed(count, seed);
    }

    // Prosty rekord (DTO), żeby nie wysyłać haseł i maili graczy do sieci
    public record PlayerScore(String username, int points) {
    }

    @GetMapping("/api/ranking")
    public List<PlayerScore> getTopPlayers() {
        return userRepo.findTop5ByOrderByPointsDesc().stream()
                .map(user -> new PlayerScore(user.getUsername(), user.getPoints()))
                .collect(Collectors.toList());
    }

    @GetMapping("/GetCrossWords")
    public List<PlacedWordDTO> getCrossword(
            // WALIDACJA OCHRONNA
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Musisz pobrać minimum 1 słówko") @Max(value = MAX_LENGTH, message = "Maksimum to 50 słówek") int count,
            @RequestParam(required = false) @Size(max = 20, message = "Seed nie może mieć więcej niż 20 znaków") String seed,
            @RequestParam(required = false, defaultValue = "MEDIUM") @Pattern(regexp = "^(EASY|MEDIUM|HARD)$", message = "Dozwolone poziomy trudności to: EASY, MEDIUM, HARD") String difficulty) {

        seed = (seed == null || seed.trim().isEmpty())
                ? UUID.randomUUID().toString().substring(0, 6).toUpperCase()
                : seed;

        // 2. KROK PIERWSZY: Wyciągamy dane z bazy
        List<Word> rawWords = wordRepo.findWordsBySeedAndCriteria(count, seed, difficulty);

        // 3. KROK DRUGI: Generator układa krzyżówkę (odrzucając to, co nie pasuje)
        return crosswordService.generateCrossword(rawWords);

    }
}
