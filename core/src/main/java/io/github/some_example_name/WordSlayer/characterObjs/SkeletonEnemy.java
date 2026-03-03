package io.github.some_example_name.WordSlayer.characterObjs;

import io.github.some_example_name.AbstractEngine.EntityManagement.AnimationRenderer;

public class SkeletonEnemy extends AbstractEnemy {

    public SkeletonEnemy() {
        // skeleton is slower and bigger than other enemies
        speed = 60f;
        size  = 80f;
        maxHP = 150f;
        attackDamage = 5f;
        attackDelay = 2f;
        attackRange = 70f;  // size 80f
    }

    @Override
    protected void setupAnimations(AnimationRenderer ar) {
        ar.addAnimation("idle",    "enemyAnim/Skeleton/idle.png",    4, 1, 0.1f,  true);
        ar.addAnimation("movement",     "enemyAnim/Skeleton/walk.png",     4, 1, 0.06f, true);
        ar.addAnimation("attack1", "enemyAnim/Skeleton/attack1.png", 8, 1, 0.06f, false);
        ar.addAnimation("attack2", "enemyAnim/Skeleton/attack2.png", 8, 1, 0.06f, false);
        ar.addAnimation("takehit", "enemyAnim/Skeleton/takehit.png", 4, 1, 0.08f, false);
        ar.addAnimation("die",     "enemyAnim/Skeleton/death.png",     4, 1, 0.08f, false);


        ar.setOnComplete("attack1", () -> ar.setState("idle"));
        ar.setOnComplete("attack2", () -> ar.setState("idle"));
        ar.setOnComplete("takehit", () -> ar.setState("idle"));
        ar.setScale(5f); // set scale per enemy
    }
}
