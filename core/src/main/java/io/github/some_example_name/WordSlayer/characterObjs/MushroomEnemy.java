package io.github.some_example_name.WordSlayer.characterObjs;

import io.github.some_example_name.AbstractEngine.EntityManagement.AnimationRenderer;

public class MushroomEnemy extends AbstractEnemy {

    public MushroomEnemy() {
        speed = 130f;
        size  = 100f;
        maxHP = 90f;
        attackDamage = 2f;
        attackDelay = 2f;
        attackRange = 105f; // size 100f
    }

    @Override
    protected void setupAnimations(AnimationRenderer ar) {
        ar.addAnimation("idle",    "enemyAnim/Mushroom/idle.png",    4, 1, 0.1f,  true);
        ar.addAnimation("movement",     "enemyAnim/Mushroom/run.png",     8, 1, 0.06f, true);
        ar.addAnimation("attack1", "enemyAnim/Mushroom/attack1.png", 8, 1, 0.06f, false);
        ar.addAnimation("attack2", "enemyAnim/Mushroom/attack2.png", 8, 1, 0.06f, false);
        ar.addAnimation("takehit", "enemyAnim/Mushroom/takehit.png", 4, 1, 0.08f, false);
        ar.addAnimation("die",     "enemyAnim/Mushroom/death.png",     4, 1, 0.08f, false);

        ar.setOnComplete("attack1", () -> ar.setState("idle"));
        ar.setOnComplete("attack2", () -> ar.setState("idle"));
        ar.setOnComplete("takehit", () -> ar.setState("idle"));
        ar.setScale(5f); // set scale per enemy
    }
}
