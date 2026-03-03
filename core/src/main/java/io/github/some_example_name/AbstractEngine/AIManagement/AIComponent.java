package io.github.some_example_name.AbstractEngine.AIManagement;

import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.AbstractEngine.EntityManagement.*;

public class AIComponent {

    private AbstractEntity target;
    private final Vector2 lookDirection = new Vector2();

    // Called once when the entity starts, not every entity needs it.
    public void start(AbstractEntity self) {}

    public void setTarget(AbstractEntity target) {
        this.target = target;
    }

    public void update(AbstractEntity self, float deltaTime) {
        if (target == null) return;

        float selfCenterX = self.getTransform().getX() + self.getTransform().getWidth() / 2f;
        float selfCenterY = self.getTransform().getY() + self.getTransform().getHeight() / 2f;

        float targetCenterX = target.getTransform().getX() + target.getTransform().getWidth() / 2f;
        float targetCenterY = target.getTransform().getY() + target.getTransform().getHeight() / 2f;

        lookDirection.set(
            targetCenterX - selfCenterX,
            targetCenterY - selfCenterY
        ).nor();
    }

    public Vector2 getLookDirection() {
        return lookDirection;
    }
}
