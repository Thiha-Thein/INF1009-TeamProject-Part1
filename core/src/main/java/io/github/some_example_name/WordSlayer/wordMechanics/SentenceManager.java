package io.github.some_example_name.WordSlayer.wordMechanics;

import java.util.*;

public class SentenceManager {
    private String baseSentence = "";
    // words that must be filled
    private List<String> missingWords = new ArrayList<>();
    // words already filled
    private Set<String> filledWords = new HashSet<>();
    public void loadSentence(String sentence, List<String> blanks) {
        this.baseSentence = sentence;
        this.missingWords = new ArrayList<>(blanks);
        this.filledWords.clear();
    }
    public boolean submitWord(String word) {
        if (missingWords.contains(word)
            && !filledWords.contains(word)) {
            filledWords.add(word);
            return true;
        }
        return false;
    }
    public boolean isSentenceComplete() {
        if (missingWords.isEmpty()) return false;
        return filledWords.size() == missingWords.size();
    }
    public String getDisplaySentence() {
        if (baseSentence == null) return "";
        String result = baseSentence;
        for (String blank : missingWords) {
            if (filledWords.contains(blank)) {
                result = result.replaceFirst("___", blank);
            } else {
                result = result.replaceFirst("___", "___");
            }
        }
        return result;
    }
    public List<String> getMissingWords() {
        return new ArrayList<>(missingWords);
    }
}
