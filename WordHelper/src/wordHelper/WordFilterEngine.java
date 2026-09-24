package wordHelper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core word-filtering algorithm. This replaces the old console-based
 * jeffgoldblum.java: instead of prompting the user through System.in,
 * it takes a list of Guess objects (one per row of colored letter
 * boxes) and narrows a candidate word list down to the words that are
 * still consistent with every guess.
 *
 * Color/state values used everywhere in this program:
 *   0 = EMPTY  (box has no letter yet, or was never clicked -> no constraint)
 *   1 = GRAY   (letter is not in the word)
 *   2 = YELLOW (letter is in the word, but in the wrong position)
 *   3 = GREEN  (letter is in the word, in the correct position)
 */
public class WordFilterEngine {

    public static final int EMPTY = 0;
    public static final int GRAY = 1;
    public static final int YELLOW = 2;
    public static final int GREEN = 3;

    /** One submitted guess: the letters typed in a row and the color of each box. */
    public static class Guess {
        public final char[] letters;
        public final int[] states;

        public Guess(char[] letters, int[] states) {
            if (letters.length != states.length) {
                throw new IllegalArgumentException("letters and states must be the same length");
            }
            this.letters = letters;
            this.states = states;
        }
    }

    /** Loads a plain-text/CSV word list with one word per line (e.g. jeff.csv, wordle.csv). */
    public static List<String> loadWords(String filePath) throws IOException {
        List<String> words = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim();
                if (!word.isEmpty()) {
                    words.add(word.toLowerCase());
                }
            }
        }
        return words;
    }

    /** Filters candidates down to words consistent with every guess submitted so far. */
    public static List<String> filter(List<String> candidates, List<Guess> guesses) {
        List<String> result = new ArrayList<>();
        for (String word : candidates) {
            if (matchesAll(word, guesses)) {
                result.add(word);
            }
        }
        return result;
    }

    private static boolean matchesAll(String word, List<Guess> guesses) {
        for (Guess guess : guesses) {
            if (!matches(word, guess)) {
                return false;
            }
        }
        return true;
    }

    private static boolean matches(String word, Guess guess) {
        int len = guess.letters.length;
        if (word.length() != len) {
            return false;
        }

        // Green letters must match exactly at their position.
        for (int i = 0; i < len; i++) {
            if (guess.states[i] == GREEN
                    && word.charAt(i) != Character.toLowerCase(guess.letters[i])) {
                return false;
            }
        }

        // Yellow letters must NOT be at that position (but must appear elsewhere - checked below).
        for (int i = 0; i < len; i++) {
            if (guess.states[i] == YELLOW
                    && word.charAt(i) == Character.toLowerCase(guess.letters[i])) {
                return false;
            }
        }

        // Handle duplicate letters correctly: figure out, per letter, the minimum
        // number of copies the word must contain (from green + yellow marks) and
        // whether a gray mark caps that letter at exactly that count.
        Map<Character, Integer> minCount = new HashMap<>();
        Map<Character, Boolean> capped = new HashMap<>();

        for (int i = 0; i < len; i++) {
            char c = Character.toLowerCase(guess.letters[i]);
            if (c == '\0') continue;
            int state = guess.states[i];
            if (state == GREEN || state == YELLOW) {
                minCount.merge(c, 1, Integer::sum);
            }
        }
        for (int i = 0; i < len; i++) {
            char c = Character.toLowerCase(guess.letters[i]);
            if (c == '\0') continue;
            if (guess.states[i] == GRAY) {
                capped.put(c, true);
                minCount.putIfAbsent(c, 0);
            }
        }

        for (Map.Entry<Character, Integer> entry : minCount.entrySet()) {
            char c = entry.getKey();
            int needed = entry.getValue();
            int actual = countOccurrences(word, c);
            if (actual < needed) {
                return false;
            }
            if (capped.getOrDefault(c, false) && actual != needed) {
                return false;
            }
        }

        return true;
    }

    private static int countOccurrences(String word, char c) {
        int count = 0;
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == c) count++;
        }
        return count;
    }
}