package io.github.some_example_name.AbstractEngine;

import io.github.some_example_name.AbstractEngine.EntityManagement.Entity;
import io.github.some_example_name.AbstractEngine.EntityManagement.entityInterface;

public class MainObject extends Entity implements entityInterface {

    public MainObject(int id) {
        super(id);  // sets the id in Entity
    }

    @Override
    public void update(float deltaTime){
        System.out.println("MainObject " + getId() + " updating. Delta time: " + deltaTime);
    }

    @Override
    public void render() {
        System.out.println("MainObject " + getId() + " rendering.");
    }
}

