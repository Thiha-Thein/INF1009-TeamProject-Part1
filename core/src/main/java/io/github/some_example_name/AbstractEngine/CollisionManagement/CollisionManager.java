package io.github.some_example_name.AbstractEngine.CollisionManagement;

import io.github.some_example_name.AbstractEngine.EntityManagement.*;
import java.util.*;

public class CollisionManager {

    private Set<CollisionPair> previousCollisions = new HashSet<>();

    public void checkCollisions(List<AbstractEntity> entities) {

        Set<CollisionPair> currentCollisions = new HashSet<>();

        for (int i = 0; i < entities.size(); i++) {
            for (int j = i + 1; j < entities.size(); j++) {

                AbstractEntity e1 = entities.get(i);
                AbstractEntity e2 = entities.get(j);

                // 🔹 Component-based detection
                Collider colA = e1.getComponent(Collider.class);
                Collider colB = e2.getComponent(Collider.class);

                if (colA == null || colB == null)
                    continue;

                // 🔹 Unity-style behaviour (must implement ICollision)
                if (!(e1 instanceof ICollision) ||
                    !(e2 instanceof ICollision))
                    continue;

                ICollision a = (ICollision) e1;
                ICollision b = (ICollision) e2;

                if (colA.isColliding(colB)) {

                    CollisionPair pair = new CollisionPair(a, b);
                    currentCollisions.add(pair);

                    if (!previousCollisions.contains(pair)) {
                        a.onCollisionStart(b);
                        b.onCollisionStart(a);
                    } else {
                        a.onCollisionUpdate(b);
                        b.onCollisionUpdate(a);
                    }
                }
            }
        }

        // 🔹 Handle Exit
        for (CollisionPair oldPair : previousCollisions) {
            if (!currentCollisions.contains(oldPair)) {
                oldPair.a.onCollisionExit(oldPair.b);
                oldPair.b.onCollisionExit(oldPair.a);
            }
        }

        previousCollisions = currentCollisions;
    }

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


