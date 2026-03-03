package io.github.some_example_name.SolarSystemSimulation;

import com.badlogic.gdx.math.MathUtils;
import io.github.some_example_name.AbstractEngine.EntityManagement.*;

public class orbitalComponent {

    private final AbstractEntity parent;
    private final float radiusX;
    private final float radiusY;
    private final float speed;
    private final float tilt;
    private float angle;

    public orbitalComponent(AbstractEntity parent,
                            float radiusX,
                            float radiusY,
                            float speed,
                            float tiltDegrees,
                            float startAngle) {

        this.parent = parent;
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.speed = speed;
        this.tilt = tiltDegrees * MathUtils.degreesToRadians;
        this.angle = startAngle * MathUtils.degreesToRadians;
    }

    public void update(AbstractEntity self, float deltaTime) {
        if (parent == null) return;

        //Angle increment
        angle += speed * deltaTime;

        Transform pt = parent.getTransform();
        float cx = pt.getX() + pt.getWidth() / 2f;
        float cy = pt.getY() + pt.getHeight() / 2f;

        // Ellipse parametric equation
        float ex = radiusX * MathUtils.cos(angle);
        float ey = radiusY * MathUtils.sin(angle);

        //Rotation matrix
        float tx = ex * MathUtils.cos(tilt) - ey * MathUtils.sin(tilt);
        float ty = ex * MathUtils.sin(tilt) + ey * MathUtils.cos(tilt);

        Transform st = self.getTransform();
        st.setX(cx + tx - st.getWidth() / 2f);
        st.setY(cy + ty - st.getHeight() / 2f);
    }

    public AbstractEntity getParent() { return parent; }
    public float getRadiusX() { return radiusX; }
    public float getRadiusY() { return radiusY; }
    public float getTiltDegrees() {
        return tilt * MathUtils.radiansToDegrees;
    }
}
