package io.github.some_example_name.Simulation;

import io.github.some_example_name.AbstractEngine.EntityManagement.AbstractEntity;
import io.github.some_example_name.AbstractEngine.EntityManagement.EntityManager;
import io.github.some_example_name.AbstractEngine.EntityManagement.Transform;

public class simulatedObject extends AbstractEntity {

    Transform transform;

    @Override
    public void start() {
        setTag("enemy");
        transform = new Transform(0,0,32,32);
        System.out.println("simulated enemy initialized");
        EntityManager em = new EntityManager();

        AbstractEntity player = em.findByTag("player");

        if (player != null) {
            System.out.println("Enemy sees player with tag: " + player.getTag());
        } else {
            System.out.println("Player not found yet");
        }

    }

    @Override
    public void update(float deltaTime) {
    }
}
