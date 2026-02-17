package io.github.some_example_name.AbstractEngine.CollisionManagement;

import io.github.some_example_name.AbstractEngine.EntityManagement.*;
import java.util.*;

public class CollisionManager {

    private Set<CollisionPair> previousCollisions = new HashSet<>();

    // 🔹 World bounds
    private float worldWidth;
    private float worldHeight;

    // 🔹 Set world boundaries from simulation layer
    public void setWorldBounds(float width, float height) {
        this.worldWidth = width;
        this.worldHeight = height;
    }

    public void checkCollisions(List<AbstractEntity> entities) {

        Set<CollisionPair> currentCollisions = new HashSet<>();

        // =====================================================
        // 🔹 Entity-to-Entity Collision Detection
        // =====================================================
        for (int i = 0; i < entities.size(); i++) {
            for (int j = i + 1; j < entities.size(); j++) {

                AbstractEntity e1 = entities.get(i);
                AbstractEntity e2 = entities.get(j);

                Collider colA = e1.getComponent(Collider.class);
                Collider colB = e2.getComponent(Collider.class);

                if (colA == null || colB == null)
                    continue;

                if (!(e1 instanceof ICollision) ||
                    !(e2 instanceof ICollision))
                    continue;

                ICollision a = (ICollision) e1;
                ICollision b = (ICollision) e2;

                if (colA.isColliding(colB)) {
                    CollisionPair pair = new CollisionPair(a, b);
                    currentCollisions.add(pair);

                    if (!previousCollisions.contains(pair)) {
                        a.onCollisionStart(e2); // Pass AbstractEntity directly!
                        b.onCollisionStart(e1); // Pass AbstractEntity directly!
                    } else {
                        a.onCollisionUpdate(e2);
                        b.onCollisionUpdate(e1);
                    }
                }
            }
        }

        //Collision Exit Detection
        for (CollisionPair oldPair : previousCollisions) {
            if (!currentCollisions.contains(oldPair)) {
                oldPair.a.onCollisionExit((AbstractEntity) oldPair.b);
                oldPair.b.onCollisionExit((AbstractEntity) oldPair.a);
            }
        }

        previousCollisions = currentCollisions;

        //World Boundary Collision (Clamp)
        for (AbstractEntity entity : entities) {
            checkWorldBounds(entity);
        }
    }

    //World Boundary Check
    private void checkWorldBounds(AbstractEntity entity) {

        Transform t = entity.getTransform();
        if (t == null) return;

        float x = t.getX();
        float y = t.getY();
        float width = t.getWidth();
        float height = t.getHeight();

        if (x < 0)
            t.setX(0);

        if (y < 0)
            t.setY(0);

        if (x + width > worldWidth)
            t.setX(worldWidth - width);

        if (y + height > worldHeight)
            t.setY(worldHeight - height);
    }

    //Internal Collision Pair Class
    private static class CollisionPair {

        ICollision a;
        ICollision b;

        CollisionPair(ICollision a, ICollision b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean equals(Object obj) {

            if (!(obj instanceof CollisionPair))
                return false;

            CollisionPair other = (CollisionPair) obj;

            return (a == other.a && b == other.b) ||
                (a == other.b && b == other.a);
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(a) +
                System.identityHashCode(b);
        }
    }
}



