package com.cloudbeds.oauthsandbox.app.model;

import com.cloudbeds.oauthsandbox.app.service.ConfigService;
import com.cloudbeds.oauthsandbox.app.di.Inject;
import com.cloudbeds.oauthsandbox.app.di.Preference;
import com.cloudbeds.oauthsandbox.app.di.Singleton;
import com.cloudbeds.oauthsandbox.app.payload.AccessToken;

import com.cloudbeds.oauthsandbox.app.task.CancelTask;
import javafx.application.Platform;
import javafx.beans.property.*;
import lombok.Data;


@Singleton
@Data
public class AppModel {

    @Inject
    private ConfigService configService;

    private BooleanProperty runningStatusProperty = new SimpleBooleanProperty();
    private BooleanProperty connectionStatusProperty = new SimpleBooleanProperty();
    private BooleanProperty connectingProperty = new SimpleBooleanProperty();
    private BooleanProperty disconnectingProperty = new SimpleBooleanProperty();

    private StringProperty progressMessage = new SimpleStringProperty();

    @Preference
    private AccessToken accessToken;

    @Preference
    private String redirectUri;

    @Preference
    private String clientId;

    @Preference
    private String mfdServerUrl;


    private CancelTask currentTaskHandle;


    public void withLock(Runnable r) {
        if (configService.isCliMode()) {
            synchronized (this) {
                r.run();
                return;
            }
        }
        if (!Platform.isFxApplicationThread()) {
            final boolean[] complete = new boolean[1];
            Platform.runLater(()->{
                r.run();
                synchronized (complete) {
                    complete[0] = true;
                    complete.notifyAll();
                }
            });
            synchronized (complete) {
                while (!complete[0]) {
                    try {
                        complete.wait();
                    } catch (Exception ex){}
                }
            }
        } else {
            r.run();
        }
    }
}
