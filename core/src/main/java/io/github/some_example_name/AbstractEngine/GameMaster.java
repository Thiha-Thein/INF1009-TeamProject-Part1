package io.github.some_example_name.AbstractEngine;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import io.github.some_example_name.AbstractEngine.Audio.SoundManager;
import io.github.some_example_name.AbstractEngine.EntityManagement.EntityManager;
import io.github.some_example_name.AbstractEngine.IO.IOManager;

public class GameMaster extends ApplicationAdapter {

    private EntityManager entityManager;
    private IOManager ioManager;
    private SoundManager soundManager;

    private boolean isInitialized = false;

    @Override
    public void create() {
        start();
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        update(deltaTime);
    }

    public void start() {
        if (isInitialized) return;

        System.out.println("GameMaster: Starting game...");

        // Managers
        entityManager = new EntityManager();
        ioManager = new IOManager();
        soundManager = new SoundManager();
        
        // Load audio
        soundManager.loadSound("jumpSfx", "sfx/jump.wav");
        soundManager.loadMusic("bgm", "music/bgm.mp3");

        // Start background music
        soundManager.playMusic("bgm", true);

        ioManager.bindKey("up", Input.Keys.W);
        ioManager.bindKey("down", Input.Keys.S);
        ioManager.bindKey("left", Input.Keys.A);
        ioManager.bindKey("right", Input.Keys.D);
        ioManager.bindKey("jump", Input.Keys.SPACE);

        
        MainObject player = new MainObject(ioManager, soundManager);
        entityManager.addEntity(player);

        isInitialized = true;
        entityManager.init();

        System.out.println("GameMaster: Game started!");
    }

    // Unity-style Update method - called every frame
    public void update(float deltaTime) {
        if (!isInitialized) return;

        ioManager.update();

        if (ioManager.wasPressed("jump")) { 
        	soundManager.playSound("jumpSfx");
        }

        entityManager.updateAll(deltaTime);
        entityManager.renderAll();
    }

    @Override
    public void dispose() {
        System.out.println("GameMaster: Cleaning up...");

        if (entityManager != null) entityManager.clear();
        if (soundManager != null) soundManager.dispose();

        System.out.println("GameMaster: Cleanup complete!");
    }

    // Getters
    public EntityManager getEntityManager() { return entityManager; }
    public IOManager getIOManager() { return ioManager; }
    public SoundManager getSoundManager() { return soundManager; }
}
