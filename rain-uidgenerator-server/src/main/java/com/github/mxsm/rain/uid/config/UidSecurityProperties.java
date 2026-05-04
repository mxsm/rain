package com.github.mxsm.rain.uid.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mxsm.uid.security")
public class UidSecurityProperties {

    private boolean enabled = false;

    private List<String> tokens = new ArrayList<>();

    private List<String> adminTokens = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens == null ? new ArrayList<>() : tokens;
    }

    public List<String> getAdminTokens() {
        return adminTokens;
    }

    public void setAdminTokens(List<String> adminTokens) {
        this.adminTokens = adminTokens == null ? new ArrayList<>() : adminTokens;
    }
}
