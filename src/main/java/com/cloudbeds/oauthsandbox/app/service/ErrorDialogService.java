package com.cloudbeds.oauthsandbox.app.service;

import com.cloudbeds.oauthsandbox.app.di.Singleton;
import com.cloudbeds.oauthsandbox.app.exception.RestException;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.ConnectException;
import java.net.URI;

@Singleton
public class ErrorDialogService {

    private static final Logger log = LoggerFactory.getLogger(ErrorDialogService.class);

    public void showError(String message) {
        showError(message, (Runnable)null);
    }

    public void showError(String message, Runnable then) {
        showError(message, (String)null, then);
    }

    public void showError(String message, String helpUrl) {
        showError(message, helpUrl, (Runnable)null);
    }

    public void showError(String message, String helpUrl, Runnable then) {

        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(()->{
                showError(message, helpUrl, then);
            });
            return;
        }
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        log.debug("Help URL is {}", helpUrl);
        if (helpUrl != null && !helpUrl.isEmpty()) {
            ButtonType help = new ButtonType("Help");
            alert.getButtonTypes().add(help);
            final Button helpBtn = (Button) alert.getDialogPane().lookupButton(help);
            helpBtn.setOnAction(evt -> {
                if (Desktop.isDesktopSupported()) {
                    try {
                        log.debug("About to browse to {}", helpUrl);
                        Desktop.getDesktop().browse(new URI(helpUrl));
                    } catch (Exception ex) {
                        log.error("Failed to navigate to URL {}", helpUrl, ex);
                    }
                }
            });


        }

        alert.showAndWait();
        if (then != null) {
            then.run();
        }
    }

    public void showError(Throwable exception) {
        log.error("showError()", new RuntimeException(exception));
        showError(getErrorMessage(exception), getErrorHelpUrl(exception));
    }

    public String getErrorMessage(Throwable t) {
        if (t instanceof RestException) {
            return ((RestException)t).getErrorResponse().getUserErrorMessage();
        }
        if (t instanceof ConnectException) {
            return String.format("Connection failed.  Try toggling the service switch to the 'On' position and try again. \nSpecific error message:\n%s", t.getMessage());
        }
        if (t.getCause() != null) {
            return getErrorMessage(t.getCause());
        }
        return t.getLocalizedMessage();
    }

    public String getErrorHelp(Throwable t) {
        if (t instanceof RestException) {
            return ((RestException)t).getErrorResponse().getUserErrorHelp();
        }
        return "";
    }

    public String getErrorHelpUrl(Throwable t) {
        log.debug("getErrorHelpUrl({})", t.getClass());
        if (t instanceof RestException) {
            return ((RestException)t).getErrorResponse().getHelpUrl();
        }
        if (t.getCause() != null) {
            return getErrorHelpUrl(t.getCause());
        }
        return "https://myfrontdesk.cloudbeds.com/hc/en-us/articles/4408608357915-Visionline-Assa-Abloy-Connection-Guide";
    }
}
