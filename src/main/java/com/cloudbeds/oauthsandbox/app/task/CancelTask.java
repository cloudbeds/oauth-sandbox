package com.cloudbeds.oauthsandbox.app.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CancelTask {
    private static final Logger log = LoggerFactory.getLogger(CancelTask.class);
    private boolean cancelled;

    private boolean dismissed;

    private StackTraceElement[] creationStackTrace;

    public CancelTask() {
        creationStackTrace = new RuntimeException().fillInStackTrace().getStackTrace();
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    public boolean isCancelled() {
        return cancelled;
    }

    public void dismiss() {
        dismissed = true;
    }

    public Process attachTo(Process process) {
        new Thread(()->{
            try {
                while (process.isAlive()) {
                    if (dismissed) {
                        return;
                    }
                    if (cancelled) {
                        process.destroyForcibly();
                    }
                    Thread.sleep(200);
                }
            } catch (Exception e) {
                log.error("Error in CancelTask monitor", e);
            }
        }).start();
        return process;
    }

    public StackTraceElement[] getCreationStackTrace() {
        return creationStackTrace;
    }
}
