package io.github.some_example_name.Simulation;

import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.AbstractEngine.EntityManagement.*;
import io.github.some_example_name.AbstractEngine.CollisionManagement.*;

public class ballObj extends AbstractEntity implements ICollision {

    private Vector2 velocity;
    private Collider collider;
    private SpriteRenderer spriteRenderer;
    private boolean hasBouncedOffPaddle = false;

    private float worldWidth;
    private float worldHeight;

    public ballObj() {
        this.velocity = new Vector2(700, 500);
    }

    @Override
    public void start() {
        setTag("ball");
        transform = new Transform(400, 300, 200, 200);

        collider = new Collider(transform);
        addComponent(Collider.class, collider);

        spriteRenderer = new SpriteRenderer("character/Apple.png");
        setSpriteRenderer(spriteRenderer);
    }

    @Override
    public void resize(int width, int height) {
        float size = width * 0.06f;
        transform.setWidth(size);
        transform.setHeight(size);

        // Store world bounds
        this.worldWidth = width;
        this.worldHeight = height;
    }

    @Override
    public void update(float deltaTime) {
        // Move the ball
        transform.translate(velocity.x * deltaTime, velocity.y * deltaTime);

        // Bounce off walls
        float x = transform.getX();
        float y = transform.getY();

        // Hit left or right wall? Flip X direction
        if (x <= 0 || x + transform.getWidth() >= worldWidth) {
            velocity.x = -velocity.x;
            velocity.x += (float) (Math.random() * 100f - 50f); // Small random nudge
        }

        // Hit top or bottom wall? Flip Y direction
        if (y <= 0 || y + transform.getHeight() >= worldHeight) {
            velocity.y = -velocity.y;
            velocity.y += (float) (Math.random() * 100f - 50f); // Small random nudge
        }
    }
    public Vector2 getVelocity() {
        return velocity;
    }

    @Override
    public void onCollisionStart(AbstractEntity other) {
        if (other.getTag().equals("player") || other.getTag().equals("ai")) {
            float ballCenterY = transform.getY() + transform.getHeight() / 2f;
            float paddleCenterY = other.getTransform().getY() + other.getTransform().getHeight() / 2f;
            float hitPosition = (ballCenterY - paddleCenterY) / (other.getTransform().getHeight() / 2f);

            velocity.x = -velocity.x;
            float randomFactor = (float) (Math.random() * 200f - 100f);
            velocity.y = hitPosition * 600f + randomFactor;

            // Ensure Y velocity is never too small
            if (Math.abs(velocity.y) < 200f) {
                velocity.y = velocity.y < 0 ? -200f : 200f;
            }

            hasBouncedOffPaddle = true;
        }
    }

    @Override
    public void onCollisionUpdate(AbstractEntity other) {
        // Safety net
    }

    @Override
    public void onCollisionExit(AbstractEntity other) {
        if (other instanceof MainObject) {
            hasBouncedOffPaddle = false;
            System.out.println("Ball left paddle!");
        }
    }

    @Override
    public Collider getCollider() {
        return collider;
    }
}