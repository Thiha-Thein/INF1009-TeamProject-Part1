package io.github.some_example_name.SolarSystemSimulation.MiniGames;

import com.badlogic.gdx.Gdx;
import com.google.gson.Gson;

// loads and caches question data from minigames.json
// only reads the file once, then reuses the cached result on every future call
public class MinigameDataLoader {

    // cached data — stays null until the first load
    private static MinigameData data;

    // reads minigames.json and parses it into a MinigameData object
    // skips loading if data is already cached
    private static void ensureDataLoaded() {
        if (data != null) return;
        // read the json file as a plain string from the assets folder
        String json = Gdx.files.internal("minigames.json").readString();
        // use Gson to convert the json string into a MinigameData object
        data = new Gson().fromJson(json, MinigameData.class);
    }

    // returns all fact or fiction questions, loading from file first if needed
    public static MinigameData.FactOrFictionQuestion[] getFactOrFiction() {
        ensureDataLoaded();
        return data.getFactOrFiction();
    }

    // returns all match the planet questions, loading from file first if needed
    public static MinigameData.MatchQuestion[] getMatchThePlanet() {
        ensureDataLoaded();
        return data.getMatchThePlanet();
    }
}
