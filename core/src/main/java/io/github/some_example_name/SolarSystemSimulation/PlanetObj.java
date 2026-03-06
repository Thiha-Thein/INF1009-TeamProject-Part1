package io.github.some_example_name.SolarSystemSimulation;

import io.github.some_example_name.AbstractEngine.CollisionManagement.*;
import io.github.some_example_name.AbstractEngine.EntityManagement.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
public class PlanetObj extends AbstractEntity implements ICollision {

    private final String name;
    private final float mass;
    private final float size;
    private final String spritePath;
    private final AbstractEntity parent;

    private orbitalComponent orbit;
    private Collider collider;

    public PlanetObj(String name, float mass, float size, String spritePath, AbstractEntity parent) {
        this.name = name;
        this.mass = mass;
        this.size = size;
        this.spritePath = spritePath;
        this.parent = parent;
    }

    public void setInitialPosition(float x, float y) {
        transform = new Transform(x, y, size, size);
    }

    // Call this BEFORE addEntity
    public void setOrbit(float radiusX, float radiusY, float speed,
                         float tiltDegrees, float startAngle) {
        orbit = new orbitalComponent(parent, radiusX, radiusY, speed, tiltDegrees, startAngle);
        addComponent(orbitalComponent.class, orbit);
    }

    public void drawOrbit(ShapeRenderer shapeRenderer) {

        if (orbit == null) return;

        Transform pt = orbit.getParent().getTransform();

        float cx = pt.getX() + pt.getWidth() / 2f;
        float cy = pt.getY() + pt.getHeight() / 2f;

        shapeRenderer.identity();

        // move origin to parent center
        shapeRenderer.translate(cx, cy, 0);

        // rotate ellipse (tilt)
        shapeRenderer.rotate(0, 0, 1, orbit.getTiltDegrees());

        // draw centered ellipse
        shapeRenderer.ellipse(
            -orbit.getRadiusX(),
            -orbit.getRadiusY(),
            orbit.getRadiusX() * 2f,
            orbit.getRadiusY() * 2f,
            120
        );

        shapeRenderer.identity();
    }

    @Override
    public void start() {
        setTag(name);

        // fallback if setInitialPosition was never called
        if (transform == null) {
            transform = new Transform(0, 0, size, size);
        }

        collider = new Collider(transform);
        addComponent(Collider.class, collider);

        AnimationRenderer ar = new AnimationRenderer();
        ar.addAnimation("spin", spritePath, 30, 8, 0.08f, true);

        setAnimationRenderer(ar);
        addComponent(AnimationRenderer.class, ar); // <-- ADD THIS LINE
    }

    @Override
    public void update(float deltaTime) {
        if (orbit != null) {
            orbit.update(this, deltaTime);
        }
    }

    @Override public void resize(int width, int height) {}
    @Override public Collider getCollider() { return collider; }
    @Override public void onCollisionStart(AbstractEntity other) {}
    @Override public void onCollisionUpdate(AbstractEntity other) {}
    @Override public void onCollisionExit(AbstractEntity other) {}
    public String getPlanetName() { return name; }
    public float getMass() { return mass; }
}
