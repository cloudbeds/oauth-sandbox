package com.cloudbeds.oauthsandbox.app.exception;

import com.cloudbeds.oauthsandbox.app.payload.ErrorResponse;
import lombok.Data;

import java.io.IOException;

@Data
public class RestException extends IOException {
    private ErrorResponse errorResponse;
    public RestException(String message, ErrorResponse errorResponse) {
        super(message);
        this.errorResponse = errorResponse;
    }
}
