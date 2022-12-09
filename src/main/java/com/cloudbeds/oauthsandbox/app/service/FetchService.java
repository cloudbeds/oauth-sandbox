package com.cloudbeds.oauthsandbox.app.service;

import com.cloudbeds.oauthsandbox.app.di.Singleton;

import com.cloudbeds.oauthsandbox.app.exception.RestException;
import com.cloudbeds.oauthsandbox.app.payload.AccessToken;

import com.cloudbeds.oauthsandbox.app.payload.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class FetchService {
    private static final Logger log = LoggerFactory.getLogger(FetchService.class);
    public String postAndReturnString(String url, Map<String,Object> postData) throws IOException {
        return postAndReturnString(url, null, postData);
    }

    public void post(String url) throws IOException {
        postAndReturnString(url, null, new HashMap<String,Object>());
    }

    public String postAndReturnString(String url, AccessToken accessToken, Map<String,Object> postData) throws IOException {
        return postAndReturnString(url, accessToken, postData, -1);
    }

    public String postAndReturnString(String url, AccessToken accessToken, Map<String,Object> postData, int timeout) throws IOException {
        CloseableHttpClient client = createClient(timeout);
        HttpPost httpPost = new HttpPost(url);
        if (accessToken != null) {
            httpPost.addHeader(new BasicHeader("Authorization", "Bearer " + accessToken.getAccessToken()));
        }
        var nameValuePairList = new ArrayList<NameValuePair>();
        for (String key : postData.keySet()) {
            nameValuePairList.add(new BasicNameValuePair(key, postData.get(key).toString()));
        }
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairList));
        CloseableHttpResponse response = client.execute(httpPost);
        try {
            throwErrorResponse(response);
            return EntityUtils.toString(response.getEntity());
        } finally {
            client.close();
        }
    }

    public String getAndReturnString(String url) throws IOException {
        return getAndReturnString(url, null);
    }

    public String getAndReturnString(String url, AccessToken accessToken) throws IOException {
        return getAndReturnString(url, accessToken, -1);
    }


    public String getAndReturnString(final String url, final AccessToken accessToken, final int timeout) throws IOException {
        //log.debug("getAndReturnString({}, {}, {})", url, accessToken, timeout);
        final CloseableHttpClient client = createClient(timeout);
        final HttpGet httpGet = new HttpGet(url);

        if (accessToken != null) {
            httpGet.addHeader(new BasicHeader("Authorization", "Bearer " + accessToken.getAccessToken()));
        }

        final CloseableHttpResponse response = client.execute(httpGet);
        try {
            throwErrorResponse(response);
            return EntityUtils.toString(response.getEntity());
        } finally {
            client.close();
        }
    }

    public void deleteAndReturnString(String url) throws IOException {
        CloseableHttpClient client = createClient(-1);

        HttpDelete httpGet = new HttpDelete(url);

        CloseableHttpResponse response = client.execute(httpGet);
        try {
            throwErrorResponse(response);
        } finally {
            client.close();
        }
    }

    public String postJsonAndReturnString(String url, Object body) throws IOException {
        return postJsonAndReturnString(url, body, -1);
    }

    public String postJsonAndReturnString(String url, Object body, int timeout) throws IOException {
        CloseableHttpClient client = createClient(timeout);
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader(new BasicHeader("content-type", "application/json"));
        httpPost.setEntity(new StringEntity(new ObjectMapper().writeValueAsString(body)));

        CloseableHttpResponse response = client.execute(httpPost);
        try {
            throwErrorResponse(response);
            return EntityUtils.toString(response.getEntity());
        } finally {
            client.close();
        }
    }
    public String putJsonAndReturnString(String url, Object body) throws IOException {
        CloseableHttpClient client = createClient(-1);
        HttpPut httpPut = new HttpPut(url);
        httpPut.addHeader(new BasicHeader("content-type", "application/json"));
        httpPut.setEntity(new StringEntity(new ObjectMapper().writeValueAsString(body)));

        CloseableHttpResponse response = client.execute(httpPut);
        try {
            throwErrorResponse(response);
            return EntityUtils.toString(response.getEntity());
        } finally {
            client.close();
        }
    }

    private void throwErrorResponse(CloseableHttpResponse response) throws RestException {
        if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() > 299) {
            log.debug("HTTP Response: {} Code={}", response.getStatusLine().getReasonPhrase(), response.getStatusLine().getStatusCode());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .message(response.getStatusLine().getReasonPhrase())
                    .code(response.getStatusLine().getStatusCode())
                    .build();
            try {
                if (response.getEntity() != null) {
                    errorResponse = new ObjectMapper().readValue(EntityUtils.toString(response.getEntity()), ErrorResponse.class);
                }
            } catch (Exception ex){
                if (log.isDebugEnabled()) {
                    log.error("throwErrorResponse({})", response, ex);
                }
            }

            throw new RestException(response.getStatusLine().getReasonPhrase(), errorResponse);
        }
    }

    private CloseableHttpClient createClient(final int timeout) throws IOException {
        final CloseableHttpClient client;
        if (timeout > 0) {
            client = HttpClientBuilder
                    .create()
                    .setDefaultRequestConfig(
                            RequestConfig
                                    .custom()
                                    .setConnectTimeout(timeout)
                                    .setConnectionRequestTimeout(timeout)
                                    .setSocketTimeout(timeout)
                                    .build()
                    )
                    .build();
        } else {
            client = HttpClients.createDefault();
        }

        return client;
    }
}
