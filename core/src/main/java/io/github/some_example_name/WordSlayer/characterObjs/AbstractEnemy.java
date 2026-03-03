package io.github.some_example_name.WordSlayer.characterObjs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.AbstractEngine.CollisionManagement.*;
import io.github.some_example_name.AbstractEngine.EntityManagement.*;
import io.github.some_example_name.AbstractEngine.MovementManagement.*;
import io.github.some_example_name.WordSlayer.HealthComponent;
import io.github.some_example_name.WordSlayer.spawnMechanics.*;
import io.github.some_example_name.WordSlayer.wordMechanics.*;

public abstract class AbstractEnemy extends AbstractEntity implements ICollision {

    /* =========================
       Components
       ========================= */

    protected MovementComponent movement;
    protected Collider collider;
    private HealthComponent healthComponent;
    protected WaveManager waveManager;

    /* =========================
       Basic stats / setup
       ========================= */

    protected float spawnX, spawnY;
    protected float speed = 100f;
    protected float size = 80f;
    protected float maxHP;
    protected float attackDamage;
    // behaviour ranges
    protected float chaseRange = 600f;   // start chasing player
    protected float attackRange = 80f;   // stop moving near player
    // attack cooldown
    protected float attackDelay = 1.5f;
    private float attackCooldown = 0f;
    // alternates attack animations
    private boolean lastAttack = false;
    // target set externally each frame
    private AbstractEntity target;
    protected SentenceManager sentenceManager;

    public void setInitialPosition(float x, float y) {
        this.spawnX = x;
        this.spawnY = y;
    }

    @Override
    public void start() {

        setTag("enemy");

        // health bar + HP system
        healthComponent = new HealthComponent(
            maxHP,
            HealthComponent.BarType.ENEMY,
            "ui/enemy_healthbar_bg.png",
            "ui/enemy_healthbar_fill.png"
        );
        addComponent(HealthComponent.class, healthComponent);
        // create transform at spawn position
        transform = new Transform(spawnX, spawnY, size, size);

        // movement component
        movement = new MovementComponent(transform, speed);
        addComponent(MovementComponent.class, movement);

        // collider for combat detection
        collider = new Collider(transform);
        addComponent(Collider.class, collider);

        // animation setup (defined by subclasses)
        AnimationRenderer ar = new AnimationRenderer();
        ar.setOnComplete("die", this::markForRemoval);
        setupAnimations(ar);
        setAnimationRenderer(ar);

        // default animation = idle
        animationRenderer.setState("idle");

    }

    @Override
    public void update(float deltaTime) {

        // if alive → normal behaviour
        if (healthComponent.getCurrentHP() > 0) {

            // reduce attack cooldown timer
            if (attackCooldown > 0)
                attackCooldown -= deltaTime;

            handleMovement();

        } else {
            die();
        }
    }

    @Override
    public void render(SpriteBatch batch) {

        super.render(batch);
        // draw health bar above enemy
        HealthComponent hc = getComponent(HealthComponent.class);
        if (hc != null) {
            hc.render(
                batch,
                transform.getX() + transform.getWidth() / 2f,
                transform.getY() + transform.getHeight(),
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
            );
        }

    }

    /* =========================
       Movement + Animation Logic
       (Single behaviour controller)
       ========================= */

    private void handleMovement() {

        String state = animationRenderer.getCurrentState();

        // do not move while attacking
        if (isAttacking(state)) {
            movement.setDirection(Vector2.Zero);
            return;
        }

        // no target → idle
        if (target == null) {
            movement.setDirection(Vector2.Zero);
            animationRenderer.setState("idle");
            return;
        }

        // enemy center
        float ex = transform.getX() + transform.getWidth() / 2f;
        float ey = transform.getY() + transform.getHeight() / 2f;

        // target center
        float tx = target.getTransform().getX()
            + target.getTransform().getWidth() / 2f;
        float ty = target.getTransform().getY()
            + target.getTransform().getHeight() / 2f;

        float distance = Vector2.dst(ex, ey, tx, ty);

        // OUTSIDE CHASE RANGE → IDLE
        if (distance > chaseRange) {
            movement.setDirection(Vector2.Zero);
            animationRenderer.setState("idle");
            return;
        }

        // INSIDE ATTACK RANGE → STOP (collision handles attacking)
        if (distance <= attackRange) {
            movement.setDirection(Vector2.Zero);
            animationRenderer.setState("idle");
            return;
        }

        // OTHERWISE → CHASE PLAYER
        Vector2 dir = new Vector2(tx - ex, ty - ey).nor();
        movement.setDirection(dir);

        animationRenderer.setFlipped(dir.x < 0);
        animationRenderer.setState(getMoveState());
    }

    /* =========================
       Attack Logic
       (ONLY called by collision)
       ========================= */

    private void triggerAttack(AbstractEntity other) {

        if (healthComponent.getCurrentHP() == 0) return;
        if (attackCooldown > 0) return;

        String state = animationRenderer.getCurrentState();
        if (isAttacking(state)) return;

        // alternate attack animations
        String nextAttack = lastAttack ? "attack2" : "attack1";
        lastAttack = !lastAttack;

        attackCooldown = attackDelay;

        // damage applied when animation finishes
        animationRenderer.setOnComplete(nextAttack, () -> {
            HealthComponent hc = other.getComponent(HealthComponent.class);
            if (hc != null) hc.takeDamage(attackDamage);

            animationRenderer.setState("idle");
        });

        animationRenderer.setState(nextAttack);
    }

    private boolean isAttacking(String state) {
        return state.equals("attack1") || state.equals("attack2");
    }

    /* =========================
       Combat / Damage
       ========================= */

    public void takeDamage(float amount) {

        if (healthComponent.getCurrentHP() == 0) return;

        healthComponent.takeDamage(amount);
        animationRenderer.setState("takehit");
    }

    public void setSentenceManager(SentenceManager sentenceManager) {
        this.sentenceManager = sentenceManager;
    }

    public void die() {
        movement.setDirection(Vector2.Zero);
        WordComponent wc = getComponent(WordComponent.class);
        if (wc != null) {
            sentenceManager.submitWord(wc.getWord());
        }
        animationRenderer.setState("die");
    }

    public void setTarget(AbstractEntity target) {
        this.target = target;
    }

    @Override
    public void onCollisionStart(AbstractEntity other) {
        if (other.getTag().equals("player"))
            triggerAttack(other);
    }

    @Override
    public void onCollisionUpdate(AbstractEntity other) {
        if (other.getTag().equals("player"))
            triggerAttack(other);
    }

    @Override
    public void onCollisionExit(AbstractEntity other) {}

    protected abstract void setupAnimations(AnimationRenderer ar);

    // subclasses can override movement animation name
    protected String getMoveState() {
        return "movement";
    }

    @Override public void resize(int width, int height) {}
    @Override public Collider getCollider() { return collider; }
}
