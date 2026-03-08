package io.github.some_example_name.AbstractEngine.CollisionManagement;

import io.github.some_example_name.AbstractEngine.EntityManagement.*;
import java.util.*;

// Runs AABB collision detection for all entities each frame and dispatches enter/update/exit events
// World boundary clamping is also handled here so entities cannot leave the playable area
public class CollisionManager {

    // Tracks pairs that were colliding last frame to detect enter and exit transitions
    private Set<CollisionPair> previousCollisions = new HashSet<>();

    private float worldWidth;
    private float worldHeight;

    // Called by the simulation layer when the viewport changes so boundary clamping uses the correct dimensions
    public void setWorldBounds(float width, float height) {
        this.worldWidth = width;
        this.worldHeight = height;
    }

    public void checkCollisions(List<AbstractEntity> entities) {

        Set<CollisionPair> currentCollisions = new HashSet<>();

        // Iterate every unique pair once using j > i to avoid checking the same pair twice
        for (int i = 0; i < entities.size(); i++) {
            for (int j = i + 1; j < entities.size(); j++) {

                AbstractEntity e1 = entities.get(i);
                AbstractEntity e2 = entities.get(j);

                Collider colA = e1.getComponent(Collider.class);
                Collider colB = e2.getComponent(Collider.class);

                // Skip any entity that lacks a collider component
                if (colA == null || colB == null)
                    continue;

                // Both entities must implement ICollision to receive events — silently skip mixed pairs
                if (!(e1 instanceof ICollision) ||
                    !(e2 instanceof ICollision))
                    continue;

                ICollision a = (ICollision) e1;
                ICollision b = (ICollision) e2;

                if (colA.isColliding(colB)) {
                    CollisionPair pair = new CollisionPair(a, b);
                    currentCollisions.add(pair);

                    if (!previousCollisions.contains(pair)) {
                        // Pair is new this frame — fire the enter event on both sides
                        a.onCollisionStart(e2);
                        b.onCollisionStart(e1);
                    } else {
                        // Pair was already overlapping last frame — fire the continuous update event
                        a.onCollisionUpdate(e2);
                        b.onCollisionUpdate(e1);
                    }
                }
            }
        }

        // Any pair that was colliding last frame but not this frame has just separated
        for (CollisionPair oldPair : previousCollisions) {
            if (!currentCollisions.contains(oldPair)) {
                oldPair.a.onCollisionExit((AbstractEntity) oldPair.b);
                oldPair.b.onCollisionExit((AbstractEntity) oldPair.a);
            }
        }

        // Swap sets — currentCollisions becomes the baseline for next frame's enter/exit detection
        previousCollisions = currentCollisions;

        // Clamp all entities inside world bounds after movement has been applied this frame
        for (AbstractEntity entity : entities) {
            checkWorldBounds(entity);
        }
    }

    // Clamps entity position so it cannot move outside the configured world boundaries
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

    // Represents an unordered pair of colliding entities — order-independent equals/hashCode
    // ensures that (A, B) and (B, A) are treated as the same collision
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

            // Treat (A,B) as equal to (B,A) so direction of overlap does not create duplicate events
            return (a == other.a && b == other.b) ||
                (a == other.b && b == other.a);
        }

        @Override
        public int hashCode() {
            // Commutative hash — addition ensures (A,B) and (B,A) produce the same bucket
            return System.identityHashCode(a) +
                System.identityHashCode(b);
        }
    }
}
