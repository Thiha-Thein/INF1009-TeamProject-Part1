package io.github.some_example_name.SolarSystemSimulation;

import io.github.some_example_name.AbstractEngine.EntityManagement.*;
import io.github.some_example_name.AbstractEngine.CollisionManagement.*;
import io.github.some_example_name.AbstractEngine.MovementManagement.MovementComponent;
import io.github.some_example_name.SolarSystemSimulation.PlanetData.PlanetData;
import io.github.some_example_name.SolarSystemSimulation.PlanetData.PlanetDataComponent;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class PlanetObj extends AbstractEntity implements ICollision {

    private final String name;
    private final float mass;
    private final float size;
    private float diameter;
    private final String spritePath;
    private final AbstractEntity parent;

    private boolean baseScaleCaptured = false;

    private final PlanetData planetData;

    // Original scale of the planet sprite
    private float baseScale = 1f;

    private OrbitalComponent orbit;
    private Collider collider;

    // Tracks whether mouse collider is currently overlapping
    private boolean hovered = false;

    public PlanetObj(String name, float mass, float size, String spritePath,
                     AbstractEntity parent, PlanetData planetData) {

        this.name = name;
        this.mass = mass;
        this.size = size;
        this.spritePath = spritePath;
        this.parent = parent;
        this.planetData = planetData;
    }

    public void pauseOrbit() {
        if (orbit != null)
            orbit.pause();
    }

    public void resumeOrbit() {
        if (orbit != null)
            orbit.resume();
    }

    public float getDiameter() {
        return diameter;
    }

    public void setInitialPosition(float x, float y) {
        transform = new Transform(x, y, size, size);
    }

    public void setOrbit(float radiusX, float radiusY, float speed,
                         float tiltDegrees, float startAngle) {

        orbit = new OrbitalComponent(parent, radiusX, radiusY, speed, tiltDegrees, startAngle);

        addComponent(OrbitalComponent.class, orbit);
    }

    public void drawOrbit(ShapeRenderer shapeRenderer) {

        if (orbit == null)
            return;

        Transform pt = orbit.getParent().getTransform();

        float cx = pt.getX() + pt.getWidth() / 2f;
        float cy = pt.getY() + pt.getHeight() / 2f;

        shapeRenderer.identity();

        shapeRenderer.translate(cx, cy, 0);
        shapeRenderer.rotate(0, 0, 1, orbit.getTiltDegrees());

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

        if (transform == null) {
            transform = new Transform(0, 0, size, size);
        }

        collider = new Collider(transform);
        addComponent(Collider.class, collider);

        AnimationRenderer ar = new AnimationRenderer();

        ar.addAnimation("spin", spritePath, 30, 8, 0.08f, true);

        setAnimationRenderer(ar);
        addComponent(AnimationRenderer.class, ar);

        if (planetData != null) {

            addComponent(
                PlanetDataComponent.class,
                new PlanetDataComponent(
                    planetData.getDescription(),
                    planetData.getFacts(),
                    planetData.getStats()
                )
            );
        }

        if (orbit != null) {

            MovementComponent movement = new MovementComponent(transform, 0);

            addComponent(MovementComponent.class, movement);

            orbit.setMovement(movement);
        }

    }

    @Override
    public void update(float deltaTime) {

        if (orbit != null)
            orbit.update(this, deltaTime);
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public Collider getCollider() {
        return collider;
    }

    // Mouse first touches the planet collider
    @Override
    public void onCollisionStart(AbstractEntity other) {
        if ("mouse".equals(other.getTag()) && !hovered) {
            hovered = true;
            AnimationRenderer renderer = getAnimationRenderer();
            // Capture base scale once
            if (!baseScaleCaptured) {
                baseScale = renderer.getScale();
                baseScaleCaptured = true;
            }
            renderer.setScale(baseScale * 1.15f);
        }
    }

    // Mouse stays overlapping planet collider
    @Override
    public void onCollisionUpdate(AbstractEntity other) {
        if ("mouse".equals(other.getTag())) {
            hovered = true;
        }
    }

    // Mouse leaves planet collider
    @Override
    public void onCollisionExit(AbstractEntity other) {
        if ("mouse".equals(other.getTag())) {
            hovered = false;
            // Reset scale when mouse leaves
            getAnimationRenderer().setScale(baseScale);
        }
    }

    // Allows map to check if mouse is currently touching this planet
    public boolean isMouseOver() {
        return hovered;
    }

    public String getPlanetName() {
        return name;
    }

    public float getBaseScale() {
        return baseScale;
    }
}
