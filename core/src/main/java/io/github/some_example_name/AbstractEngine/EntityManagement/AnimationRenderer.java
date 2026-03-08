package io.github.some_example_name.AbstractEngine.EntityManagement;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;
import java.util.Map;

// Handles sprite-sheet based animation for entities that have multiple visual states
// Each named state maps to its own animation clip and the renderer switches between them cleanly
public class AnimationRenderer {

    private float alpha = 1f; // 1 = fully opaque, 0 = invisible — used for fade effects
    private boolean visible = true;
    private float stateTime = 0f; // running timer that drives which frame is shown
    private float scale = 1f;     // uniform scale applied on top of the transform size

    // Each state (e.g. "spin", "idle", "explode") stores its own animation and source texture
    private final Map<String, Animation<TextureRegion>> animations = new HashMap<>();
    private final Map<String, Texture> textures = new HashMap<>();

    // Callbacks fired when a non-looping animation finishes — useful for triggering death or attack transitions
    private final Map<String, Runnable> onCompleteCallbacks = new HashMap<>();

    private String currentState;

    // When true the sprite is drawn right-to-left — used to face an entity left without a separate sprite sheet
    private boolean flipped = false;

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    // Loads a sprite sheet, slices it into frames and registers the animation under a named state
    // The first state added becomes the default — subsequent addAnimation calls do not override the current state
    public void addAnimation(String state, String path, int cols, int rows,
                             float frameDuration, boolean looping) {
        Texture sheet = new Texture(path);
        // Nearest filter preserves pixel art sharpness when the sprite is scaled up
        sheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // LibGDX splits the sheet into a 2D array based on equal tile dimensions
        TextureRegion[][] tmp = TextureRegion.split(sheet,
            sheet.getWidth() / cols,
            sheet.getHeight() / rows);

        // Flatten row-major 2D array into a 1D sequence for the Animation constructor
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

        // Automatically set the first registered state as the active one
        if (currentState == null) currentState = state;
    }

    // Switches the active animation state — resets stateTime to start the new clip from frame 0
    // Duplicate calls with the same state are ignored to prevent the animation restarting mid-play
    public void setState(String state) {
        if (state.equals(currentState)) return;
        currentState = state;
        stateTime = 0f;
    }

    // Registers a callback fired when the named non-looping animation finishes (e.g. trigger removal after a death animation)
    public void setOnComplete(String state, Runnable callback) {
        onCompleteCallbacks.put(state, callback);
    }

    // true = sprite faces right, false = sprite faces left
    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
    }

    public String getCurrentState() {
        return currentState;
    }

    // Advances the frame timer and fires completion callbacks for non-looping animations
    public void update(float deltaTime) {
        stateTime += deltaTime;

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

        // Re-center the scaled sprite so it grows outward from the transform origin rather than shifting
        float x = transform.getX() - (w - transform.getWidth()) / 2f;
        float y = transform.getY() - (h - transform.getHeight()) / 2f;

        TextureRegion frame = animations.get(currentState).getKeyFrame(stateTime);

        // Apply transparency before drawing so other sprites rendered after this are not affected
        batch.setColor(1f, 1f, 1f, alpha);

        if (flipped) {
            // Drawing with negative width flips the sprite horizontally
            batch.draw(frame, x + w, y, -w, h);
        } else {
            batch.draw(frame, x, y, w, h);
        }

        // Reset to fully opaque so subsequent draw calls in the same batch are not tinted
        batch.setColor(1f, 1f, 1f, 1f);
    }

    // Resets the animation timer back to the first frame — useful when reusing an entity
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

    // Returns the frame currently being displayed — used by external systems that need to read the sprite (e.g. comparison renderer)
    public TextureRegion getCurrentFrame() {
        if (currentState == null || !animations.containsKey(currentState)) return null;
        return animations.get(currentState).getKeyFrame(stateTime);
    }

    // Releases all loaded textures — must be called before discarding this renderer to prevent GL memory leaks
    public void dispose() {
        for (Texture t : textures.values()) {
            if (t != null) t.dispose();
        }
        textures.clear();
        animations.clear();
    }
}
