package io.github.some_example_name.AbstractEngine;

import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.AbstractEngine.EntityManagement.*;
import io.github.some_example_name.AbstractEngine.MovementManagement.*;
import io.github.some_example_name.AbstractEngine.CollisionManagement.*;

public class simulatedObject extends AbstractEntity implements ICollision {

    private MovementComponent movement;
    private SpriteRenderer spriteRenderer;
    private Collider collider;
    private ballObj ball; // Reference to the ball to chase

    public simulatedObject(ballObj ball) {
        this.ball = ball;
    }

    @Override
    public void start() {
<<<<<<< Updated upstream:OOPPorject/core/src/main/java/io/github/some_example_name/AbstractEngine/simulatedObject.java
        setTag("enemy");
        transform = new Transform(0,0,32,32);
        System.out.println("simulated enemy initialized");
        EntityManager em = EntityManager.getInstance();
=======
        setTag("ai");
        transform = new Transform(0, 500, 128, 128); // X will be set in resize()
>>>>>>> Stashed changes:core/src/main/java/io/github/some_example_name/Simulation/simulatedObject.java

        collider = new Collider(transform);
        addComponent(Collider.class, collider);

        movement = new MovementComponent(transform, 500f);
        addComponent(MovementComponent.class, movement);

        spriteRenderer = new SpriteRenderer("character/virtualman.png");
        setSpriteRenderer(spriteRenderer);
    }

    public void resize(int width, int height) {
        float size = width * 0.1f;
        transform.setWidth(size);
        transform.setHeight(size);

        // Push AI further from the right wall
        transform.setX(width - 300 - size); // Extra size margin from wall

        float baseSpeed = 900f;
        float referenceWidth = 1920f;
        float scaledSpeed = baseSpeed * (width / referenceWidth);

        movement.setSpeed(scaledSpeed);
    }
    @Override
    public void update(float deltaTime) {
        if (ball == null) return;

        float aiCenterY = transform.getY() + transform.getHeight() / 2f;
        float ballCenterY = ball.getTransform().getY() + ball.getTransform().getHeight() / 2f;
        float ballCenterX = ball.getTransform().getX() + ball.getTransform().getWidth() / 2f;
        float aiCenterX = transform.getX() + transform.getWidth() / 2f;

        float distance = Math.abs(ballCenterX - aiCenterX);

        // Only react if ball is moving TOWARDS AI (negative X = moving right to left... wait)
        // AI is on the RIGHT side, so ball moving towards AI = positive X velocity
        if (ball.getVelocity().x > 0 && distance < 400f) {
            float speedMultiplier = distance / 400f;
            movement.setSpeed(900f * speedMultiplier);

            float y = Math.max(-1, Math.min(1, (ballCenterY - aiCenterY) / 100f));
            movement.setDirection(new Vector2(0, y));
        } else {
            // Ball moving away, stop moving
            movement.setSpeed(900f);
            movement.setDirection(new Vector2(0, 0));
        }
    }

    // Implement ICollision interface
    @Override
    public Collider getCollider() {
        return collider;
    }

    @Override
    public void onCollisionStart(AbstractEntity other) {
        System.out.println("AI paddle hit ball!");
    }

    @Override
    public void onCollisionUpdate(AbstractEntity other) {}

    @Override
    public void onCollisionExit(AbstractEntity other) {}

    public MovementComponent getMovement() {
        return movement;
    }

    @Override
    public void render() {

    }
}
