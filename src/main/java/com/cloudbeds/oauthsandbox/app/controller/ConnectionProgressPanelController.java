package com.cloudbeds.oauthsandbox.app.controller;

import com.cloudbeds.oauthsandbox.app.di.Inject;
import com.cloudbeds.oauthsandbox.app.model.AppModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class ConnectionProgressPanelController implements Initializable {

    @Inject
    AppModel appModel;

    @FXML
    private Label progressLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        appModel.withLock(()->{
            progressLabel.textProperty().bind(appModel.getProgressMessage());
        });
    }
}
