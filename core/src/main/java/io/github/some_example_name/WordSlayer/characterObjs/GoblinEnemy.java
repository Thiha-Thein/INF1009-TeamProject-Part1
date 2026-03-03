package io.github.some_example_name.WordSlayer.characterObjs;

import io.github.some_example_name.AbstractEngine.EntityManagement.AnimationRenderer;

public class GoblinEnemy extends AbstractEnemy {

    public GoblinEnemy() {
        speed = 150f;
        size  = 85f;
        maxHP = 85f;
        attackDamage = 2f;
        attackDelay = 2f;
        attackRange = 90f;  // size 85f — same as bat
    }

    @Override
    protected void setupAnimations(AnimationRenderer ar) {
        ar.addAnimation("idle",    "enemyAnim/Goblin/idle.png",    4, 1, 0.1f,  true);
        ar.addAnimation("movement",     "enemyAnim/Goblin/run.png",     8, 1, 0.06f, true);
        ar.addAnimation("attack1", "enemyAnim/Goblin/attack1.png", 8, 1, 0.06f, false);
        ar.addAnimation("attack2", "enemyAnim/Goblin/attack2.png", 8, 1, 0.06f, false);
        ar.addAnimation("takehit", "enemyAnim/Goblin/takehit.png", 4, 1, 0.08f, false);
        ar.addAnimation("die",     "enemyAnim/Goblin/death.png",     4, 1, 0.08f, false);

        ar.setOnComplete("attack1", () -> ar.setState("idle"));
        ar.setOnComplete("attack2", () -> ar.setState("idle"));
        ar.setOnComplete("takehit", () -> ar.setState("idle"));
        ar.setScale(5f); // set scale per enemy
    }
}
