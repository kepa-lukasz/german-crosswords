package com.germancrosswords.germancrosswords.Classes.Ranking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.germancrosswords.germancrosswords.Classes.CrosswordGenerator.CrosswordService;
import com.germancrosswords.germancrosswords.Classes.DB.Word;
import com.germancrosswords.germancrosswords.Classes.DB.WordRepo;
import com.germancrosswords.germancrosswords.Classes.Security.User;
import com.germancrosswords.germancrosswords.Classes.Security.UserRepo;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;

@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
@RestController
@Validated
public class RankingController {
    private final CrosswordService crosswordService;

    public RankingController(CrosswordService crosswordService) {
        this.crosswordService = crosswordService;
    }

    private static final int RANKING_COUNT = 10;
    @Autowired
    private WordRepo wordRepo;
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RankingGameRepo rankingGameRepo;

    // 1. START GRY - Generowanie i zapisywanie sesji
    @GetMapping("/ranking/GetCrossWords")
    public RankingCrosswordDTO getCrossWords(
            @RequestParam(required = false, defaultValue = "MEDIUM") @Pattern(regexp = "^(EASY|MEDIUM|HARD)$") String difficulty,
            Authentication auth) {
        // Generowanie danych
        String seed = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        List<Word> rawWords = wordRepo.findWordsBySeedAndCriteria(RANKING_COUNT, seed, difficulty);

        // Obliczanie mnożnika i punktów
        int multiplier = switch (difficulty.toUpperCase()) {
            case "HARD" -> 3;
            case "MEDIUM" -> 2;
            default -> 1;
        };

        int totalLetters = rawWords.stream().mapToInt(w -> w.getWord().length()).sum();
        int potentialPoints = totalLetters * multiplier;

        Object crossword = crosswordService.generateCrossword(rawWords);

        Long gameId = null;

        // ZAPIS SESJI: Jeśli user jest zalogowany, tworzymy rekord gry w DB

        // if (auth != null && auth.isAuthenticated()) {
        // System.out.println(auth.getName());
        // User user = userRepo.findByUsername(auth.getName()).orElse(null);
        // System.out.print("User:" + auth.getDetails());
        // if (user != null) {
        // RankingGame game = new RankingGame(user, RANKING_COUNT, seed, difficulty,
        // potentialPoints);
        // RankingGame savedGame = rankingGameRepo.save(game);
        // gameId = savedGame.getId(); // To przesyłamy do Reacta
        // }
        // }

        if (auth != null && auth.isAuthenticated()) {
            User user = null;
            Object principal = auth.getPrincipal();

            // 1. Obsługa logowania przez Google (OAuth2)
            if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User oAuth2User) {
                // Google przechowuje email w atrybutach
                String email = oAuth2User.getAttribute("email");
                System.out.println("Logowanie Google: " + email);
                user = userRepo.findByEmail(email).orElse(null);
            }
            // 2. Obsługa logowania standardowego (Form Login)
            else {
                String usernameOrEmail = auth.getName();
                System.out.println("Logowanie standardowe: " + usernameOrEmail);
                // Szukamy najpierw po mailu, potem po username dla pewności
                user = userRepo.findByEmail(usernameOrEmail)
                        .orElseGet(() -> userRepo.findByUsername(usernameOrEmail).orElse(null));
            }

            // 3. Jeśli znaleźliśmy użytkownika, tworzymy grę
            if (user != null) {
                System.out.println("Zidentyfikowano użytkownika: " + user.getId());

                RankingGame game = new RankingGame(user, RANKING_COUNT, seed, difficulty, potentialPoints, 0);
                RankingGame savedGame = rankingGameRepo.save(game);
                gameId = savedGame.getId();
            } else {
                System.out.println("UWAGA: Nie znaleziono użytkownika w bazie dla: " + auth.getName());
            }
        }

        return new RankingCrosswordDTO(crossword, potentialPoints, gameId);
    }

public record FinishGameRequest(
    Long gameId, 
    int hints 
) {}
    @PostMapping("/ranking/FinishGame")
