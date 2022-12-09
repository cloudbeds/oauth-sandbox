package com.cloudbeds.oauthsandbox.app.service;

import com.cloudbeds.oauthsandbox.app.di.Inject;
import com.cloudbeds.oauthsandbox.app.di.Singleton;
import com.cloudbeds.oauthsandbox.app.model.AppModel;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

@Singleton
public class CookieService {

    @Inject
    private AppModel appModel;

    public CompletableFuture<Void> clearCookies(WebEngine engine) {
        CompletableFuture<Void> out = new CompletableFuture<>();
        engine.load(appModel.getMfdServerUrl() + "/auth/logout");
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(()->{
                    out.complete(null);
                });
            }
        };
        timer.schedule(timerTask, 2000);
        return out;
    }
}
