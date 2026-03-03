package io.github.some_example_name.WordSlayer.characterObjs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;

import io.github.some_example_name.AbstractEngine.CollisionManagement.*;
import io.github.some_example_name.AbstractEngine.EntityManagement.*;
import io.github.some_example_name.AbstractEngine.IOManagement.*;
import io.github.some_example_name.AbstractEngine.MovementManagement.*;
import io.github.some_example_name.WordSlayer.*;

public class PlayerObj extends AbstractEntity implements ICollision {

    private final float speed = 300f;
    private final float size  = 100f;
    private final Viewport viewport;

    private boolean lastAttack = false;
    private final float attackDamage = 25f;
    private final List<AbstractEntity> enemiesInRange = new ArrayList<>();

    private MovementComponent movement;
    private Collider collider;
    private InputComponent input;
    private HealthComponent healthComponent;
    AnimationRenderer ar = new AnimationRenderer();

    public PlayerObj(Viewport viewport) {
        this.viewport = viewport;
    }

    @Override
    public void start() {
        setTag("player");

        healthComponent = new HealthComponent(
            100f,
            HealthComponent.BarType.PLAYER,
            "ui/player_healthbar_bg.png",
            "ui/player_healthbar_fill.png"
        );
        addComponent(HealthComponent.class, healthComponent);

        float cx = (Gdx.graphics.getWidth()  - size) / 2f;
        float cy = (Gdx.graphics.getHeight() - size) / 2f;
        transform = new Transform(cx, cy, size, size);

        movement = new MovementComponent(transform, speed);
        addComponent(MovementComponent.class, movement);

        collider = new Collider(transform);
        addComponent(Collider.class, collider);

        setupInput();
        setupAnimation();
    }

    @Override
    public void update(float deltaTime) {
        if (healthComponent.getCurrentHP() != 0){
            handleAttack();
            handleMovement();
            updateAnimationState();
        }
        else{
            die();
        }
    }

    private void setupInput() {
        input = new InputComponent();
        input.bind("moveUp",    "w");
        input.bind("moveDown",  "s");
        input.bind("moveLeft",  "a");
        input.bind("moveRight", "d");
        input.bind("attack1",   "leftClick");
        addComponent(InputComponent.class, input);
    }

    private void setupAnimation() {
        ar.addAnimation("idle",     "characterAnim/player_idle.png",   11, 1, 0.08f, true);
        ar.addAnimation("run",      "characterAnim/player_run.png",     8, 1, 0.06f, true);
        ar.addAnimation("takehit",  "characterAnim/player_takehit.png", 4, 1, 0.08f, false);
        ar.addAnimation("attack1",  "characterAnim/player_attack1.png", 7, 1, 0.06f, false);
        ar.addAnimation("attack2",  "characterAnim/player_attack2.png", 7, 1, 0.06f, false);
        ar.addAnimation("die",      "characterAnim/player_die.png",    11, 1, 0.08f, false);

        ar.setOnComplete("attack1", () -> ar.setState("idle"));
        ar.setOnComplete("attack2", () -> ar.setState("idle"));
        ar.setOnComplete("die",     this::markForRemoval);
        ar.setScale(5f);

        setAnimationRenderer(ar);
    }

    private void handleMovement() {
        String state = animationRenderer.getCurrentState();
        if (state.equals("attack1") || state.equals("attack2")) {
            movement.setDirection(Vector2.Zero);
            return;
        }
        float dx = 0, dy = 0;

        if (input.isDown("moveUp"))    dy += 1;
        if (input.isDown("moveDown"))  dy -= 1;
        if (input.isDown("moveLeft"))  { dx -= 1; animationRenderer.setFlipped(true); }
        if (input.isDown("moveRight")) { dx += 1; animationRenderer.setFlipped(false); }

        movement.setDirection(new Vector2(dx, dy));
    }

    private void handleAttack() {
        String state = animationRenderer.getCurrentState();
        if (state.equals("attack1") || state.equals("attack2")) return;

        if (input.wasPressed("attack1")) {
            movement.setDirection(Vector2.Zero);

            String nextAttack = !lastAttack ? "attack1" : "attack2";
            lastAttack = !lastAttack;

            // deal damage to enemy when attack animation finishes
            animationRenderer.setOnComplete(nextAttack, () -> {
                for (AbstractEntity enemy : enemiesInRange) {
                    if (!enemy.isActive()) continue;
                    HealthComponent hc = enemy.getComponent(HealthComponent.class);
                    if (hc != null) {
                        hc.takeDamage(attackDamage);
                    }
                }
                animationRenderer.setState("idle");
            });

            animationRenderer.setState(nextAttack);
        }
    }

    private void updateAnimationState() {
        String state = animationRenderer.getCurrentState();

        // don't interrupt one-shot animations
        if (state.equals("takehit") || state.equals("attack1") ||
            state.equals("attack2") || state.equals("die")) return;

        boolean moving = input.isDown("moveUp")   || input.isDown("moveDown") ||
            input.isDown("moveLeft")  || input.isDown("moveRight");

        animationRenderer.setState(moving ? "run" : "idle");
    }

    public void takeDamage(float amount) {
        healthComponent.takeDamage(amount);
        // chain into die after takehit if health is zero
        if (healthComponent.getCurrentHP() == 0) {
            animationRenderer.setOnComplete("takehit", () -> die());
        } else {
            animationRenderer.setState("takehit");
        }
    }

    public void die() {
        movement.setDirection(Vector2.Zero);
        animationRenderer.setState("die");
    }

    @Override
    public void render(SpriteBatch batch) {
        super.render(batch);

        HealthComponent hc = getComponent(HealthComponent.class);
        if (hc != null) {
            hc.render(batch,
                transform.getX() + transform.getWidth()  / 2f,
                transform.getY() + transform.getHeight(),
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight());
        }
    }

    @Override public void resize(int width, int height) {}
    @Override public Collider getCollider() { return collider; }

    @Override
    public void onCollisionStart(AbstractEntity other) {
        if ("enemy".equals(other.getTag())) {
            if (!enemiesInRange.contains(other)) {
                enemiesInRange.add(other);
            }
        }
    }

    @Override
    public void onCollisionUpdate(AbstractEntity other) {
    }

    @Override
    public void onCollisionExit(AbstractEntity other) {
        if ("enemy".equals(other.getTag())) {
            enemiesInRange.remove(other);
        }
    }
}
