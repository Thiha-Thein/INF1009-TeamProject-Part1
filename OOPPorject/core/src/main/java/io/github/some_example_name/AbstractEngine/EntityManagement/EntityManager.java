package io.github.some_example_name.AbstractEngine.EntityManagement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sun.font.TrueTypeFont;


public class EntityManager {

    private static EntityManager instance;

    private ArrayList<AbstractEntity> EntityArrayList = new ArrayList<>();
    private int nextId = 0;  // To generate unique IDs for entities
    private EntityManager() {
        System.out.println("EntityManager: Initialized");
    }

    public static EntityManager getInstance() {
        if (instance == null) {
            instance = new EntityManager();
        }
        return instance;
    }

    public void addEntity(AbstractEntity abstractEntity){
        EntityArrayList.add(abstractEntity);
        abstractEntity.setId(nextId++);
        abstractEntity.setActive(true);
        System.out.println("EntityManager: " + abstractEntity.getId() + " added.");
    }

    public void removeEntity(AbstractEntity abstractEntity){
        System.out.println("EntityManager: " + abstractEntity.getTag() + " removed.");
        abstractEntity.dispose();
        EntityArrayList.remove(abstractEntity);
    }

    public void start(){
        for (AbstractEntity e: EntityArrayList){
            if (e.isActive()) {
                System.out.println("Entity Manager: Started");
                e.start();
            }
        }
    }

    public void updateAll(float deltaTime){
        for (AbstractEntity e: EntityArrayList){
            if (e.isActive()) {
                e.update(deltaTime);
            }
        }
    }

    public void renderAll(){
        for (AbstractEntity e: EntityArrayList){
            if (e.isActive()) {
                e.render();
            }
        }
    }

    public AbstractEntity findByTag(String tag) {
        for (AbstractEntity e : EntityArrayList) {
            if (e.isActive() && tag.equals(e.getTag())) {
                return e; // first match
            }
        }
        return null;
    }


    public void clear() {
        System.out.println("EntityManager: Clearing all entities...");
        for (AbstractEntity e : EntityArrayList) {
            e.dispose();
        }
        EntityArrayList.clear();
        System.out.println("EntityManager: All entities cleared");
    }
}
