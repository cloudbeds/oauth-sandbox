package com.cloudbeds.oauthsandbox.app.test;

import com.cloudbeds.oauthsandbox.app.di.Inject;
import com.cloudbeds.oauthsandbox.app.di.Singleton;
import com.cloudbeds.oauthsandbox.app.exception.AssertionException;
import com.cloudbeds.oauthsandbox.app.service.ConfigService;

import java.io.File;

@Singleton
public class SmokeTest {
    @Inject
    private ConfigService configService;


    public void checkConfig() throws AssertionException {



    }

    private void assertFileExists(File file) throws AssertionException {
        if (!file.exists()) {
            throw new AssertionException("File " + file + " is expected to exist");
        }
    }

    private void assertNonEmpty(String key, String value) throws AssertionException {
        if (value == null || value.isEmpty()) {
            throw new AssertionException("Config param " + key + " is expected to be non-empty.");
        }
    }
}
