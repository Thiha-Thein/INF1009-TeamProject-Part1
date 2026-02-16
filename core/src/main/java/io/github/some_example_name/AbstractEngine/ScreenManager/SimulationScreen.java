package io.github.some_example_name.AbstractEngine.ScreenManager;

import java.util.ArrayList;

import io.github.some_example_name.AbstractEngine.EntityManagement.Entity;
import io.github.some_example_name.AbstractEngine.EntityManagement.EntityManager;

public class SimulationScreen extends AbstractScreen {

    private Object resultData;
    private final EntityManager entityManager;

    public SimulationScreen(ScreenManager manager, EntityManager entityManager) {
        super(manager);  // Call parent constructor with manager
        if (entityManager == null) throw new IllegalArgumentException("EntityManager cannot be null");
        this.entityManager = entityManager;
    }

    @Override
    public void show(ArrayList<Entity> objects) {  // Match the abstract method signature
        setActive(true);
        
        entityManager.clear();
        
        if (objects != null) {
            for (Entity e : objects) {
                if (e != null) entityManager.addEntity(e);
            }
        }
        
        entityManager.init();
    }

    @Override
    public void hide() {
        setActive(false);
    }

    @Override
    public void pause() {
        // optional: implement pause logic
    }

    @Override
    public void resume() {
        // optional: implement resume logic
    }

    @Override
    public void update(float deltaTime) {
        if (!isActive()) return;  // Use method, not field
        entityManager.updateAll(deltaTime);
    }

    @Override
    public void render() {
        if (!isActive()) return;  // Use method, not field
        entityManager.renderAll();
    }

    public Object getResultData() { return resultData; }
    public void setResultData(Object resultData) { this.resultData = resultData; }
}