package com.cloudbeds.oauthsandbox.app.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown=true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessToken {
    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private long expiresIn;

    @JsonProperty("access_token")
    private String accessToken;
}
