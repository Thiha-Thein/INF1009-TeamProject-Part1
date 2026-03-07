package io.github.some_example_name.SolarSystemSimulation;

import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.List;

import io.github.some_example_name.AbstractEngine.EntityManagement.*;

public class PlanetPresentationHandler {

    private final Viewport viewport;

    private static final float TRANSITION_SPEED = 8f;

    private PlanetObj selectedPlanet;

    private boolean transitioning = false;
    private boolean returningToOrbit = false;

    // controls UI visibility instantly
    private boolean presenting = false;

    private boolean showOrbits = true;

    private float currentX;
    private float currentY;
    private float currentScale;

    private float targetX;
    private float targetY;
    private float targetScale;

    private float originalX;
    private float originalY;
    private float originalScale;

    public PlanetPresentationHandler(Viewport viewport) {
        this.viewport = viewport;
    }

    public void update(float deltaTime) {
        if (transitioning) {
            updateTransition(deltaTime);
        }
    }

    public void triggerPresentation(PlanetObj planet, List<AbstractEntity> entities) {

        if (selectedPlanet != null) return;

        selectedPlanet = planet;

        // UI changes instantly
        presenting = true;
        showOrbits = false;

        setAllActive(entities, false);

        selectedPlanet.setActive(true);

        if (selectedPlanet.getAnimationRenderer() != null) {
            selectedPlanet.getAnimationRenderer().setVisible(true);
        }

        // pause orbit movement
        for (AbstractEntity entity : entities) {
            if (entity instanceof PlanetObj) {
                ((PlanetObj) entity).pauseOrbit();
            }
        }

        beginPresentationTransition();
    }

    public void triggerDeselect(List<AbstractEntity> entities) {

        if (selectedPlanet == null) return;

        // UI disappears instantly
        presenting = false;
        showOrbits = true;

        setAllActive(entities, true);

        for (AbstractEntity entity : entities) {
            if (entity instanceof PlanetObj) {
                ((PlanetObj) entity).resumeOrbit();
            }
        }

        beginReturnTransition();
    }

    public boolean isSelected() {
        return selectedPlanet != null;
    }

    // UI visibility check
    public boolean isPresenting() {
        return presenting;
    }

    public PlanetObj getSelectedPlanet() {
        return selectedPlanet;
    }

    public boolean shouldShowOrbits() {
        return showOrbits;
    }

    private void beginPresentationTransition() {

        Transform t = selectedPlanet.getTransform();

        originalX = t.getX();
        originalY = t.getY();
        originalScale = selectedPlanet.getAnimationRenderer() != null
            ? selectedPlanet.getAnimationRenderer().getScale()
            : 1f;

        currentX = originalX;
        currentY = originalY;

        // ── REMOVE all targetScale logic, just move to center-left ──
        float worldWidth  = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        targetX = (worldWidth * 0.25f) - (t.getWidth() / 2f);
        targetY = (worldHeight * 0.5f)  - (t.getHeight() / 2f);

        // scale stays unchanged during transition
        currentScale = originalScale;
        targetScale  = originalScale;  // <-- no resize, render() handles that

        transitioning    = true;
        returningToOrbit = false;
    }

    private void beginReturnTransition() {

        Transform t = selectedPlanet.getTransform();

        currentX = t.getX();
        currentY = t.getY();

        currentScale = selectedPlanet.getAnimationRenderer() != null
            ? selectedPlanet.getAnimationRenderer().getScale()
            : 1f;

        targetX = originalX;
        targetY = originalY;
        targetScale = originalScale;

        transitioning = true;
        returningToOrbit = true;
    }

    private void updateTransition(float deltaTime) {

        float step = TRANSITION_SPEED * deltaTime;

        currentX += (targetX - currentX) * step;
        currentY += (targetY - currentY) * step;
        currentScale += (targetScale - currentScale) * step;

        selectedPlanet.getTransform().setX(currentX);
        selectedPlanet.getTransform().setY(currentY);

        if (selectedPlanet.getAnimationRenderer() != null) {
            selectedPlanet.getAnimationRenderer().setScale(currentScale);
        }

        if (Math.abs(targetX - currentX) < 0.5f &&
            Math.abs(targetY - currentY) < 0.5f &&
            Math.abs(targetScale - currentScale) < 0.01f) {

            selectedPlanet.getTransform().setX(targetX);
            selectedPlanet.getTransform().setY(targetY);

            if (selectedPlanet.getAnimationRenderer() != null) {
                selectedPlanet.getAnimationRenderer().setScale(targetScale);
            }

            transitioning = false;

            if (returningToOrbit) {
                selectedPlanet = null;
                returningToOrbit = false;
            }
        }
    }

    // calculates visual scale for planet comparison
    public float calculateScale(float baseDiameter, float compareDiameter) {

        float ratio = compareDiameter / baseDiameter;

        return (float)Math.sqrt(ratio);
    }

    private void setAllActive(List<AbstractEntity> entities, boolean active) {

        for (AbstractEntity entity : entities) {

            entity.setActive(active);

            if (entity.getAnimationRenderer() != null) {
                entity.getAnimationRenderer().setVisible(active);
            }
        }
    }
}
