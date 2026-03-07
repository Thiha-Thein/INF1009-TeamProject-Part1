package io.github.some_example_name.AbstractEngine.EntityManagement;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;
import java.util.Map;

public class AnimationRenderer {

    private float alpha = 1f; // 1 = fully visible

    private boolean visible = true;
    private float stateTime = 0f, scale = 1f;

    // Multi-state animations — state name maps to animation and texture
    private final Map<String, Animation<TextureRegion>> animations = new HashMap<>();
    private final Map<String, Texture> textures = new HashMap<>();

    // Fire callback when non-looping animation finishes e.g. die, attack
    private final Map<String, Runnable> onCompleteCallbacks = new HashMap<>();

    private String currentState;

    // Flip sprite horizontally for left/right direction
    private boolean flipped = false;

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    // Register a named animation state — first added becomes default
    public void addAnimation(String state, String path, int cols, int rows,
                             float frameDuration, boolean looping) {
        Texture sheet = new Texture(path);
        sheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // LibGDX slices automatically based on cols and rows
        TextureRegion[][] tmp = TextureRegion.split(sheet,
            sheet.getWidth() / cols,
            sheet.getHeight() / rows);

        // Flatten 2D array into 1D
        TextureRegion[] frames = new TextureRegion[cols * rows];
        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                frames[index++] = tmp[i][j];
            }
        }

        Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
        anim.setPlayMode(looping ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);

        animations.put(state, anim);
        textures.put(state, sheet);

        // first added state becomes default
        if (currentState == null) currentState = state;
    }

    // Ignore duplicate state calls to prevent animation jitter
    public void setState(String state) {
        if (state.equals(currentState)) return;
        currentState = state;
        stateTime = 0f;
    }

    // Fire callback when non-looping animation finishes e.g. die, attack
    public void setOnComplete(String state, Runnable callback) {
        onCompleteCallbacks.put(state, callback);
    }

    // true = facing right, false = facing left (default)
    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void update(float deltaTime) {
        stateTime += deltaTime;

        // Check if non-looping animation finished and fire its callback
        if (currentState != null) {
            Animation<TextureRegion> current = animations.get(currentState);
            if (current != null && current.isAnimationFinished(stateTime)) {
                Runnable callback = onCompleteCallbacks.get(currentState);
                if (callback != null) callback.run();
            }
        }
    }

    public void render(SpriteBatch batch, Transform transform) {

        if (!visible) return;
        if (currentState == null || !animations.containsKey(currentState)) return;

        float w = transform.getWidth() * scale;
        float h = transform.getHeight() * scale;

        // Center the scaled sprite on the transform position
        float x = transform.getX() - (w - transform.getWidth()) / 2f;
        float y = transform.getY() - (h - transform.getHeight()) / 2f;

        TextureRegion frame = animations.get(currentState).getKeyFrame(stateTime);

        // Apply transparency
        batch.setColor(1f, 1f, 1f, alpha);

        if (flipped) {
            batch.draw(frame, x + w, y, -w, h);
        } else {
            batch.draw(frame, x, y, w, h);
        }

        // Reset color so other objects aren't affected
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void reset() {
        stateTime = 0f;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getScale() {
        return scale;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public float getAlpha() {
        return alpha;
    }

    public TextureRegion getCurrentFrame() {
        if (currentState == null || !animations.containsKey(currentState)) return null;
        return animations.get(currentState).getKeyFrame(stateTime);
    }

    public void dispose() {
        // dispose all textures
        for (Texture t : textures.values()) {
            if (t != null) t.dispose();
        }
        textures.clear();
        animations.clear();
    }
}
