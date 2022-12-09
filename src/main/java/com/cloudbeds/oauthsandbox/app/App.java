package com.cloudbeds.oauthsandbox.app;

import com.cloudbeds.oauthsandbox.app.controller.ApplicationController;
import com.cloudbeds.oauthsandbox.app.exception.AssertionException;
import com.cloudbeds.oauthsandbox.app.model.AppModel;
import com.cloudbeds.oauthsandbox.app.service.ConfigService;
import com.cloudbeds.oauthsandbox.app.di.DI;
import com.cloudbeds.oauthsandbox.app.test.SmokeTest;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.security.KeyManagementException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import java.util.List;

/**
 * JavaFX App
 */
public class App extends Application {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    static {
        TrustManager[] trustAllCerts = { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            }
        } };
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        }
        catch (KeyManagementException | java.security.NoSuchAlgorithmException e) {
        }

        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    @Override
    public void start(Stage stage) {
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/shared/setup_icon256.png")));

        stage.setOnCloseRequest(evt->{
            log.debug("Closing");
            Platform.exit();
            System.exit(0);
        });
        final String titleSuffix = "";
        stage.setTitle("Oauth2 Sandbox" + titleSuffix);
        stage.setWidth(1200);
        stage.setHeight(800);
        System.setProperty("log4j.rootLogger", "INFO, consoleAppender");
        var javaVersion = SystemInfo.javaVersion();
        var javafxVersion = SystemInfo.javafxVersion();

        AppModel appModel = DI.get(AppModel.class);
        DI.get(ApplicationController.class).showLoginForm(stage);

    }

    public static void main(String[] args) {

        if (args.length > 0 && "smoke".equals(args[0])) {
            log.info("Running smoke test...");
            smokeTest();
            System.exit(0);
        }
        if (!"true".equals(System.getProperty("debug"))) {
            try {
                setupLogging();
            } catch (Exception ex) {
                System.err.println("Failed to setup logging");
                ex.printStackTrace();
            }
        }
        log.info("System properties: " + System.getProperties());
        smokeTest();
        launch();
    }

    private static void setupLogging() throws FileNotFoundException {
        File logFile = DI.get(ConfigService.class).getConfig().appLogFile();
        if (!logFile.getParentFile().exists()) {
            logFile.getParentFile().mkdirs();
        }
        PrintStream out = new PrintStream(new FileOutputStream(logFile));
        log.debug("Output log set to " + logFile);
        System.setOut(out);
        System.setErr(out);
    }

    private static void smokeTest() {
        try {
            DI.get(SmokeTest.class).checkConfig();
        } catch (AssertionException error) {
            log.error("Smoke test failed.  There appears to be a problem with the configuration or the environment", error);
        }
    }

}