package com.empverify.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@ConfigurationProperties(prefix = "empverify.identity")
@Validated
public record IdentityMappingConfig(
        Map<String, UserIdentity> apiKeyMapping
) {

    public record UserIdentity(
            String userName,
            String mspId,
            String role,
            String certPath,
            String privateKeyPath
    ) {}

    public UserIdentity getIdentityForApiKey(String apiKey) {
        return apiKeyMapping.get(apiKey);
    }

    public boolean isValidApiKey(String apiKey) {
        return apiKeyMapping.containsKey(apiKey);
    }
}