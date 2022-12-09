package com.cloudbeds.oauthsandbox.app.secrets;

import com.cloudbeds.oauthsandbox.app.di.Singleton;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

@Singleton
public class ClientSecretManager {
    public String getSecretForClientID(String clientID) {
        if (clientID == null) {
            return null;
        }
        Preferences prefs = Preferences.userNodeForPackage(ClientSecretManager.class);
        Preferences secretsNode = prefs.node("secrets");
        return secretsNode.get(clientID, null);
    }

    public void setSecretForClientID(String clientID, String clientSecret) throws BackingStoreException {
        if (clientID == null) {
            return;
        }

        Preferences prefs = Preferences.userNodeForPackage(ClientSecretManager.class);
        Preferences secretsNode = prefs.node("secrets");
        if (clientSecret == null) {
            secretsNode.remove(clientID);
        } else {
            secretsNode.put(clientID, clientSecret);
        }
        prefs.flush();
    }
}
