package com.empverify.service;

import com.empverify.config.FabricNetworkConfig;
import com.empverify.config.IdentityMappingConfig;
import org.hyperledger.fabric.gateway.Identity;
import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IdentityManagerService {

    private static final Logger logger = LoggerFactory.getLogger(IdentityManagerService.class);

    private final FabricNetworkConfig fabricConfig;
    private final IdentityMappingConfig identityConfig;
    private final ConcurrentHashMap<String, Identity> identityCache = new ConcurrentHashMap<>();
    private Wallet wallet;

    @Autowired
    public IdentityManagerService(FabricNetworkConfig fabricConfig, IdentityMappingConfig identityConfig) {
        this.fabricConfig = fabricConfig;
        this.identityConfig = identityConfig;
    }

    @PostConstruct
    public void initializeIdentities() {
        try {
            logger.info("Initializing identity manager with wallet at: {}", fabricConfig.getFullWalletPath());

            // Create wallet
            Path walletPath = Paths.get(fabricConfig.getFullWalletPath());
            this.wallet = Wallets.newFileSystemWallet(walletPath);

            // Load all configured identities
            for (var entry : identityConfig.apiKeyMapping().entrySet()) {
                String apiKey = entry.getKey();
                var userIdentity = entry.getValue();

                try {
                    Identity identity = loadOrCreateIdentity(userIdentity);
                    identityCache.put(apiKey, identity);
                    logger.info("Loaded identity for API key: {} -> User: {} ({})",
                            maskApiKey(apiKey), userIdentity.userName(), userIdentity.role());
                } catch (Exception e) {
                    logger.error("Failed to load identity for API key: {}, User: {}",
                            maskApiKey(apiKey), userIdentity.userName(), e);
                }
            }

            logger.info("Identity manager initialized with {} identities", identityCache.size());

        } catch (Exception e) {
            logger.error("Failed to initialize identity manager", e);
            throw new RuntimeException("Identity manager initialization failed", e);
        }
    }

    /**
     * Get blockchain identity for API key
     */
    public Identity getIdentityForApiKey(String apiKey) {
        if (apiKey == null) {
            throw new IllegalArgumentException("API key cannot be null");
        }

        Identity identity = identityCache.get(apiKey);
        if (identity == null) {
            logger.warn("No identity found for API key: {}", maskApiKey(apiKey));
            throw new IllegalArgumentException("Invalid API key: " + maskApiKey(apiKey));
        }

        return identity;
    }

    /**
     * Get user information for API key
     */
    public IdentityMappingConfig.UserIdentity getUserInfoForApiKey(String apiKey) {
        return identityConfig.getIdentityForApiKey(apiKey);
    }

    /**
     * Check if API key is valid
     */
    public boolean isValidApiKey(String apiKey) {
        return identityCache.containsKey(apiKey);
    }

    /**
     * Get the username associated with an API key
     */
    public String getUsernameForApiKey(String apiKey) {
        var userIdentity = identityConfig.getIdentityForApiKey(apiKey);
        return userIdentity != null ? userIdentity.userName() : null;
    }

    /**
     * Load or create identity from configuration
     */
    private Identity loadOrCreateIdentity(IdentityMappingConfig.UserIdentity userIdentity) throws Exception {
        // Check if identity already exists in wallet
        if (wallet.get(userIdentity.userName()) != null) {
            logger.debug("Identity {} already exists in wallet", userIdentity.userName());
            return wallet.get(userIdentity.userName());
        }

        // Load certificate and private key
        X509Certificate certificate = loadCertificate(userIdentity.certPath());
        PrivateKey privateKey = loadPrivateKey(userIdentity.privateKeyPath());

        // Create identity
        Identity identity = Identities.newX509Identity(userIdentity.mspId(), certificate, privateKey);
        wallet.put(userIdentity.userName(), identity);

        logger.info("Created and stored identity {} in wallet", userIdentity.userName());
        return identity;
    }

    /**
     * Load X.509 certificate from file
     */
    private X509Certificate loadCertificate(String certPath) throws Exception {
        Path fullPath = Paths.get(fabricConfig.basePath(), certPath);

        if (!Files.exists(fullPath)) {
            throw new RuntimeException("Certificate not found at: " + fullPath);
        }

        byte[] certPEM = Files.readAllBytes(fullPath);
        return Identities.readX509Certificate(new String(certPEM));
    }

    /**
     * Load private key from file
     */
    private PrivateKey loadPrivateKey(String privateKeyPath) throws Exception {
        Path fullPath = Paths.get(fabricConfig.basePath(), privateKeyPath);

        // Handle directory with multiple key files
        if (Files.isDirectory(fullPath)) {
            Path finalFullPath = fullPath;
            fullPath = Files.list(fullPath)
                    .filter(path -> path.toString().endsWith("_sk"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No private key file found in directory: " + finalFullPath));
        }

        if (!Files.exists(fullPath)) {
            throw new RuntimeException("Private key not found at: " + fullPath);
        }

        byte[] privateKeyPEM = Files.readAllBytes(fullPath);
        return Identities.readPrivateKey(new String(privateKeyPEM));
    }

    /**
     * Mask API key for logging (show only last 4 characters)
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 4) {
            return "****";
        }
        return "****" + apiKey.substring(apiKey.length() - 4);
    }

    /**
     * Get all configured API keys (for debugging)
     */
    public java.util.Set<String> getAllConfiguredApiKeys() {
        return identityConfig.apiKeyMapping().keySet();
    }

    /**
     * Get identity cache status
     */
    public java.util.Map<String, String> getIdentityCacheStatus() {
        java.util.Map<String, String> status = new java.util.HashMap<>();

        for (var entry : identityConfig.apiKeyMapping().entrySet()) {
            String apiKey = entry.getKey();
            String userName = entry.getValue().userName();
            boolean cached = identityCache.containsKey(apiKey);

            status.put(maskApiKey(apiKey), userName + " (" + (cached ? "LOADED" : "NOT_LOADED") + ")");
        }

        return status;
    }
}