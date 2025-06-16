package com.empverify.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "fabric.network")
@Validated
public record FabricNetworkConfig(
        @NotBlank String name,
        @NotBlank String contractName,
        @NotBlank String channelName,
        @NotBlank String basePath,
        @NotBlank String mspId,
        @NotBlank String userName,
        @NotBlank String connectionProfile,
        @NotBlank String walletPath,
        @NotBlank String certPath,
        @NotBlank String privateKeyPath
) {

    public String getFullConnectionProfilePath() {
        return basePath + "/" + connectionProfile;
    }

    public String getFullWalletPath() {
        return basePath + "/" + walletPath;
    }

    public String getFullCertPath() {
        return basePath + "/" + certPath;
    }

    public String getFullPrivateKeyPath() {
        return basePath + "/" + privateKeyPath;
    }
}