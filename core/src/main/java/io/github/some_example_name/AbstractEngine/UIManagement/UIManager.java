package io.github.some_example_name.AbstractEngine.UIManagement;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

// Central coordinator for UI rendering — owns a list of UILayers and drives their update and render each frame
// Intentionally has no knowledge of game logic; game-specific UI interaction is handled by UIInputSystem
public class UIManager {

    private final List<UILayer> layers = new ArrayList<>();

    // Adds a new layer — layers are rendered in insertion order, so add background layers before foreground layers
    public void addLayer(UILayer layer) {
        layers.add(layer);
    }

    // Advances all layers each frame so animated elements can update
    public void update(float deltaTime) {
        for (UILayer layer : layers) {
            layer.update(deltaTime);
        }
    }

    // Renders all layers in insertion order — earlier layers appear behind later ones
    public void render(SpriteBatch batch) {
        for (UILayer layer : layers) {
            layer.render(batch);
        }
    }

    // Exposes the layer list so UIInputSystem can walk all layers for hit-testing
    public List<UILayer> getLayers() {
        return layers;
    }
}
