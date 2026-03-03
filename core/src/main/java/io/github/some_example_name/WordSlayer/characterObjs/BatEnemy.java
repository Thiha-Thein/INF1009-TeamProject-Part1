package io.github.some_example_name.WordSlayer.characterObjs;

import io.github.some_example_name.AbstractEngine.EntityManagement.AnimationRenderer;

public class BatEnemy extends AbstractEnemy {

    public BatEnemy() {
        speed = 200f;
        size  = 85f;
        maxHP = 60f;
        attackDamage = 2f;
        attackDelay = 2f;
        attackRange = 90f;
    }

    @Override
    protected void setupAnimations(AnimationRenderer ar) {
        ar.addAnimation("movement", "enemyAnim/flying_eye/flight.png",     8, 1, 0.08f, true);
        ar.addAnimation("idle", "enemyAnim/flying_eye/flight.png",     8, 1, 0.08f, true);
        ar.addAnimation("attack1", "enemyAnim/flying_eye/attack1.png", 8, 1, 0.06f, false);
        ar.addAnimation("attack2", "enemyAnim/flying_eye/attack2.png", 8, 1, 0.06f, false);
        ar.addAnimation("takehit", "enemyAnim/flying_eye/takehit.png", 4, 1, 0.08f, false);
        ar.addAnimation("die",     "enemyAnim/flying_eye/death.png",     4, 1, 0.08f, false);
        ar.setScale(5f); // set scale per enemy
    }
}
