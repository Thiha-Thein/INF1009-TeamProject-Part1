package io.github.some_example_name.AbstractEngine.EntityManagement;
import java.util.ArrayList;
import java.util.List;


public class EntityManager {
    private ArrayList<Entity> entityArrayList;
    private int nextID = 0;

    public EntityManager(){
        entityArrayList = new ArrayList<>();
    }

    public void addEntity(Entity entity){
        entityArrayList.add(entity);
        entity.setId(nextID++);
        System.out.println("EntityManager: " + entity.getTag() + " added.");
    }

    public void removeEntity(Entity entity){
        System.out.println("EntityManager: " + entity.getTag() + " removed.");
        entityArrayList.remove(entity);

    }

    public void init(){
        for (Entity e: entityArrayList){
            if (e.isActive()) {
                e.start();
                System.out.println("EntityManager: " + e.getTag() + " started.");
            }
        }
    }

    public void updateAll(float deltaTime){
        for (Entity e: entityArrayList){
            if (e.isActive()) {
                e.update(deltaTime);
            }
        }
    }

    public void renderAll(){
        for (Entity e: entityArrayList){
            if (e.isActive()) {
                e.render();
            }
        }
    }

    public ArrayList<Entity> getEntities() {
        return entityArrayList;
    }

    public void clear() {
        System.out.println("EntityManager: Clearing all entities...");
        for (Entity e : entityArrayList) {
            e.dispose();
        }
        entityArrayList.clear();
        System.out.println("EntityManager: All entities cleared");
    }
}
