package io.github.some_example_name.SolarSystemSimulation;

import com.badlogic.gdx.math.MathUtils;

import io.github.some_example_name.AbstractEngine.EntityManagement.AbstractEntity;
import io.github.some_example_name.AbstractEngine.EntityManagement.Transform;
import io.github.some_example_name.AbstractEngine.MovementManagement.MovementComponent;

// Drives elliptical orbit movement for a planet around a parent entity
// Each frame the component advances the orbit angle and pushes the resulting world position to MovementComponent
// The orbit shape can be tilted to create the inclined-plane visual effect seen in the simulation
public class OrbitalComponent {

    private final AbstractEntity parent; // the body being orbited — the planet's position is calculated relative to this
    private final float radiusX;         // half-width of the orbit ellipse in world units
    private final float radiusY;         // half-height of the orbit ellipse in world units
    private final float speed;           // angular velocity in radians per second
    private final float tilt;            // orbit plane tilt in radians, applied as a 2D rotation
    private float angle;                 // current angular position in the orbit, in radians

    private boolean paused = false; // when true, angle does not advance — used during presentation mode

    // MovementComponent is injected after construction because PlanetObj creates it during start()
    private MovementComponent movement;


    public OrbitalComponent(AbstractEntity parent,
                            float radiusX,
                            float radiusY,
                            float speed,
                            float tiltDegrees,
                            float startAngle) {

        this.parent = parent;
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.speed = speed;
        this.tilt = tiltDegrees * MathUtils.degreesToRadians; // store in radians to avoid converting every frame
        this.angle = startAngle * MathUtils.degreesToRadians; // each planet starts at a different point on its orbit
    }


    // Called by PlanetObj.start() once the MovementComponent has been created and added as a component
    public void setMovement(MovementComponent movement) {
        this.movement = movement;
    }


    // Advances the orbit each frame and repositions the entity by updating its MovementComponent
    public void update(AbstractEntity self, float deltaTime) {

        if (parent == null || movement == null)
            return;

        if (paused)
            return;

        angle += speed * deltaTime; // advance angle at a rate proportional to elapsed time

        Transform pt = parent.getTransform();

        // Center-point of the parent body — planets orbit around this, not around the parent's bottom-left origin
        float cx = pt.getX() + pt.getWidth() / 2f;
        float cy = pt.getY() + pt.getHeight() / 2f;

        // Unrotated ellipse position at the current angle
        float ex = radiusX * MathUtils.cos(angle);
        float ey = radiusY * MathUtils.sin(angle);

        // Apply the orbit tilt as a 2D rotation matrix to simulate inclined orbital planes
        float tx = ex * MathUtils.cos(tilt) - ey * MathUtils.sin(tilt);
        float ty = ex * MathUtils.sin(tilt) + ey * MathUtils.cos(tilt);

        Transform st = self.getTransform();

        // Offset by half the planet's size so the planet centers on the orbit path rather than its bottom-left
        float targetX = cx + tx - st.getWidth() / 2f;
        float targetY = cy + ty - st.getHeight() / 2f;

        movement.moveTo(targetX, targetY);
    }


    // Stops the orbit angle from advancing — used when a planet is selected for presentation
    public void pause() {
        paused = true;
    }

    // Resumes orbit movement after returning from presentation mode
    public void resume() {
        paused = false;
    }


    public AbstractEntity getParent() {
        return parent;
    }

    public float getRadiusX() {
        return radiusX;
    }

    public float getRadiusY() {
        return radiusY;
    }

    // Returns the tilt converted back to degrees — used by PlanetObj.drawOrbit() to rotate the ShapeRenderer
    public float getTiltDegrees() {
        return tilt * MathUtils.radiansToDegrees;
    }
}
