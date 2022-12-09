package com.cloudbeds.oauthsandbox.app.exception;

public class AssertionException extends Exception {
    public AssertionException(String message) {
        super("Assertion failed: " + message);
    }
}
