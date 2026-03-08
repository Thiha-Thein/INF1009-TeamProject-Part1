package io.github.some_example_name.AbstractEngine.CollisionManagement;

import io.github.some_example_name.AbstractEngine.EntityManagement.*;

// Contract that an entity must fulfill to participate in the collision system
// Entities that implement this receive three distinct events so they can react
// differently to the moment of contact, sustained overlap, and separation
public interface ICollision {

    // Returns the entity's collider so CollisionManager can perform overlap tests
    Collider getCollider();

    // Fired once on the first frame two entities begin overlapping
    void onCollisionStart(AbstractEntity other);

    // Fired every frame while two entities remain overlapping after the first frame
    void onCollisionUpdate(AbstractEntity other);

    // Fired once on the first frame two entities stop overlapping
    void onCollisionExit(AbstractEntity other);
}
