package io.github.some_example_name.SolarSystemSimulation.PlanetData;

import java.util.List;

// Top level wrapper — mirrors the root object in planets.json
// Gson maps the "planets" array directly to this list
public class PlanetDataList {
    private List<PlanetData> planets;
    public List<PlanetData> getPlanets() { return planets; }
}
