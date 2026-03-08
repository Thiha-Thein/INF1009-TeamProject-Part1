package io.github.some_example_name.SolarSystemSimulation.MiniGames;

// holds all question data that gets loaded from minigames.json
// has two inner classes, one for each minigame type
public class MinigameData {

    // array of all fact or fiction questions
    private FactOrFictionQuestion[] factorfiction;
    // array of all match the planet questions
    private MatchQuestion[] matchtheplanet;

    // returns the fact or fiction questions array
    public FactOrFictionQuestion[] getFactOrFiction() { return factorfiction; }
    // returns the match the planet questions array
    public MatchQuestion[] getMatchThePlanet() { return matchtheplanet; }

    // represents one fact or fiction question with a statement, answer, and explanation
    public static class FactOrFictionQuestion {
        // the sentence the player has to judge as true or false
        private String statement;
        // the correct answer, either "true" or "false"
        private String answer;
        // the explanation shown to the player after they answer
        private String explanation;

        // returns the statement text
        public String getStatement() { return statement; }
        // returns the correct answer string
        public String getAnswer() { return answer; }
        // returns the explanation text
        public String getExplanation() { return explanation; }
    }

    // represents one match the planet question with a clue and the planet it describes
    public static class MatchQuestion {
        // the hint text shown to the player
        private String clue;
        // the name of the planet this clue belongs to
        private String planet;

        // returns the clue text
        public String getClue() { return clue; }
        // returns the correct planet name
        public String getPlanet() { return planet; }
    }
}
