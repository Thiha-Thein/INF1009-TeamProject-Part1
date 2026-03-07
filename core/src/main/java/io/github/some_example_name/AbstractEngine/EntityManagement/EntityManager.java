package io.github.some_example_name.AbstractEngine.EntityManagement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// Owns all active game entities and drives their lifecycle — add, start, update, render, remove, and clear
public class EntityManager {

    private ArrayList<AbstractEntity> EntityArrayList = new ArrayList<>();
    private int nextId = 0; // monotonically increasing counter so every entity gets a unique ID

    public EntityManager() {
        System.out.println("EntityManager: Initialized");
    }

    // Adds an entity to the world, assigns it a unique ID and marks it active immediately
    public void addEntity(AbstractEntity abstractEntity){
        EntityArrayList.add(abstractEntity);
        abstractEntity.setId(nextId++);
        abstractEntity.setActive(true);
        System.out.println("EntityManager: " + abstractEntity.getId() + " added.");
    }

    // Immediately disposes and removes an entity — use markForRemoval() instead if called during an update loop
    public void removeEntity(AbstractEntity abstractEntity){
        System.out.println("EntityManager: " + abstractEntity.getTag() + " removed.");
        abstractEntity.dispose();
        EntityArrayList.remove(abstractEntity);
    }

    // Calls start() on all active entities — must be called once after all entities have been added to the world
    public void start(){
        System.out.println("Entity Manager: Started");
        for (AbstractEntity e: EntityArrayList){
            if (e.isActive()) {
                e.start();
            }
        }
    }

    // Advances all active entities by one frame — animation is ticked before entity update so it uses the latest deltaTime
    // Entities flagged with markForRemoval() are cleaned up after the loop to avoid ConcurrentModificationException
    public void updateAll(float deltaTime){
        for (AbstractEntity e: EntityArrayList){
            if (e.isActive()) {
                if (e.getAnimationRenderer() != null) {
                    e.getAnimationRenderer().update(deltaTime);
                }
                e.update(deltaTime);
            }
        }
        EntityArrayList.removeIf(AbstractEntity::isPendingRemoval);
    }

    // Draws all active entities in list order — add entities in the desired draw order when building the scene
    public void renderAll(SpriteBatch batch){
        for (AbstractEntity e: EntityArrayList){
            if (e.isActive()) {
                e.render(batch);
            }
        }
    }

    // Finds and returns the first active entity with a matching tag — returns null if not found
    public AbstractEntity findByTag(String tag) {
        for (AbstractEntity e : EntityArrayList) {
            if (e.isActive() && tag.equals(e.getTag())) {
                return e;
            }
        }
        return null;
    }

    // Returns an unmodifiable view of the entity list so systems can iterate without risk of external modification
    public List<AbstractEntity> getEntities() {
        return Collections.unmodifiableList(EntityArrayList);
    }

    // Disposes all entities and empties the list — called when a simulation world is unloaded
    public void clear() {
        System.out.println("EntityManager: Clearing all entities...");
        for (AbstractEntity e : EntityArrayList) {
            e.dispose();
        }
        EntityArrayList.clear();
        System.out.println("EntityManager: All entities cleared");
    }
}
