package com.empverify.service;

import com.empverify.config.FabricNetworkConfig;
import com.empverify.exception.BlockchainException;
import org.hyperledger.fabric.gateway.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

@Service
public class FabricGatewayService {

    private static final Logger logger = LoggerFactory.getLogger(FabricGatewayService.class);

    private final FabricNetworkConfig config;
    private Gateway gateway;
    private Network network;
    private Contract contract;

    @Autowired
    public FabricGatewayService(FabricNetworkConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void initializeGateway() {
        try {
            logger.info("Initializing Fabric Gateway with base path: {}", config.basePath());

            // Load connection profile
            Path connectionProfilePath = Paths.get(config.getFullConnectionProfilePath());
            if (!Files.exists(connectionProfilePath)) {
                throw new BlockchainException("Connection profile not found at: " + connectionProfilePath);
            }

            // Load wallet and identity
            Wallet wallet = createWallet();
            Identity identity = loadOrCreateIdentity(wallet);

            // Create gateway
            Gateway.Builder builder = Gateway.createBuilder()
                    .identity(wallet, config.userName())
                    .networkConfig(connectionProfilePath)
                    .discovery(true);

            this.gateway = builder.connect();
            this.network = gateway.getNetwork(config.channelName());
            this.contract = network.getContract(config.contractName());

            logger.info("Successfully initialized Fabric Gateway for channel: {} and contract: {}",
                    config.channelName(), config.contractName());

        } catch (Exception e) {
            logger.error("Failed to initialize Fabric Gateway", e);
            throw new BlockchainException("Failed to initialize Fabric Gateway: " + e.getMessage(), e);
        }
    }

    private Wallet createWallet() throws IOException {
        Path walletPath = Paths.get(config.getFullWalletPath());
        return Wallets.newFileSystemWallet(walletPath);
    }

    private Identity loadOrCreateIdentity(Wallet wallet) throws Exception {
        // Check if identity already exists in wallet
        if (wallet.get(config.userName()) != null) {
            logger.info("Identity {} already exists in wallet", config.userName());
            return wallet.get(config.userName());
        }

        // Load certificate and private key
        X509Certificate certificate = loadCertificate();
        PrivateKey privateKey = loadPrivateKey();

        // Create identity
        Identity identity = Identities.newX509Identity(config.mspId(), certificate, privateKey);
        wallet.put(config.userName(), identity);

        logger.info("Created and stored identity {} in wallet", config.userName());
        return identity;
    }

    private X509Certificate loadCertificate() throws CertificateException, IOException {
        Path certPath = Paths.get(config.getFullCertPath());
        if (!Files.exists(certPath)) {
            throw new BlockchainException("Certificate not found at: " + certPath);
        }

        byte[] certPEM = Files.readAllBytes(certPath);
        return Identities.readX509Certificate(new String(certPEM));
    }

    private PrivateKey loadPrivateKey() throws IOException, InvalidKeyException {
        Path privateKeyPath = Paths.get(config.getFullPrivateKeyPath());

        // Handle directory with multiple key files
        if (Files.isDirectory(privateKeyPath)) {
            try {
                final Path searchPath = privateKeyPath;
                privateKeyPath = Files.list(privateKeyPath)
                        .filter(path -> path.toString().endsWith("_sk"))
                        .findFirst()
                        .orElseThrow(() -> new BlockchainException("No private key file found in directory: " + searchPath));
            } catch (IOException e) {
                throw new BlockchainException("Error reading private key directory: " + privateKeyPath, e);
            }
        }

        if (!Files.exists(privateKeyPath)) {
            throw new BlockchainException("Private key not found at: " + privateKeyPath);
        }

        byte[] privateKeyPEM = Files.readAllBytes(privateKeyPath);
        return Identities.readPrivateKey(new String(privateKeyPEM));
    }

    public String submitTransaction(String functionName, String... args) {
        try {
            logger.debug("Submitting transaction: {} with args: {}", functionName, String.join(", ", args));

            byte[] result = contract.submitTransaction(functionName, args);
            String response = new String(result);

            logger.debug("Transaction submitted successfully: {}", functionName);
            return response;

        } catch (Exception e) {
            logger.error("Failed to submit transaction: {}", functionName, e);
            throw new BlockchainException("Failed to submit transaction: " + functionName, e);
        }
    }

    public String evaluateTransaction(String functionName, String... args) {
        try {
            logger.debug("Evaluating transaction: {} with args: {}", functionName, String.join(", ", args));

            byte[] result = contract.evaluateTransaction(functionName, args);
            String response = new String(result);

            logger.debug("Transaction evaluated successfully: {}", functionName);
            return response;

        } catch (Exception e) {
            logger.error("Failed to evaluate transaction: {}", functionName, e);
            throw new BlockchainException("Failed to evaluate transaction: " + functionName, e);
        }
    }

    public boolean isConnected() {
        return gateway != null && network != null && contract != null;
    }

    @PreDestroy
    public void cleanup() {
        if (gateway != null) {
            try {
                gateway.close();
                logger.info("Fabric Gateway connection closed");
            } catch (Exception e) {
                logger.warn("Error closing Fabric Gateway", e);
            }
        }
    }
}