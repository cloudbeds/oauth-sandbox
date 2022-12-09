package com.cloudbeds.oauthsandbox.app.service;

import com.cloudbeds.oauthsandbox.app.di.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PlatformService {

    private static final Logger log = LoggerFactory.getLogger(PlatformService.class);
    public enum OS {
        WINDOWS, LINUX, MAC, SOLARIS
    };// Operating systems.

    private OS os = null;

    public OS getOS() {
        if (os == null) {
            String operSys = System.getProperty("os.name").toLowerCase();
            log.debug("Operating system {}", operSys);
            if (operSys.contains("win")) {
                os = OS.WINDOWS;
            } else if (operSys.contains("nix") || operSys.contains("nux")
                    || operSys.contains("aix")) {
                os = OS.LINUX;
            } else if (operSys.contains("mac")) {
                os = OS.MAC;
            } else if (operSys.contains("sunos")) {
                os = OS.SOLARIS;
            }
        }
        return os;
    }



}
