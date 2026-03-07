package io.github.some_example_name.SolarSystemSimulation.PlanetData;

import java.util.Map;

// Represents one planet entry from planets.json
public class PlanetData {

    private String name;
    private String description;

    // facts are now objects, not plain strings
    private Fact[] facts;

    private Map<String, String> stats;

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Fact[] getFacts() { return facts; }
    public Map<String,String> getStats() { return stats; }

    // small inner class representing a fact card
    public static class Fact {

        private String title;
        private String text;

        public String getTitle() { return title; }
        public String getText() { return text; }
    }
}
