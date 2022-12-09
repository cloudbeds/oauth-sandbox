package com.cloudbeds.oauthsandbox.app.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    public static final int DOORLOCKS_SERVICE_ALREADY_REGISTERED = 1;
    private int code;
    private String message;

    public String getUserErrorMessage() {
        if (message != null && !message.isEmpty()) {
            return message;
        }
        switch (code) {
            case DOORLOCKS_SERVICE_ALREADY_REGISTERED:
                return "This doorlocks service is already registered";
            default:
                return "An unknown error occurred";
        }
    }

    public String getUserErrorHelp() {
        if (message != null) {
            return message;
        }
        switch (code) {
            case DOORLOCKS_SERVICE_ALREADY_REGISTERED:
                return "Go back to the main form, and press the 'Settings' button in the row corresponding to the doorlocks service you wish to edit, in order to make changes to the existing provider.";
            default:
                return "";
        }
    }

    public String getHelpUrl() {
        return "https://myfrontdesk.cloudbeds.com/hc/en-us/articles/4408608357915-Visionline-Assa-Abloy-Connection-Guide";
    }
}
