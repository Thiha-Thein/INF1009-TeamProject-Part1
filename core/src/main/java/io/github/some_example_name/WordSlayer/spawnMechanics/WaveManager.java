package io.github.some_example_name.WordSlayer.spawnMechanics;

import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WaveManager {

    private int currentWave = 0;
    private boolean waveActive = false;

    private float mapWidth;
    private float mapHeight;

    private final Random random = new Random();

    public void setMapBounds(float width, float height) {
        this.mapWidth = width;
        this.mapHeight = height;
    }

    public List<SpawnData> startWave() {
        if (currentWave >= 4) {
            return new ArrayList<>();
        }
        currentWave++;
        waveActive = true;
        List<SpawnData> spawns = new ArrayList<>();

        switch (currentWave) {

            case 1:
                spawn(spawns, 5, EnemyType.BAT);
                break;

            case 2:
                spawn(spawns, 3, EnemyType.BAT);
                spawn(spawns, 3, EnemyType.GOBLIN);
                break;

            case 3:
                spawn(spawns, 3, EnemyType.BAT);
                spawn(spawns, 3, EnemyType.GOBLIN);
                spawn(spawns, 2, EnemyType.SKELETON);
                break;

            case 4:
                spawn(spawns, 1, EnemyType.BOSS);
                break;
        }
        return spawns;
    }

    private void spawn(List<SpawnData> list, int count, EnemyType type) {
        for (int i = 0; i < count; i++) {
            list.add(new SpawnData(getRandomSpawnPosition(), type));
        }
    }

    public void endWave() {
        waveActive = false;
    }

    public boolean isWaveActive() {
        return waveActive;
    }

    public int getCurrentWave() {
        return currentWave;
    }

    private Vector2 getRandomSpawnPosition() {

        int side = random.nextInt(4);
        float x = 0;
        float y = 0;

        switch (side) {
            case 0: // top
                x = random.nextFloat() * mapWidth;
                y = mapHeight;
                break;

            case 1: // bottom
                x = random.nextFloat() * mapWidth;
                y = 0;
                break;

            case 2: // left
                x = 0;
                y = random.nextFloat() * mapHeight;
                break;

            case 3: // right
                x = mapWidth;
                y = random.nextFloat() * mapHeight;
                break;
        }
        return new Vector2(x, y);
    }
}
