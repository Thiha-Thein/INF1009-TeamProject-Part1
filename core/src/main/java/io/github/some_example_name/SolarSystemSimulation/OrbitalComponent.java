package io.github.some_example_name.SolarSystemSimulation;

import com.badlogic.gdx.math.MathUtils;

import io.github.some_example_name.AbstractEngine.EntityManagement.AbstractEntity;
import io.github.some_example_name.AbstractEngine.EntityManagement.Transform;
import io.github.some_example_name.AbstractEngine.MovementManagement.MovementComponent;

public class OrbitalComponent {

    private final AbstractEntity parent;
    private final float radiusX;
    private final float radiusY;
    private final float speed;
    private final float tilt;
    private float angle;

    // Stops orbit updates when paused
    private boolean paused = false;

    // Movement system used to apply calculated position
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

        // Convert tilt to radians
        this.tilt = tiltDegrees * MathUtils.degreesToRadians;

        // Convert starting angle to radians
        this.angle = startAngle * MathUtils.degreesToRadians;
    }


    // Inject movement component used to move the entity
    public void setMovement(MovementComponent movement) {

        this.movement = movement;
    }


    // Update orbital position each frame
    public void update(AbstractEntity self, float deltaTime) {

        if (parent == null || movement == null)
            return;

        if (paused)
            return;

        // Advance orbit angle
        angle += speed * deltaTime;

        Transform pt = parent.getTransform();

        float cx = pt.getX() + pt.getWidth() / 2f;
        float cy = pt.getY() + pt.getHeight() / 2f;

        // Calculate ellipse position
        float ex = radiusX * MathUtils.cos(angle);
        float ey = radiusY * MathUtils.sin(angle);

        // Apply orbit tilt
        float tx = ex * MathUtils.cos(tilt) - ey * MathUtils.sin(tilt);
        float ty = ex * MathUtils.sin(tilt) + ey * MathUtils.cos(tilt);

        Transform st = self.getTransform();

        float targetX = cx + tx - st.getWidth() / 2f;
        float targetY = cy + ty - st.getHeight() / 2f;

        // Apply movement
        movement.moveTo(targetX, targetY);
    }


    public void pause() {
        paused = true;
    }

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

    public float getTiltDegrees() {
        return tilt * MathUtils.radiansToDegrees;
    }
}
