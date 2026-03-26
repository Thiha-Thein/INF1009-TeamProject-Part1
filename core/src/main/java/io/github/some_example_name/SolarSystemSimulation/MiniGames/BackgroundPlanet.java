package io.github.some_example_name.SolarSystemSimulation.MiniGames;

import io.github.some_example_name.AbstractEngine.AudioManagement.SoundEventComponent;
import io.github.some_example_name.AbstractEngine.EntityManagement.AbstractEntity;
import io.github.some_example_name.AbstractEngine.EntityManagement.AnimationRenderer;
import io.github.some_example_name.AbstractEngine.EntityManagement.Transform;

// a faint spinning Earth drawn behind the question panel in Fact or Fiction
// also carries a SoundEventComponent so the minigame can request sounds through the engine
// previously an anonymous AbstractEntity defined inline inside FactOrFictionMap
public class BackgroundPlanet extends AbstractEntity {

    private final float screenW;
    private final float screenH;

    // stores the screen dimensions so start() can size and center the sprite correctly
    public BackgroundPlanet(float screenW, float screenH) {
        this.screenW = screenW;
        this.screenH = screenH;
    }

    // sizes the earth at 55% of screen height, centers it, and sets it to very low alpha
    // so it does not distract from the question text
    @Override
    public void start() {

        float size = screenH * 0.55f;
        transform = new Transform((screenW - size) / 2f, (screenH - size) / 2f, size, size);

        AnimationRenderer ar = new AnimationRenderer();
        ar.addAnimation("spin", "planets/earth.png", 30, 8, 0.08f, true);
        ar.setAlpha(0.12f);
        setAnimationRenderer(ar);

        // SoundEventComponent lets the minigame request sounds through the AudioSystem
        addComponent(SoundEventComponent.class, new SoundEventComponent());
        setTag("factorfiction_bg");
    }

    @Override public void update(float deltaTime) {}
    @Override public void resize(int w, int h) {}
}
