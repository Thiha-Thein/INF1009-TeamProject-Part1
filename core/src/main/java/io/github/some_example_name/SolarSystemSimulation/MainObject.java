package io.github.some_example_name.SolarSystemSimulation;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.some_example_name.AbstractEngine.EntityManagement.*;
import io.github.some_example_name.AbstractEngine.CollisionManagement.*;
import io.github.some_example_name.AbstractEngine.IOManagement.IOManager;

public class MainObject extends AbstractEntity implements ICollision{

    private final IOManager ioManager;
    private final Viewport viewport;

    public MainObject(IOManager ioManager, Viewport viewport) {

        this.ioManager = ioManager;
        this.viewport = viewport;
    }

    @Override
    public void start() {

        // Create transform for this entity
        transform = new Transform(0, 0, 4, 4);

        // Register transform as component
        addComponent(Transform.class, transform);

        // Create collider using the same transform
        Collider collider = new Collider(transform);

        addComponent(Collider.class, collider);

        // Tag used to identify the mouse collider
        setTag("mouse");
    }

    @Override
    public void update(float deltaTime) {

        // Convert mouse screen position into world coordinates
        Vector2 mouse = viewport.unproject(
            new Vector2(ioManager.getMouseX(), ioManager.getMouseY())
        );

        Transform transform = getTransform();

        transform.setX(mouse.x);
        transform.setY(mouse.y);
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public Collider getCollider() {
        return null;
    }

    @Override
    public void onCollisionStart(AbstractEntity other) {

    }

    @Override
    public void onCollisionUpdate(AbstractEntity other) {

    }

    @Override
    public void onCollisionExit(AbstractEntity other) {

    }
}
