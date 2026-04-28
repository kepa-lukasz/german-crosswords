package com.germancrosswords.germancrosswords.Classes.CrosswordGenerator;

import com.germancrosswords.germancrosswords.Classes.DB.Word;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CrosswordService {

    private static final int GRID_SIZE = 50;

    public List<PlacedWordDTO> generateCrossword(List<Word> words) {
        List<PlacedWordDTO> placedWords = new ArrayList<>();
        char[][] grid = new char[GRID_SIZE][GRID_SIZE];
        int currentPosition = 1;

        for (Word word : words) {
            String answer = word.getGerman().toUpperCase();
            String polish = word.getPolish(); // Pobieramy polskie tłumaczenie

            if (placedWords.isEmpty()) {
                int startX = (GRID_SIZE / 2) - (answer.length() / 2);
                int startY = GRID_SIZE / 2;

                placeOnGrid(grid, answer, startX, startY, "across");
                // Przekazujemy 'polish' do DTO
                placedWords.add(new PlacedWordDTO(answer, polish, startX, startY, "across", currentPosition++));
            } else {
                // Przekazujemy 'polish' do metody szukającej, by mogła stworzyć DTO
                PlacedWordDTO placement = findPlacementForWord(answer, polish, placedWords, grid, currentPosition);
                
                if (placement != null) {
                    placeOnGrid(grid, answer, placement.getStartx(), placement.getStarty(), placement.getOrientation());
                    placedWords.add(placement);
                    currentPosition++;
                }
            }
        }
        normalizeCoordinates(placedWords);
        return placedWords;
    }

    private PlacedWordDTO findPlacementForWord(String newWord, String polish, List<PlacedWordDTO> placedWords, char[][] grid, int position) {
        for (PlacedWordDTO placed : placedWords) {
            String placedAnswer = placed.getAnswer().toUpperCase();

            for (int i = 0; i < newWord.length(); i++) {
                char c = newWord.charAt(i);
                int matchIndex = placedAnswer.indexOf(c);

                if (matchIndex != -1) {
                    int newX, newY;
                    String newOrientation;

                    if (placed.getOrientation().equals("across")) {
                        newOrientation = "down";
                        newX = placed.getStartx() + matchIndex;
                        newY = placed.getStarty() - i;
                    } else {
                        newOrientation = "across";
                        newX = placed.getStartx() - i;
                        newY = placed.getStarty() + matchIndex;
                    }
                    if (placedWords.stream().anyMatch(w -> w.getStartx() == newX && w.getStarty() == newY)) continue;
                    if (canPlaceOnGrid(grid, newWord, newX, newY, newOrientation)) {
                        return new PlacedWordDTO(newWord, polish, newX, newY, newOrientation, position);
                    }
                }
            }
        }
        return null; 
    }

    private boolean canPlaceOnGrid(char[][] grid, String word, int x, int y, String orientation) {
        if (x < 0 || y < 0) return false;
        if (orientation.equals("across") && x + word.length() >= GRID_SIZE) return false;
        if (orientation.equals("down") && y + word.length() >= GRID_SIZE) return false;

        // 1. Sprawdzenie pola TUŻ PRZED słowem (musi być puste)
        if (orientation.equals("across") && x > 0 && grid[x - 1][y] != '\u0000') return false;
        if (orientation.equals("down") && y > 0 && grid[x][y - 1] != '\u0000') return false;

        // 2. Sprawdzenie pola TUŻ ZA słowem (musi być puste)
        if (orientation.equals("across") && x + word.length() < GRID_SIZE && grid[x + word.length()][y] != '\u0000') return false;
        if (orientation.equals("down") && y + word.length() < GRID_SIZE && grid[x][y + word.length()] != '\u0000') return false;

        // 3. Sprawdzenie liter i ich bocznego sąsiedztwa
        for (int i = 0; i < word.length(); i++) {
            int cx = orientation.equals("across") ? x + i : x;
            int cy = orientation.equals("down") ? y + i : y;

            char existing = grid[cx][cy];

            if (existing != '\u0000') {
                // Jeśli pole zajęte, litera musi się zgadzać (to jest nasze legalne skrzyżowanie)
                if (existing != word.charAt(i)) return false;
            } else {
                // Jeśli pole jest puste, sprawdzamy sąsiadów PROSTOPADŁYCH do kierunku pisania
                // To zapobiega sytuacji "słowo obok słowa" z Twojego obrazka
                if (orientation.equals("across")) {
                    if (cy > 0 && grid[cx][cy - 1] != '\u0000') return false; // góra
                    if (cy < GRID_SIZE - 1 && grid[cx][cy + 1] != '\u0000') return false; // dół
                } else {
                    if (cx > 0 && grid[cx - 1][cy] != '\u0000') return false; // lewo
                    if (cx < GRID_SIZE - 1 && grid[cx + 1][cy] != '\u0000') return false; // prawo
                }
            }
        }
        return true;
    }

    private void placeOnGrid(char[][] grid, String word, int x, int y, String orientation) {
        for (int i = 0; i < word.length(); i++) {
            int placeX = orientation.equals("across") ? x + i : x;
            int placeY = orientation.equals("down") ? y + i : y;
            grid[placeX][placeY] = word.charAt(i);
        }
    }

    private void normalizeCoordinates(List<PlacedWordDTO> placedWords) {
        if (placedWords.isEmpty()) return;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        for (PlacedWordDTO word : placedWords) {
            if (word.getStartx() < minX) minX = word.getStartx();
            if (word.getStarty() < minY) minY = word.getStarty();
        }
        for (PlacedWordDTO word : placedWords) {
            word.setStartx(word.getStartx() - minX + 1);
            word.setStarty(word.getStarty() - minY + 1);
        }
    }
}