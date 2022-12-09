package com.cloudbeds.oauthsandbox.app.service;

import com.cloudbeds.oauthsandbox.app.di.Singleton;
import com.cloudbeds.oauthsandbox.app.task.CancelTask;

@Singleton
public class TaskManager {
    private CancelTask cancelTask;

    public CancelTask createPrimaryCancelTask() {
        if (cancelTask != null) {
            cancelTask.dismiss();
        }
        cancelTask = new CancelTask();

        return cancelTask;
    }

    public CancelTask createSecondaryCancelTask() {
        return new CancelTask();
    }

}
