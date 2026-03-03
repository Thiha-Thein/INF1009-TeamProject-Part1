package io.github.some_example_name.WordSlayer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class HealthComponent {

    public enum BarType {
        PLAYER,
        ENEMY,
        BOSS
    }

    private float maxHP;
    private float currentHP;
    private final BarType type;
    private final Texture bg;
    private final Texture fill;

    public HealthComponent(float maxHP, BarType type, String bgPath, String fillPath) {
        this.maxHP     = maxHP;
        this.currentHP = maxHP;
        this.type      = type;
        bg   = new Texture(bgPath);
        fill = new Texture(fillPath);
    }

    public void takeDamage(float amount) {
        currentHP = Math.max(0, currentHP - amount);
    }

    public void heal(float amount) {
        currentHP = Math.min(currentHP + amount, maxHP);
    }

    public void render(SpriteBatch batch, float x, float y, float screenWidth, float screenHeight) {
        switch (type) {
            case PLAYER: renderPlayerBar(batch, screenHeight); break;
            case ENEMY:  renderEnemyBar(batch, x, y);         break;
            case BOSS:   renderBossBar(batch, screenWidth, screenHeight); break;
        }
    }

    private void renderPlayerBar(SpriteBatch batch, float screenHeight) {
        float barWidth  = 50f * 2.5f;
        float barHeight = 250f * 2.5f;
        float x = 20f;
        float y = screenHeight / 2f - barHeight / 2f - 400f;

        batch.draw(bg, x, y, barWidth, barHeight);

        float fillPercent = getHealthPercent();
        float visibleHeight = barHeight * fillPercent;
        int srcHeight = (int)(fill.getHeight() * fillPercent);
        int srcY = fill.getHeight() - srcHeight;

        batch.draw(fill, x, y, barWidth, visibleHeight,
            0, srcY, fill.getWidth(), srcHeight, false, false);
    }

    private void renderEnemyBar(SpriteBatch batch, float x, float y) {
        float barWidth  = 60f * 2;
        float barHeight = 8f * 2;
        float barX = x - barWidth / 2f;
        float barY = y + 10f;

        batch.draw(bg, barX, barY, barWidth, barHeight);

        float fillPercent = getHealthPercent();
        float visibleWidth = barWidth * fillPercent;
        int srcWidth = (int)(fill.getWidth() * fillPercent);

        batch.draw(fill, barX, barY, visibleWidth, barHeight,
            0, 0, srcWidth, fill.getHeight(), false, false);
    }

    private void renderBossBar(SpriteBatch batch, float screenWidth, float screenHeight) {
        float barWidth  = screenWidth * 0.6f;
        float barHeight = 100f;
        float x = screenWidth / 2f - barWidth / 2f;
        float y = screenHeight - 130f;

        batch.draw(bg, x, y, barWidth, barHeight);

        float fillPercent = getHealthPercent();
        float visibleWidth = barWidth * fillPercent;
        int srcWidth = (int)(fill.getWidth() * fillPercent);

        batch.draw(fill, x, y, visibleWidth, barHeight,
            0, 0, srcWidth, fill.getHeight(), false, false);
    }

    public float getHealthPercent() { return currentHP / maxHP; }
    public float getCurrentHP()     { return currentHP; }
    public float getMaxHP()         { return maxHP; }

    public void dispose() {
        if (bg   != null) bg.dispose();
        if (fill != null) fill.dispose();
    }
}
