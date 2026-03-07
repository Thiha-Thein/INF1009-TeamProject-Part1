package io.github.some_example_name.SolarSystemSimulation.PlanetInteractive;

import java.util.List;
import io.github.some_example_name.SolarSystemSimulation.PlanetObj;

/*
Handles which planet is used for comparison.
Skips the currently selected planet.
*/

public class PlanetComparisonSelector {

    private List<PlanetObj> planets;
    private int index = 0;

    public PlanetComparisonSelector(List<PlanetObj> planets) {
        this.planets = planets;
    }

    public void next() {
        index++;
        if (index >= planets.size())
            index = 0;
    }

    public void previous() {
        index--;
        if (index < 0)
            index = planets.size() - 1;
    }

    public PlanetObj getPlanet(PlanetObj selected) {

        PlanetObj planet = planets.get(index);

        // prevent comparing a planet with itself
        if (planet == selected) {

            index++;

            if (index >= planets.size())
                index = 0;

            planet = planets.get(index);
        }

        return planet;
    }
}
