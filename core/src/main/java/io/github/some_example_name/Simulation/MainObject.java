package io.github.some_example_name.Simulation;

import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.AbstractEngine.CollisionManagement.*;
import io.github.some_example_name.AbstractEngine.EntityManagement.*;
import io.github.some_example_name.AbstractEngine.MovementManagement.*;
import io.github.some_example_name.AbstractEngine.IOManagement.InputComponent;

public class MainObject extends AbstractEntity implements ICollision {

    private MovementComponent movement;
    private SpriteRenderer spriteRenderer;
    private Collider collider;
    private InputComponent input;

    @Override
    public void start() {

        setTag("player");

        transform = new Transform(250, 200, 128, 128);

        collider = new Collider(transform);
        addComponent(Collider.class, collider);

        movement = new MovementComponent(transform, 500f);
        addComponent(MovementComponent.class, movement);

        input = new InputComponent();
        input.bind("moveUp", "w");
        input.bind("moveDown", "s");
        input.bind("moveLeft", "a");
        input.bind("moveRight", "d");

        addComponent(InputComponent.class, input);

        spriteRenderer = new SpriteRenderer("character/frogman.png");
        setSpriteRenderer(spriteRenderer);
    }

    @Override
    public void update(float deltaTime) {

        float x = 0, y = 0;

        if (input.isDown("moveUp")) y += 1;
        if (input.isDown("moveDown")) y -= 1;
        if (input.isDown("moveRight")) x += 1;
        if (input.isDown("moveLeft")) x -= 1;

        movement.setDirection(new Vector2(x, y));

    }

    public MovementComponent getMovement() {
        return movement;
    }

    @Override
    public Collider getCollider() {
        return collider;
    }
    
    @Override
    public void resize(int width, int height) {}
    
    @Override public void onCollisionStart(AbstractEntity other) {}
    @Override public void onCollisionUpdate(AbstractEntity other) {}
    @Override public void onCollisionExit(AbstractEntity other) {}
}
