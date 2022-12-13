package com.cloudbeds.oauthsandbox.app.controller;

import com.cloudbeds.oauthsandbox.app.di.DI;
import com.cloudbeds.oauthsandbox.app.di.Inject;
import com.cloudbeds.oauthsandbox.app.model.AppModel;
import com.cloudbeds.oauthsandbox.app.payload.AccessToken;
import com.cloudbeds.oauthsandbox.app.secrets.ClientSecretManager;
import com.cloudbeds.oauthsandbox.app.service.ConfigService;
import com.cloudbeds.oauthsandbox.app.service.CookieService;
import com.cloudbeds.oauthsandbox.app.service.ErrorDialogService;
import com.cloudbeds.oauthsandbox.app.service.MFDService;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;

import static com.cloudbeds.oauthsandbox.app.util.URLs.splitQuery;
import static javafx.application.Platform.runLater;


public class LoginFormController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(LoginFormController.class);

    @Inject
    private ApplicationController applicationController;

    @Inject
    private ErrorDialogService errorDialogService;

    @Inject
    private AppModel appModel;

    @Inject
    private ClientSecretManager clientSecretManager;

    @Inject
    private ConfigService configService;

    @Inject
    private CookieService cookieService;

    @FXML
    private WebView webView;


    @FXML
    private TextArea accessTokenField;

    @FXML
    private TextArea refreshTokenField;

    @FXML
    private Label status;

    @Inject
    private MFDService mfdService;


    public LoginFormController() {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        status.setText("");
        WebEngine engine = webView.getEngine();
        log.debug("Trying to open URL {}", mfdService.getLoginUrl());

        engine.load(mfdService.getLoginUrl());

        update();

        if (!isOAuthConfigured()) {
            runLater(() -> {
                applicationController.showOAuthSettingsForm(null);
            });

            return;
        }


        engine.locationProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                log.debug("Location changed: " + newValue);
                if (mfdService.isRedirectUrl(newValue)) {
                    log.debug("Found redirect " + newValue + ". Cancelling load");
                    engine.getLoadWorker().cancel();
                    processRedirectUrl(newValue);
                }
            }
        });

        engine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState) {
                log.debug("Location: " + engine.getLocation() + " state = " + newState);
                if (newState != Worker.State.SUCCEEDED) {
                    webView.setVisible(false);
                    applicationController.showProgressModal(null, "Loading...");
                } else {
                    webView.setVisible(true);
                    applicationController.hideProgressModal();
                }
            }
        });
    }


    private void update() {
        AccessToken accessToken = appModel.getAccessToken();
        if (accessToken != null) {
            accessTokenField.setText(accessToken.getAccessToken());
            refreshTokenField.setText(accessToken.getRefreshToken());
        }
    }

    private void processRedirectUrl(String url) {
        webView.setVisible(false);
        applicationController.showProgressModal(null, "Verifying Credentials...");
        if (Platform.isFxApplicationThread()) {
            new Thread(() -> {
                processRedirectUrl(url);
            }).start();
            return;
        }


        try {
            String code = mfdService.extractCodeFromUrl(url).orElseThrow();
            mfdService.getAccessToken(code);
            runLater(()->{
                appModel.setAccessToken(mfdService.getAccessToken());
                DI.savePreferences(appModel);
                setStatus("Login was successful.  Your access token and refresh token are shown in the fields above");
                refresh();
            });


        } catch (IOException ex) {
            log.error("Failed to get access token", ex);
            errorDialogService.showError(
                    "Failed to get access token: "+ex.getMessage(),
                    null,
                    ()->{
                applicationController.hideProgressModal();
                log.debug("Returning to login form");
                applicationController.showLoginForm();
            });
        }


    }

    private boolean isOAuthConfigured() {
        return !(isEmpty(appModel.getClientId()) ||
                isEmpty(clientSecretManager.getSecretForClientID(appModel.getClientId())) ||
                isEmpty(appModel.getRedirectUri()) ||
                isEmpty(appModel.getMfdServerUrl())
        );
    }

    @FXML
    private void refresh() {
        cookieService.clearCookies(webView.getEngine()).thenAccept(arg -> {
            update();
            webView.getEngine().load(mfdService.getLoginUrl());
        });
    }

    @FXML
    private void openOauthSettings() {
        applicationController.showOAuthSettingsForm(null);
    }

    @FXML
    private void accessTokenClicked() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(accessTokenField.getText()), null);
            setStatus("Access token copied to clipboard");
        } catch (Exception ex) {
            log.error("Failed to copy access token to clipboard", ex);
        }
    }

    @FXML
    private void refreshTokenClicked() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(refreshTokenField.getText()), null);
            setStatus("Refresh token copied to clipboard");
        } catch (Exception ex) {
            log.error("Failed to copy access token to clipboard", ex);
        }
    }



    private boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    private Timer clearStatusTimer;
    private void setStatus(String statusString) {
        status.setText(statusString);
        if (clearStatusTimer != null) {
            clearStatusTimer.cancel();
            clearStatusTimer = null;
        }
        clearStatusTimer = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                clearStatusTimer = null;
                Platform.runLater(()->status.setText(""));
            }
        };
        clearStatusTimer.schedule(tt, 5000);
    }
}
