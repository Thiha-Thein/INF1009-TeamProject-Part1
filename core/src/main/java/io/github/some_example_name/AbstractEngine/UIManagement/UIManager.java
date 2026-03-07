package io.github.some_example_name.AbstractEngine.UIManagement;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

// Central UI renderer for the engine
// Responsible only for updating and rendering UI layers
// Does not communicate with game logic directly
public class UIManager {

    private final List<UILayer> layers = new ArrayList<>();

    // Adds a new UI layer
    public void addLayer(UILayer layer) {
        layers.add(layer);
    }

    // Updates all UI layers
    public void update(float deltaTime) {
        for (UILayer layer : layers) {
            layer.update(deltaTime);
        }
    }

    // Renders UI layers in order
    public void render(SpriteBatch batch) {
        for (UILayer layer : layers) {
            layer.render(batch);
        }
    }

    // Returns layers for input processing
    public List<UILayer> getLayers() {
        return layers;
    }
}
