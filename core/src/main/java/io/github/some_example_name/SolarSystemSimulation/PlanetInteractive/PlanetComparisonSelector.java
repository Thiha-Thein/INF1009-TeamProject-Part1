package io.github.some_example_name.SolarSystemSimulation.PlanetInteractive;

import java.util.List;
import io.github.some_example_name.SolarSystemSimulation.PlanetObj;

// Tracks which planet is currently used as the comparison body in presentation mode
// Wraps around at both ends so the user can cycle through all planets indefinitely with A and D
public class PlanetComparisonSelector {

    private List<PlanetObj> planets;
    private int index = 0; // current position in the planet list

    public PlanetComparisonSelector(List<PlanetObj> planets) {
        this.planets = planets;
    }

    // Advances to the next planet — wraps to index 0 after the last one
    public void next() {
        index++;
        if (index >= planets.size())
            index = 0;
    }

    // Steps back to the previous planet — wraps to the last index when going below 0
    public void previous() {
        index--;
        if (index < 0)
            index = planets.size() - 1;
    }

    // Returns the current comparison planet — automatically skips past the selected planet
    // so the comparison is never "planet vs itself"
    public PlanetObj getPlanet(PlanetObj selected) {

        PlanetObj planet = planets.get(index);

        // If the current index happens to point at the selected planet, advance once to skip it
        if (planet == selected) {

            index++;

            if (index >= planets.size())
                index = 0;

            planet = planets.get(index);
        }

        return planet;
    }
}
