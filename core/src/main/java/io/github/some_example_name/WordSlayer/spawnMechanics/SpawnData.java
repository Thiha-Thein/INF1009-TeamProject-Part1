package io.github.some_example_name.WordSlayer.spawnMechanics;

import com.badlogic.gdx.math.Vector2;

/*
 * SpawnData
 *
 * Simple data holder for enemy spawn info.
 */

public class SpawnData {

    private final Vector2 position;
    private final EnemyType enemyType;

    public SpawnData(Vector2 position, EnemyType enemyType) {
        this.position = position;
        this.enemyType = enemyType;
    }

    public Vector2 getPosition() {
        return position;
    }

    public EnemyType getEnemyType() {
        return enemyType;
    }
}
