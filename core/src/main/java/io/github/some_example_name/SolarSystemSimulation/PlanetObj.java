package io.github.some_example_name.SolarSystemSimulation;

import io.github.some_example_name.AbstractEngine.EntityManagement.*;
import io.github.some_example_name.AbstractEngine.CollisionManagement.*;
import io.github.some_example_name.AbstractEngine.MovementManagement.MovementComponent;
import io.github.some_example_name.SolarSystemSimulation.PlanetData.PlanetData;
import io.github.some_example_name.SolarSystemSimulation.PlanetData.PlanetDataComponent;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

// Represents a single planet (or the Sun) in the simulation
// Handles its own orbit, animation, collision with the mouse cursor, and hover scale effect
public class PlanetObj extends AbstractEntity implements ICollision {

    private final String name;
    private final float mass;
    private final float size;   // base render size in world units
    private float diameter;
    private final String spritePath;
    private final AbstractEntity parent; // null for the Sun, or the Sun entity for all orbiting planets

    // Tracks whether baseScale has been read from the renderer yet — captured once to avoid overwriting with scaled value
    private boolean baseScaleCaptured = false;

    private final PlanetData planetData; // educational data loaded from JSON, attached as a component during start()

    private float baseScale = 1f; // the renderer scale before any hover enlargement is applied

    private OrbitalComponent orbit;
    private Collider collider;

    // True while the mouse cursor's collider overlaps this planet's collider
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

    // Sets an explicit starting position before start() is called — used during initialization
    // to place planets at the center before their first orbit update positions them correctly
    public void setInitialPosition(float x, float y) {
        transform = new Transform(x, y, size, size);
    }

    // Configures the orbital parameters — must be called before start() or the planet will not orbit
    public void setOrbit(float radiusX, float radiusY, float speed,
                         float tiltDegrees, float startAngle) {

        orbit = new OrbitalComponent(parent, radiusX, radiusY, speed, tiltDegrees, startAngle);

        addComponent(OrbitalComponent.class, orbit);
    }

    // Draws the orbit ellipse using the provided ShapeRenderer — called by SolarSystemMap before the planet itself is rendered
    public void drawOrbit(ShapeRenderer shapeRenderer) {

        if (orbit == null)
            return;

        Transform pt = orbit.getParent().getTransform();

        float cx = pt.getX() + pt.getWidth() / 2f;
        float cy = pt.getY() + pt.getHeight() / 2f;

        // Translate to parent center and rotate by tilt so the ellipse is drawn in the orbit's inclined plane
        shapeRenderer.identity();
        shapeRenderer.translate(cx, cy, 0);
        shapeRenderer.rotate(0, 0, 1, orbit.getTiltDegrees());

        shapeRenderer.ellipse(
            -orbit.getRadiusX(),
            -orbit.getRadiusY(),
            orbit.getRadiusX() * 2f,
            orbit.getRadiusY() * 2f,
            120 // segment count — higher values produce a smoother ellipse
        );

        shapeRenderer.identity(); // reset transform so subsequent draws are not affected
    }

    @Override
    public void start() {

        setTag(name); // tag matches the planet name so collision callbacks and searches can identify it

        // Transform may have been set by setInitialPosition() — only create a default one if it was not
        if (transform == null) {
            transform = new Transform(0, 0, size, size);
        }

        collider = new Collider(transform);
        addComponent(Collider.class, collider);

        // Load the spinning animation from a 30x8 sprite sheet at 0.08s per frame
        AnimationRenderer ar = new AnimationRenderer();
        ar.addAnimation("spin", spritePath, 30, 8, 0.08f, true);

        setAnimationRenderer(ar);
        addComponent(AnimationRenderer.class, ar);

        // Attach educational data as a component so the UI panel can read it without casting to PlanetObj
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

        // Orbiting planets need a MovementComponent so OrbitalComponent can call moveTo() each frame
        if (orbit != null) {

            MovementComponent movement = new MovementComponent(transform, 0); // speed is 0 because orbital math bypasses direction-based movement

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

    // Fires when the mouse cursor first overlaps this planet — enlarges the sprite slightly as a hover cue
    @Override
    public void onCollisionStart(AbstractEntity other) {
        if ("mouse".equals(other.getTag()) && !hovered) {
            hovered = true;
            AnimationRenderer renderer = getAnimationRenderer();
            // Capture the original scale once so we can restore it accurately on exit
            if (!baseScaleCaptured) {
                baseScale = renderer.getScale();
                baseScaleCaptured = true;
            }
            renderer.setScale(baseScale * 1.15f); // 15% enlargement gives clear hover feedback without feeling jarring
        }
    }

    // Fires every frame the cursor stays overlapping — keeps hovered true so onCollisionExit can reset it
    @Override
    public void onCollisionUpdate(AbstractEntity other) {
        if ("mouse".equals(other.getTag())) {
            hovered = true;
        }
    }

    // Fires when the cursor leaves — restores the planet to its original scale
    @Override
    public void onCollisionExit(AbstractEntity other) {
        if ("mouse".equals(other.getTag())) {
            hovered = false;
            getAnimationRenderer().setScale(baseScale);
        }
    }

    // Checked by SolarSystemMap each frame to determine if a left-click should trigger presentation mode
    public boolean isMouseOver() {
        return hovered;
    }

    public String getPlanetName() {
        return name;
    }

    // Returns the pre-hover scale so presentation mode can restore it after enlarging the planet during display
    public float getBaseScale() {
        return baseScale;
    }
}
