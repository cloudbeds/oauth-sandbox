package com.cloudbeds.oauthsandbox.app.controller;

import com.cloudbeds.oauthsandbox.app.service.ConfigService;
import com.cloudbeds.oauthsandbox.app.service.ErrorDialogService;
import com.cloudbeds.oauthsandbox.app.di.DI;
import com.cloudbeds.oauthsandbox.app.di.Inject;
import com.cloudbeds.oauthsandbox.app.di.Singleton;
import com.cloudbeds.oauthsandbox.app.model.AppModel;
import com.cloudbeds.oauthsandbox.app.model.NavigationModel;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ApplicationController {
    Logger log = LoggerFactory.getLogger(ApplicationController.class);

    private int preferredWindowWidth() {
        return configService.getConfig().preferredWindowWidth();
    };
    private int getPreferredWindowHeight() {
        return configService.getConfig().getPreferredWindowHeight();
    }



    @Inject
    private NavigationModel navigationModel;

    @Inject
    AppModel appModel;

    @Inject
    ErrorDialogService errorDialogService;

    @Inject
    ConfigService configService;

    private Stage primaryStage;

    public ApplicationController() {

    }

    public void showLoginForm() {
        showLoginForm(primaryStage);
    }

    public void showLoginForm(Stage stage) {
        primaryStage = stage == null ? primaryStage : stage;
        stage = primaryStage;
        var scene = new Scene(DI.loadFXML(getClass(), "/fxml/LoginForm.fxml").getRoot(), 1200 , 800);
        while (!navigationModel.isEmpty()) {
            navigationModel.pop();
        }
        stage.setScene(scene);
        stage.show();
    }

    public void showOAuthSettingsForm(Stage stage) {
        primaryStage = stage == null ? primaryStage : stage;
        stage = primaryStage;
        var scene = new Scene(DI.loadFXML(getClass(), "/fxml/OAuthSettingsForm.fxml").getRoot(), 640, 480);
        navigationModel.push(primaryStage.getScene());
        stage.setScene(scene);
        stage.show();
    }


    public void showProgressDialog(Stage stage) {
        stage.setScene(new Scene(DI.loadFXML(getClass(), "/fxml/ConnectionProgressPanel.fxml").getRoot(), 640, 480));
    }

    public void back() {
        navigationModel.back(primaryStage);
    }

    public boolean canGoBack() {
        return !navigationModel.isEmpty();
    }

    private Dialog progressModalDialog;

    public void showProgressModal(Stage stage, String message) {
        showProgressModal(stage, message, (Runnable)null);
    }
    public void showProgressModal(Stage stage, String message, Runnable onCancel){
        if (!Platform.isFxApplicationThread()) {
            Stage fStage = stage;
            Platform.runLater(()->showProgressModal(fStage, message));
            return;
        }
        primaryStage = stage == null ? primaryStage : stage;
        stage = primaryStage;
        log.debug("Showing progress modal for message "+ message);
        hideProgressModal();
        Dialog<Void> dialog = new Dialog<>();
        progressModalDialog = dialog;
        dialog.initModality(Modality.NONE);
        dialog.initOwner(stage);
        dialog.initStyle(StageStyle.TRANSPARENT);

        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setPrefSize(30, 30);

        Label label = new Label(message);
        label.setMinWidth(Label.USE_PREF_SIZE);

        VBox content = new VBox(10, indicator, label);

        content.setPadding(new Insets(10));
        content.setAlignment(Pos.CENTER);
        dialog.getDialogPane().setBackground(
                new Background(
                        new BackgroundFill(
                                new Color(0, 0, 0, 0.1),
                                new CornerRadii(2),
                                null
                        )
                )
        );

        dialog.getDialogPane().setContent(content);

        if (onCancel != null) {
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
            ((Button)dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setOnAction(evt -> {
                log.debug("onCancel was clicked");
                Platform.runLater(onCancel);
            });
        }

        dialog.show();
    }

    public void hideProgressModal() {
        if (progressModalDialog != null) {
            if (!Platform.isFxApplicationThread()) {
                Platform.runLater(()->hideProgressModal());
                return;
            }
            log.debug("Hiding progress modal");
            progressModalDialog.setResult(true);
            progressModalDialog.hide();
            progressModalDialog.close();
            progressModalDialog = null;
        }
    }

}
