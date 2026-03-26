package io.github.some_example_name.SolarSystemSimulation;

import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.List;

import io.github.some_example_name.AbstractEngine.EntityManagement.*;

// Manages the visual transition when a planet is selected for detailed viewing
// Smoothly slides the selected planet to a presentation position and hides all others,
// then slides it back and re-activates everything when the user exits
public class PlanetPresentationHandler {

    private final Viewport viewport;

    // Higher values make transitions snappier — this is used as a lerp weight multiplied by deltaTime
    private static final float TRANSITION_SPEED = 8f;

    private PlanetObj selectedPlanet; // null when no planet is selected

    private boolean transitioning = false;    // true while the planet is moving to or from its presentation position
    private boolean returningToOrbit = false; // distinguishes between entry and exit transitions

    // Flips immediately when a presentation starts or ends — UI shows/hides instantly rather than waiting for transition
    private boolean presenting = false;

    // Whether orbit lines should be drawn — hidden during presentation to reduce visual clutter
    private boolean showOrbits = true;

    // Current animated position and scale
    private float currentX;
    private float currentY;
    private float currentScale;

    // Target values the lerp is moving toward
    private float targetX;
    private float targetY;
    private float targetScale;

    // Stores original transform values so the planet can be returned to its orbit position
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

    // Selects a planet, hides all others and starts the transition to the presentation position
    // Silently ignored if a planet is already selected to prevent overlapping presentations
    public void triggerPresentation(PlanetObj planet, List<AbstractEntity> entities) {

        // If we are mid-return-transition, snap it to completion immediately
        // so a fast click on a new planet is never silently dropped
        if (selectedPlanet != null) {
            if (returningToOrbit) {
                selectedPlanet.getTransform().setX(originalX);
                selectedPlanet.getTransform().setY(originalY);
                if (selectedPlanet.getAnimationRenderer() != null)
                    selectedPlanet.getAnimationRenderer().setScale(originalScale);
                transitioning    = false;
                returningToOrbit = false;
                selectedPlanet   = null;
            } else {
                return; // genuinely mid-presentation, ignore the click
            }
        }

        selectedPlanet = planet;

        // UI and orbit lines respond instantly — only the planet position transitions smoothly
        presenting = true;
        showOrbits = false;

        // Deactivate all entities so they disappear from the scene during presentation
        setAllActive(entities, false);

        // Keep the selected planet visible
        selectedPlanet.setActive(true);

        if (selectedPlanet.getAnimationRenderer() != null) {
            selectedPlanet.getAnimationRenderer().setVisible(true);
        }

        // Stop all planets from orbiting so positions are stable during the presentation
        for (AbstractEntity entity : entities) {
            if (entity instanceof PlanetObj) {
                ((PlanetObj) entity).pauseOrbit();
            }
        }

        beginPresentationTransition();
    }

    // Deselects the planet, re-activates all others and starts the return transition
    public void triggerDeselect(List<AbstractEntity> entities) {

        if (selectedPlanet == null) return;

        presenting = false;
        showOrbits = true;

        setAllActive(entities, true);

        // Resume orbits before the return transition so they are running by the time the planet snaps back
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

    // UI elements check this to determine when to show the facts panel and comparison selector
    public boolean isPresenting() {
        return presenting;
    }

    public PlanetObj getSelectedPlanet() {
        return selectedPlanet;
    }

    public boolean shouldShowOrbits() {
        return showOrbits;
    }

    // Records current position and calculates the center-left target position for the presentation layout
    private void beginPresentationTransition() {

        Transform t = selectedPlanet.getTransform();

        originalX = t.getX();
        originalY = t.getY();
        originalScale = selectedPlanet.getAnimationRenderer() != null
            ? selectedPlanet.getAnimationRenderer().getScale()
            : 1f;

        currentX = originalX;
        currentY = originalY;

        float worldWidth  = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        // Target the left quarter of the screen horizontally, centered vertically
        targetX = (worldWidth * 0.25f) - (t.getWidth() / 2f);
        targetY = (worldHeight * 0.5f)  - (t.getHeight() / 2f);

        // Scale is not changed during the move — presentation rendering handles display sizing separately
        currentScale = originalScale;
        targetScale  = originalScale;

        transitioning    = true;
        returningToOrbit = false;
    }

    // Sets the return transition targets back to the orbit position saved in originalX/Y
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

    // Lerp-based transition — moves current values toward target at a rate proportional to deltaTime
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

        // Snap to exact target values once close enough to avoid the lerp asymptotically approaching forever
        if (Math.abs(targetX - currentX) < 0.5f &&
            Math.abs(targetY - currentY) < 0.5f &&
            Math.abs(targetScale - currentScale) < 0.01f) {

            selectedPlanet.getTransform().setX(targetX);
            selectedPlanet.getTransform().setY(targetY);

            if (selectedPlanet.getAnimationRenderer() != null) {
                selectedPlanet.getAnimationRenderer().setScale(targetScale);
            }

            transitioning = false;

            // Clear the selected planet only at the end of the return transition, not the entry one
            if (returningToOrbit) {
                selectedPlanet = null;
                returningToOrbit = false;
            }
        }
    }

    // Returns a visual scale factor for displaying one planet relative to another in comparison mode
    // Uses square root of the diameter ratio to avoid the largest planets overwhelming the screen
    public float calculateScale(float baseDiameter, float compareDiameter) {

        float ratio = compareDiameter / baseDiameter;

        return (float)Math.sqrt(ratio);
    }

    // Enables or disables all entities and their animation renderers in a single pass
    private void setAllActive(List<AbstractEntity> entities, boolean active) {

        for (AbstractEntity entity : entities) {

            entity.setActive(active);

            if (entity.getAnimationRenderer() != null) {
                entity.getAnimationRenderer().setVisible(active);
            }
        }
    }
}
