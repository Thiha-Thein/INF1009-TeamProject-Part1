package io.github.some_example_name.AbstractEngine.EntityManagement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class EntityManager {

    private ArrayList<AbstractEntity> EntityArrayList = new ArrayList<>();
    private int nextId = 0;  // To generate unique IDs for entities
    public EntityManager() {
        System.out.println("EntityManager: Initialized");
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
        System.out.println("Entity Manager: Started");
        for (AbstractEntity e: EntityArrayList){
            if (e.isActive()) {
                e.start();
            }
        }
    }

    public void updateAll(float deltaTime){
        for (AbstractEntity e: EntityArrayList){
            if (e.isActive()) {
                if (e.getAnimationRenderer() != null) {
                    e.getAnimationRenderer().update(deltaTime);  // ADD THIS
                }
                e.update(deltaTime);
            }
        }
        // remove entities flagged for removal after update loop
        EntityArrayList.removeIf(AbstractEntity::isPendingRemoval);
    }

    public void renderAll(SpriteBatch batch){
        for (AbstractEntity e: EntityArrayList){
            if (e.isActive()) {
                e.render(batch);
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

    public List<AbstractEntity> getEntities() {
        return Collections.unmodifiableList(EntityArrayList);
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
