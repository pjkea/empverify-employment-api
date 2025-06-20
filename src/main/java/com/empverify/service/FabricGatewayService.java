package com.empverify.service;

import com.empverify.config.FabricNetworkConfig;
import com.empverify.exception.BlockchainException;
import org.hyperledger.fabric.gateway.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class FabricGatewayService {

    private static final Logger logger = LoggerFactory.getLogger(FabricGatewayService.class);

    private final FabricNetworkConfig config;
    private final IdentityManagerService identityManager;

    // Cache of gateways per API key to avoid recreating connections
    private final ConcurrentHashMap<String, Gateway> gatewayCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Network> networkCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Contract> contractCache = new ConcurrentHashMap<>();

    @Autowired
    public FabricGatewayService(FabricNetworkConfig config, IdentityManagerService identityManager) {
        this.config = config;
        this.identityManager = identityManager;
    }

    @PostConstruct
    public void initializeService() {
        logger.info("Enhanced Fabric Gateway Service initialized");
        logger.info("Channel: {}, Contract: {}", config.channelName(), config.contractName());
    }

    /**
     * Get gateway for current API key context
     */
    private Gateway getGatewayForCurrentContext() {
        String apiKey = getCurrentApiKey();

        return gatewayCache.computeIfAbsent(apiKey, key -> {
            try {
                logger.debug("Creating new gateway connection for API key: {}", maskApiKey(key));

                Identity identity = identityManager.getIdentityForApiKey(key);
                String username = identityManager.getUsernameForApiKey(key);

                Path connectionProfilePath = Paths.get(config.getFullConnectionProfilePath());

                Gateway gateway = Gateway.createBuilder()
                        .identity(identity)
                        .networkConfig(connectionProfilePath)
                        .discovery(true)
                        .connect();

                logger.info("Created gateway connection for user: {} (API key: {})", username, maskApiKey(key));
                return gateway;

            } catch (Exception e) {
                logger.error("Failed to create gateway for API key: {}", maskApiKey(key), e);
                throw new BlockchainException("Failed to create gateway connection", e);
            }
        });
    }

    /**
     * Get network for current API key context
     */
    private Network getNetworkForCurrentContext() {
        String apiKey = getCurrentApiKey();

        return networkCache.computeIfAbsent(apiKey, key -> {
            try {
                Gateway gateway = getGatewayForCurrentContext();
                Network network = gateway.getNetwork(config.channelName());

                logger.debug("Retrieved network for channel: {} (API key: {})", config.channelName(), maskApiKey(key));
                return network;

            } catch (Exception e) {
                logger.error("Failed to get network for API key: {}", maskApiKey(key), e);
                throw new BlockchainException("Failed to get network", e);
            }
        });
    }

    /**
     * Get contract for current API key context
     */
    private Contract getContractForCurrentContext() {
        String apiKey = getCurrentApiKey();

        return contractCache.computeIfAbsent(apiKey, key -> {
            try {
                Network network = getNetworkForCurrentContext();
                Contract contract = network.getContract(config.contractName());

                logger.debug("Retrieved contract: {} (API key: {})", config.contractName(), maskApiKey(key));
                return contract;

            } catch (Exception e) {
                logger.error("Failed to get contract for API key: {}", maskApiKey(key), e);
                throw new BlockchainException("Failed to get contract", e);
            }
        });
    }

    /**
     * Submit transaction using identity based on API key
     */
    public String submitTransaction(String functionName, String... args) {
        try {
            String apiKey = getCurrentApiKey();
            String username = identityManager.getUsernameForApiKey(apiKey);

            logger.debug("Submitting transaction: {} as user: {} (API key: {})",
                    functionName, username, maskApiKey(apiKey));

            Contract contract = getContractForCurrentContext();
            byte[] result = contract.submitTransaction(functionName, args);
            String response = new String(result);

            logger.debug("Transaction submitted successfully: {} by user: {}", functionName, username);
            return response;

        } catch (Exception e) {
            logger.error("Failed to submit transaction: {}", functionName, e);
            throw new BlockchainException("Failed to submit transaction: " + functionName, e);
        }
    }

    /**
     * Evaluate transaction using identity based on API key
     */
    public String evaluateTransaction(String functionName, String... args) {
        try {
            String apiKey = getCurrentApiKey();
            String username = identityManager.getUsernameForApiKey(apiKey);

            logger.debug("Evaluating transaction: {} as user: {} (API key: {})",
                    functionName, username, maskApiKey(apiKey));

            Contract contract = getContractForCurrentContext();
            byte[] result = contract.evaluateTransaction(functionName, args);
            String response = new String(result);

            logger.debug("Transaction evaluated successfully: {} by user: {}", functionName, username);
            return response;

        } catch (Exception e) {
            logger.error("Failed to evaluate transaction: {}", functionName, e);
            throw new BlockchainException("Failed to evaluate transaction: " + functionName, e);
        }
    }

    /**
     * Get current API key from request context
     */
    private String getCurrentApiKey() {
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = requestAttributes.getRequest();
            String apiKey = request.getHeader("X-API-Key");

            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new IllegalArgumentException("No API key found in request headers");
            }

            if (!identityManager.isValidApiKey(apiKey)) {
                throw new IllegalArgumentException("Invalid API key: " + maskApiKey(apiKey));
            }

            return apiKey;

        } catch (IllegalStateException e) {
            logger.error("No request context available - cannot determine API key");
            throw new BlockchainException("No request context available", e);
        }
    }

    /**
     * Check if service is connected (checks if any gateway exists)
     */
    public boolean isConnected() {
        try {
            String apiKey = getCurrentApiKey();
            Gateway gateway = gatewayCache.get(apiKey);
            return gateway != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get current user information
     */
    public String getCurrentUsername() {
        try {
            String apiKey = getCurrentApiKey();
            return identityManager.getUsernameForApiKey(apiKey);
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * Get current user's organization
     */
    public String getCurrentUserOrganization() {
        try {
            String apiKey = getCurrentApiKey();
            var userIdentity = identityManager.getUserInfoForApiKey(apiKey);
            return userIdentity != null ? userIdentity.mspId() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * Get current user's role
     */
    public String getCurrentUserRole() {
        try {
            String apiKey = getCurrentApiKey();
            var userIdentity = identityManager.getUserInfoForApiKey(apiKey);
            return userIdentity != null ? userIdentity.role() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * Mask API key for logging
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 4) {
            return "****";
        }
        return "****" + apiKey.substring(apiKey.length() - 4);
    }

    /**
     * Get connection statistics
     */
    public java.util.Map<String, Object> getConnectionStatistics() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("active_gateways", gatewayCache.size());
        stats.put("active_networks", networkCache.size());
        stats.put("active_contracts", contractCache.size());
        stats.put("identity_cache_status", identityManager.getIdentityCacheStatus());
        return stats;
    }

    /**
     * Force refresh connection for API key
     */
    public void refreshConnection(String apiKey) {
        logger.info("Refreshing connection for API key: {}", maskApiKey(apiKey));

        // Close and remove cached connections
        Gateway gateway = gatewayCache.remove(apiKey);
        if (gateway != null) {
            try {
                gateway.close();
            } catch (Exception e) {
                logger.warn("Error closing gateway during refresh", e);
            }
        }

        networkCache.remove(apiKey);
        contractCache.remove(apiKey);

        logger.info("Connection refreshed for API key: {}", maskApiKey(apiKey));
    }

    @PreDestroy
    public void cleanup() {
        logger.info("Cleaning up Enhanced Fabric Gateway Service");

        // Close all gateway connections
        for (var entry : gatewayCache.entrySet()) {
            try {
                entry.getValue().close();
                logger.debug("Closed gateway for API key: {}", maskApiKey(entry.getKey()));
            } catch (Exception e) {
                logger.warn("Error closing gateway for API key: {}", maskApiKey(entry.getKey()), e);
            }
        }

        gatewayCache.clear();
        networkCache.clear();
        contractCache.clear();

        logger.info("Enhanced Fabric Gateway Service cleanup completed");
    }
}