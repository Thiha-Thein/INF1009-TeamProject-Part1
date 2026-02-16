package io.github.some_example_name.AbstractEngine.CollisionManagement;

public interface ICollision {

    Collider getCollider();

    void onCollisionStart(ICollision other);
    void onCollisionUpdate(ICollision other);
    void onCollisionExit(ICollision other);
}

