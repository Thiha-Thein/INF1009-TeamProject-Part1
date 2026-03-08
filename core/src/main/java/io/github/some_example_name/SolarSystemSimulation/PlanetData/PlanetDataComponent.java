package io.github.some_example_name.SolarSystemSimulation.PlanetData;

import java.util.Map;

// Entity component that carries a planet's educational content
// Stored as a component so PlanetFactsPanel can access it via getComponent() without knowing about PlanetObj
public class PlanetDataComponent {

    private final String description;
    private final PlanetData.Fact[] facts;
    private final Map<String,String> stats;

    public PlanetDataComponent(String description,
                               PlanetData.Fact[] facts,
                               Map<String,String> stats) {

        this.description = description;
        this.facts = facts;
        this.stats = stats;
    }

    public String getDescription() { return description; }
    public PlanetData.Fact[] getFacts() { return facts; }
    public Map<String,String> getStats() { return stats; }
}
