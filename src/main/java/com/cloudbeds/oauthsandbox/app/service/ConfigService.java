package com.cloudbeds.oauthsandbox.app.service;

import com.cloudbeds.oauthsandbox.app.config.Config;
import com.cloudbeds.oauthsandbox.app.di.DI;
import com.cloudbeds.oauthsandbox.app.di.Inject;
import com.cloudbeds.oauthsandbox.app.di.Provider;
import com.cloudbeds.oauthsandbox.app.di.Singleton;
import com.cloudbeds.oauthsandbox.app.model.AppModel;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;

@Singleton
@Data
public class ConfigService {

    private static final Logger log = LoggerFactory.getLogger(ConfigService.class);

    @Inject
    private Config config;

    @Inject
    private PlatformService platformService;

    private Provider<AppModel> appModel = () -> DI.get(AppModel.class);


    private boolean cliMode;


    public File getJarPath() {
        try {
            return new File(ConfigService.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    public File getJavaBinaryPath() {
        String path = System.getProperty("java.home", null);
        if (path == null) {
            throw new RuntimeException("java.home is not set");
        }
        File javaBin;
        switch (platformService.getOS()) {
            case MAC:
            case LINUX:
                javaBin = new File(path, "bin/java");
                if (!javaBin.exists()) {
                    throw new RuntimeException("Cannot find java at " + javaBin);
                }
                break;
            case WINDOWS:
                javaBin = new File(path, "bin/java.exe");
                if (!javaBin.exists()) {
                    throw new RuntimeException("Cannot find java at " + javaBin);
                }
                break;
            default:
                throw new RuntimeException("Unsupported OS "+platformService.getOS());

        }
        return javaBin;

    }

}
