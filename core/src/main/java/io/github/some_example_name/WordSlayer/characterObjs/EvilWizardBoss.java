package io.github.some_example_name.WordSlayer.characterObjs;

import io.github.some_example_name.AbstractEngine.EntityManagement.*;
import io.github.some_example_name.WordSlayer.*;

public class EvilWizardBoss extends AbstractEnemy {

    public EvilWizardBoss() {
        speed        = 200f;
        size         = 160f;
        maxHP        = 500f;
        attackDamage = 7f;
        attackDelay = 5f;
        attackRange = 130f; // size 150f
    }

    @Override
    public void start() {
        super.start();

        // replace default enemy bar with boss bar
        HealthComponent hc = new HealthComponent(
            maxHP,
            HealthComponent.BarType.BOSS,
            "ui/enemy_healthbar_bg.png",
            "ui/enemy_healthbar_fill.png"
        );
        addComponent(HealthComponent.class, hc);
    }

    @Override
    protected void setupAnimations(AnimationRenderer ar) {
        ar.addAnimation("movement", "enemyAnim/evil_wizard2/run.png",     8, 1, 0.06f, true);
        ar.addAnimation("idle",    "enemyAnim/evil_wizard2/idle.png",    8, 1, 0.1f,  true);
        ar.addAnimation("attack1",  "enemyAnim/evil_wizard2/attack1.png", 8, 1, 0.06f, false);
        ar.addAnimation("attack2",  "enemyAnim/evil_wizard2/attack2.png", 8, 1, 0.06f, false);
        ar.addAnimation("takehit",  "enemyAnim/evil_wizard2/takehit.png", 4, 1, 0.08f, false);
        ar.addAnimation("die",      "enemyAnim/evil_wizard2/death.png",   4, 1, 0.08f, false);
        ar.setScale(5f);
    }
}
