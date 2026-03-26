package io.github.some_example_name.SolarSystemSimulation.MiniGames;

import io.github.some_example_name.AbstractEngine.EntityManagement.AbstractEntity;
import io.github.some_example_name.AbstractEngine.UIManagement.UIButton;

// holds all the state for one planet card in the Order the Planets minigame
// replaces the six parallel arrays (homeX/Y, currentX/Y, targetX/Y) that were
// previously spread across OrderThePlanetsMap — one PlanetCard per planet instead
public class PlanetCard {

    // the planet name this card represents
    public final String name;

    // the resting position in the top row — the card snaps back here when cleared from a slot
    public float homeX, homeY;

    // the position currently being drawn this frame — lerps toward target each update
    public float currentX, currentY;

    // the position we are lerping toward — set to a slot center when placed, home when cleared
    public float targetX, targetY;

    // the animated sprite entity managed by EntityManager
    public AbstractEntity entity;

    // the invisible hitbox button placed over the sprite for click detection
    public UIButton button;

    // sets home, current and target all to the same starting position
    public PlanetCard(String name, float x, float y) {
        this.name = name;
        homeX = currentX = targetX = x;
        homeY = currentY = targetY = y;
    }
}
