package io.github.some_example_name.AbstractEngine.CollisionManagement;

import io.github.some_example_name.AbstractEngine.EntityManagement.*;

public interface ICollision {
    Collider getCollider();
    void onCollisionStart(AbstractEntity other);
    void onCollisionUpdate(AbstractEntity other);
    void onCollisionExit(AbstractEntity other);
}

