package sixkiller.sample.configuration;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="appconfig")
public class ApplicationConfigurationProperties {

    private String[] defaultUserRoles;

    private String clientId;

    private String clientSecret;

    private String[] onPopStateUrls;

    public String[] getDefaultUserRoles() {
        return defaultUserRoles;
    }

    public void setDefaultUserRoles(String[] defaultUserRoles) {
        this.defaultUserRoles = defaultUserRoles;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String[] getOnPopStateUrls() {
        return onPopStateUrls;
    }

    public void setOnPopStateUrls(String[] onPopStateUrls) {
        this.onPopStateUrls = onPopStateUrls;
    }
}
