package com.cloudbeds.oauthsandbox.app.config;
import com.cloudbeds.oauthsandbox.app.di.Singleton;

import java.io.File;

@Singleton
@org.aeonbits.owner.Config.LoadPolicy(org.aeonbits.owner.Config.LoadType.MERGE)
@org.aeonbits.owner.Config.Sources({
        "system:properties",
        "system:env",
        "classpath:app.properties"
})
public interface Config extends org.aeonbits.owner.Config {

    @Key("pna.app-log-file")
    @DefaultValue("${java.io.tmpdir}/oauth-sandbox.log")
    File appLogFile();

    @Key("window.width")
    @DefaultValue("1200")
    Integer preferredWindowWidth();

    @Key("window.height")
    @DefaultValue("800")
    Integer getPreferredWindowHeight();

}