@Transactional
public ResponseEntity<?> finishGame(
        @RequestBody FinishGameRequest request,
        Authentication auth,
        @RequestParam(required = false, defaultValue = "false") boolean surrender
) {
    if (auth == null)
        return ResponseEntity.status(401).body("Musisz być zalogowany");

    // 1. Identyfikacja użytkownika (odporna na Google OAuth2)
    User currentUser = null;
    if (auth.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        currentUser = userRepo.findByEmail(email).orElse(null);
    } else {
        currentUser = userRepo.findByEmail(auth.getName())
                .orElseGet(() -> userRepo.findByUsername(auth.getName()).orElse(null));
    }

    if (currentUser == null)
        return ResponseEntity.status(401).body("Nie znaleziono użytkownika");

    // 2. Znajdź grę w bazie
    RankingGame game = rankingGameRepo.findById(request.gameId())
            .orElseThrow(() -> new RuntimeException("Nie znaleziono sesji gry"));

    // 3. Weryfikacja właściciela gry
    if (!game.getUser().getId().equals(currentUser.getId())) {
        return ResponseEntity.status(403).body("To nie Twoja gra!");
    }

    if (game.isFinished()) {
        return ResponseEntity.badRequest().body("Ta gra została już rozliczona");
    }

    // 4. Obliczanie punktów
    int finalPoints = 0;
    if (surrender) {
        finalPoints = 0;
    } else {
        int multiplier = game.getDifficulty().equalsIgnoreCase("HARD") ? 3
                : game.getDifficulty().equalsIgnoreCase("MEDIUM") ? 2 : 1;

        // Liczymy punkty: potencjał - (błędy * mnożnik) - (podpowiedzi * 1)
        // Używamy danych z obiektu 'request'
        int penalty = (request.hints() * multiplier);
        finalPoints = Math.max(0, game.getPotentialPoints() - penalty);
    }

    // 5. Zapisz dane sesji
    game.setFinalPoints(finalPoints);
    game.setHintsCount(request.hints()); // Jeśli dodałeś to pole do encji
    game.setFinished(true);
    rankingGameRepo.save(game);

    // 6. Aktualizacja punktów użytkownika
    currentUser.setPoints(currentUser.getPoints() + finalPoints);
    userRepo.save(currentUser);

    return ResponseEntity.ok(Map.of(
        "earnedPoints", finalPoints, 
        "newTotalPoints", currentUser.getPoints(),
        "status", surrender ? "SURRENDERED" : "OK"
    ));
}



@GetMapping("/ranking/MyHistory")
public ResponseEntity<?> getMyHistory(Authentication auth) {
    if (auth == null) return ResponseEntity.status(401).body("Musisz być zalogowany");

    // 1. Identyfikacja użytkownika (ten sam mechanizm co w FinishGame)
    User currentUser = null;
    if (auth.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        currentUser = userRepo.findByEmail(email).orElse(null);
    } else {
        currentUser = userRepo.findByEmail(auth.getName())
                .orElseGet(() -> userRepo.findByUsername(auth.getName()).orElse(null));
    }

    if (currentUser == null) return ResponseEntity.status(401).body("Nie znaleziono użytkownika");

    // 2. Pobranie gier z bazy
    List<RankingGame> games = rankingGameRepo.findByUserIdOrderByCreatedAtDesc(currentUser.getId());

    // 3. Mapowanie na listę map lub DTO (żeby nie wysyłać całego obiektu User w każdym rekordzie)
    List<Map<String, Object>> history = games.stream().map(game -> {
        Map<String, Object> map = new HashMap<>();
        map.put("id", game.getId());
        map.put("date", game.getCreatedAt());
        map.put("difficulty", game.getDifficulty());
        map.put("potentialPoints", game.getPotentialPoints());
        map.put("finalPoints", game.getFinalPoints());
        map.put("hints", game.getHintsCount());
        map.put("isFinished", game.isFinished());
        return map;
    }).toList();

    return ResponseEntity.ok(history);
}
}