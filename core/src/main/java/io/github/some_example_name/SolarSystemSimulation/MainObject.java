package io.github.some_example_name.SolarSystemSimulation;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.some_example_name.AbstractEngine.EntityManagement.*;
import io.github.some_example_name.AbstractEngine.CollisionManagement.*;
import io.github.some_example_name.AbstractEngine.IOManagement.IOManager;

// Invisible entity that tracks the mouse cursor in world space
// By giving the cursor a Transform and Collider it can trigger collision events on any ICollision entity
// without those entities needing to query mouse position directly
public class MainObject extends AbstractEntity implements ICollision{

    private final IOManager ioManager;
    private final Viewport viewport; // needed to unproject screen-space mouse coords into world-space

    public MainObject(IOManager ioManager, Viewport viewport) {

        this.ioManager = ioManager;
        this.viewport = viewport;
    }

    @Override
    public void start() {

        // Small 4x4 transform acts as the cursor's hitbox — small enough to feel precise
        transform = new Transform(0, 0, 4, 4);

        addComponent(Transform.class, transform);

        Collider collider = new Collider(transform);
        addComponent(Collider.class, collider);

        // Tag is checked by PlanetObj.onCollisionStart() to confirm the colliding entity is the mouse and not another planet
        setTag("mouse");
    }

    @Override
    // Each frame the cursor's transform is moved to the current mouse position in world coordinates
    public void update(float deltaTime) {

        // LibGDX mouse Y is top-down; viewport.unproject converts it to the bottom-up world coordinate system
        Vector2 mouse = viewport.unproject(
            new Vector2(ioManager.getMouseX(), ioManager.getMouseY())
        );

        Transform transform = getTransform();

        transform.setX(mouse.x);
        transform.setY(mouse.y);
    }

    @Override
    public void resize(int width, int height) {}

    // The cursor does not need to expose a collider externally — collision is detected by CollisionManager using the component
    @Override
    public Collider getCollider() {
        return null;
    }

    // Collision callbacks are empty — the cursor itself does not react; only the planet it touches reacts
    @Override
    public void onCollisionStart(AbstractEntity other) {}

    @Override
    public void onCollisionUpdate(AbstractEntity other) {}

    @Override
    public void onCollisionExit(AbstractEntity other) {}
}
