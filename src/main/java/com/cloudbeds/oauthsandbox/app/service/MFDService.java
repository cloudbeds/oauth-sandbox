package com.cloudbeds.oauthsandbox.app.service;

import com.cloudbeds.oauthsandbox.app.model.AppModel;
import com.cloudbeds.oauthsandbox.app.payload.AccessToken;
import com.cloudbeds.oauthsandbox.app.secrets.ClientSecretManager;
import com.cloudbeds.oauthsandbox.app.util.MapBuilder;
import com.cloudbeds.oauthsandbox.app.di.Inject;
import com.cloudbeds.oauthsandbox.app.di.Singleton;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import static com.cloudbeds.oauthsandbox.app.util.Strings.empty;

@Singleton
@Data
public class MFDService {

    @Inject
    private ConfigService configService;

    private static final Logger log = LoggerFactory.getLogger(MFDService.class);

    private static final String SCOPE = "read:hotel";

    private AccessToken accessToken;

    @Inject
    private FetchService fetchService;

    @Inject
    private AppModel appModel;

    @Inject
    private ClientSecretManager clientSecretManager;

    public Optional<String> extractCodeFromUrl(String url) {
        try {
            log.debug("Extracting code from URL {}", url);
            URIBuilder uriBuilder = new URIBuilder(new URI(url));
            NameValuePair codePair = uriBuilder.getQueryParams().stream()
                    .filter(pair -> "code".equals(pair.getName()))
                    .findFirst()
                    .orElse(null);
            if (codePair != null) {
                return Optional.of(codePair.getValue());
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public AccessToken getAccessToken(String code) throws IOException {
        log.debug("Trying to get access token for login code {} from BASE_URL={}", code, BASE_URL());
        Map params = new MapBuilder()
                .put("code", code)
                .put("client_id", CLIENT_ID())
                .put("client_secret", CLIENT_SECRET())
                .put("grant_type", "authorization_code")
                .put("redirect_uri", REDIRECT_URI())
                .put("scope", SCOPE)
                .build();
        String url = BASE_URL() + "/api/v1.1/access_token";
        log.debug("POST {} with Params {}", url, params);
        String response = fetchService.postAndReturnString(url, params);
        accessToken =  new ObjectMapper().readValue(response, AccessToken.class);
        return accessToken;

    }

    public String getLoginUrl() {
        return OAUTH_URL().replace("[CLIENT_ID]", URLEncoder.encode(CLIENT_ID(), StandardCharsets.UTF_8))
                .replace("[REDIRECT_URI]", URLEncoder.encode(REDIRECT_URI(), StandardCharsets.UTF_8))
                .replace("[SCOPE]", URLEncoder.encode(SCOPE, StandardCharsets.UTF_8));
    }

    public boolean isRedirectUrl(String url) {
        return url.contains("code=");
    }

    private String OAUTH_URL() {
        return BASE_URL() + "/api/v1.1/oauth?client_id=[CLIENT_ID]&redirect_uri=[REDIRECT_URI]&response_type=code&scope=[SCOPE]";
    }

    private String CLIENT_ID() {
        return empty(appModel.getClientId());
    }

    private String CLIENT_SECRET() {
        return empty(clientSecretManager.getSecretForClientID(appModel.getClientId()));
    }

    private String REDIRECT_URI() {
        return empty(appModel.getRedirectUri());
    }

    private String BASE_URL() {
        return empty(appModel.getMfdServerUrl());
    }

}
