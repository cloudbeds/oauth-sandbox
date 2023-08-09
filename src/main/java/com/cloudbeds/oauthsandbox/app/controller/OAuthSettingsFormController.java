package com.cloudbeds.oauthsandbox.app.controller;

import com.cloudbeds.oauthsandbox.app.model.AppModel;
import com.cloudbeds.oauthsandbox.app.secrets.ClientSecretManager;
import com.cloudbeds.oauthsandbox.app.service.ConfigService;
import com.cloudbeds.oauthsandbox.app.service.ErrorDialogService;
import com.cloudbeds.oauthsandbox.app.di.DI;
import com.cloudbeds.oauthsandbox.app.di.Inject;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import javafx.scene.control.TextInputDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static com.cloudbeds.oauthsandbox.app.util.URLs.splitQuery;


public class OAuthSettingsFormController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(OAuthSettingsFormController.class);

    @Inject
    private ApplicationController applicationController;

    @Inject
    private ErrorDialogService errorDialogService;

    @Inject
    private AppModel appModel;

    @Inject
    ClientSecretManager clientSecretManager;

    @Inject
    private ConfigService configService;

    @FXML
    private TextField oauthURLField;

    @FXML
    private TextField clientId;

    @FXML
    private TextField clientSecret;

    @FXML
    private TextField redirectURI;

    @FXML
    private ComboBox<String> mfdServerList;


    public OAuthSettingsFormController() {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        clientId.setText(notNull(appModel.getClientId()));
        clientSecret.setText(notNull(clientSecretManager.getSecretForClientID(appModel.getClientId())));
        redirectURI.setText(notNull(appModel.getRedirectUri()));
        List<String> options = new ArrayList<>(Arrays.asList(
                "https://hotels.cloudbeds.com",
                "https://hotels.cloudbeds-dev.com",
                "https://hotels.stage-ga.cloudbeds-dev.com",
                "https://hotels.acessa.loc"
                )
        );
        if (appModel.getMfdServerUrl() != null && !options.contains(appModel.getMfdServerUrl())) {
            options.add(appModel.getMfdServerUrl());
        }
        mfdServerList.getItems().addAll(options);
        if (appModel.getMfdServerUrl() != null) {
            mfdServerList.getSelectionModel().select(appModel.getMfdServerUrl());
        }

    }


    private static String notNull(String str) {
        if (str == null) return "";
        return str;
    }

    @FXML
    private void save() {
        appModel.setClientId(clientId.getText());
        try {
            clientSecretManager.setSecretForClientID(appModel.getClientId(), clientSecret.getText());
        } catch (Exception ex) {
            log.error("Failed to save client secret", ex);
        }
        appModel.setRedirectUri(redirectURI.getText());
        appModel.setMfdServerUrl(mfdServerList.getSelectionModel().getSelectedItem());
        DI.savePreferences(appModel);

        applicationController.showLoginForm();
    }

    @FXML
    private void cancel() {
        applicationController.back();
    }

    @FXML
    private void clientIDChanged() {
        String clientSecret = clientSecretManager.getSecretForClientID(clientId.getText());
        if (clientSecret != null) {
            this.clientSecret.setText(clientSecret);
        }
    }

    @FXML
    private void oauthURLChanged() {
        String newUrl = oauthURLField.getText();
        if (newUrl == null || newUrl.isEmpty()) {
            return;
        }
        try {
            URL url = new URL(newUrl);
            String host = url.getHost();
            Map<String, String> splitQuery = splitQuery(url);
            String oldClientId = appModel.getClientId();
            if (splitQuery.containsKey("client_id") && !Objects.equals(oldClientId, splitQuery.get("client_id"))) {
                clientId.setText(splitQuery.get("client_id"));
                clientIDChanged();
            }
            if (splitQuery.containsKey("redirect_uri")) {
                redirectURI.setText(splitQuery.get("redirect_uri"));
            }
            mfdServerList.getSelectionModel().select("https://" + host);
        } catch (MalformedURLException e) {
            log.error("Error occurred while processing Oauth url", e);
        } catch (UnsupportedEncodingException e) {
            log.error("Error occurred while processing Oauth url", e);
        }
    }

}
