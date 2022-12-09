package com.cloudbeds.oauthsandbox.app.model;

import com.cloudbeds.oauthsandbox.app.di.Singleton;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.LinkedList;

@Singleton
public class NavigationModel {
    private LinkedList<Scene> navigationStack = new LinkedList<Scene>();

    public void push(Scene scene) {
        navigationStack.push(scene);
    }

    public Scene pop() {
        return navigationStack.pop();
    }

    public boolean isEmpty() {
        return navigationStack.isEmpty();
    }

    public void back(Stage stage) {
        stage.setScene(pop());
    }
}
