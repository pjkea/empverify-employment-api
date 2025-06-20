package com.empverify.controller;

import com.empverify.dto.BlockchainResponse;
import com.empverify.service.FabricGatewayService;
import com.empverify.service.IdentityManagerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/system/rbac")
@Tag(name = "Role-Based Access Control", description = "Manage and test role-based access control")
@SecurityRequirement(name = "apiKey")
public class RoleBasedAccessController {

    private static final Logger logger = LoggerFactory.getLogger(RoleBasedAccessController.class);

    private final FabricGatewayService fabricGatewayService;
    private final IdentityManagerService identityManager;

    @Autowired
    public RoleBasedAccessController(FabricGatewayService fabricGatewayService,
                                     IdentityManagerService identityManager) {
        this.fabricGatewayService = fabricGatewayService;
        this.identityManager = identityManager;
    }

    @GetMapping("/current-user")
    @Operation(summary = "Get Current User Info",
            description = "Get information about the current user based on API key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User information retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid API key"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<Map<String, Object>>> getCurrentUser() {
        try {
            logger.info("Getting current user information");

            Map<String, Object> userInfo = Map.of(
                    "username", fabricGatewayService.getCurrentUsername(),
                    "organization", fabricGatewayService.getCurrentUserOrganization(),
                    "role", fabricGatewayService.getCurrentUserRole(),
                    "access_level", determineAccessLevel(
                            fabricGatewayService.getCurrentUserOrganization(),
                            fabricGatewayService.getCurrentUserRole()
                    )
            );

            BlockchainResponse<Map<String, Object>> response =
                    BlockchainResponse.success("Current user information retrieved", userInfo);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting current user information", e);
            BlockchainResponse<Map<String, Object>> errorResponse =
                    BlockchainResponse.error("Failed to get user information: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/access-test")
    @Operation(summary = "Test Access Levels",
            description = "Test what data the current user can access")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access test completed"),
            @ApiResponse(responseCode = "401", description = "Invalid API key"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<Map<String, Object>>> testAccess() {
        try {
            logger.info("Testing access levels for current user");

            String org = fabricGatewayService.getCurrentUserOrganization();
            String role = fabricGatewayService.getCurrentUserRole();
            String username = fabricGatewayService.getCurrentUsername();

            Map<String, Object> accessTest = Map.of(
                    "user", username,
                    "organization", org,
                    "role", role,
                    "can_create_records", canCreateRecords(org, role),
                    "can_read_basic", canReadBasic(org, role),
                    "can_read_sensitive", canReadSensitive(org, role),
                    "can_read_restricted", canReadRestricted(org, role),
                    "can_add_documents", canAddDocuments(org, role),
                    "access_summary", getAccessSummary(org, role)
            );

            BlockchainResponse<Map<String, Object>> response =
                    BlockchainResponse.success("Access test completed", accessTest);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error testing access levels", e);
            BlockchainResponse<Map<String, Object>> errorResponse =
                    BlockchainResponse.error("Failed to test access: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/connection-status")
    @Operation(summary = "Get Connection Status",
            description = "Get status of blockchain connections for all identities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Connection status retrieved"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<Map<String, Object>>> getConnectionStatus() {
        try {
            logger.info("Getting connection status");

            Map<String, Object> status = fabricGatewayService.getConnectionStatistics();
            status.put("current_user", fabricGatewayService.getCurrentUsername());
            status.put("is_connected", fabricGatewayService.isConnected());

            BlockchainResponse<Map<String, Object>> response =
                    BlockchainResponse.success("Connection status retrieved", status);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting connection status", e);
            BlockchainResponse<Map<String, Object>> errorResponse =
                    BlockchainResponse.error("Failed to get connection status: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/refresh-connection")
    @Operation(summary = "Refresh Connection",
            description = "Refresh blockchain connection for current API key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Connection refreshed successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<String>> refreshConnection(
            @RequestHeader("X-API-Key") String apiKey) {
        try {
            logger.info("Refreshing connection for current user");

            fabricGatewayService.refreshConnection(apiKey);

            BlockchainResponse<String> response =
                    BlockchainResponse.success("Connection refreshed successfully", "Connection reset");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error refreshing connection", e);
            BlockchainResponse<String> errorResponse =
                    BlockchainResponse.error("Failed to refresh connection: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/api-keys")
    @Operation(summary = "List Configured API Keys",
            description = "Get list of all configured API keys (masked for security)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "API keys listed successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<Map<String, String>>> listApiKeys() {
        try {
            logger.info("Listing configured API keys");

            Map<String, String> apiKeys = identityManager.getIdentityCacheStatus();

            BlockchainResponse<Map<String, String>> response =
                    BlockchainResponse.success("Configured API keys retrieved", apiKeys);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error listing API keys", e);
            BlockchainResponse<Map<String, String>> errorResponse =
                    BlockchainResponse.error("Failed to list API keys: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // Helper methods for access control logic
    private String determineAccessLevel(String org, String role) {
        if ("Org1MSP".equals(org) && "admin".equals(role)) {
            return "FULL_ACCESS";
        } else if ("Org2MSP".equals(org) && "admin".equals(role)) {
            return "ADMIN_ACCESS";
        } else if ("Org1MSP".equals(org)) {
            return "ORG1_MEMBER";
        } else if ("Org2MSP".equals(org)) {
            return "ORG2_MEMBER";
        }
        return "NO_ACCESS";
    }

    private boolean canCreateRecords(String org, String role) {
        return "Org1MSP".equals(org); // Only Org1 can create records
    }

    private boolean canReadBasic(String org, String role) {
        return "Org1MSP".equals(org) || "Org2MSP".equals(org); // Both orgs can read basic
    }

    private boolean canReadSensitive(String org, String role) {
        return "Org1MSP".equals(org) || ("Org2MSP".equals(org) && "admin".equals(role));
    }

    private boolean canReadRestricted(String org, String role) {
        return "Org1MSP".equals(org) && "admin".equals(role); // Only Org1 admins
    }

    private boolean canAddDocuments(String org, String role) {
        return "Org1MSP".equals(org); // Only Org1 can add documents
    }

    private String getAccessSummary(String org, String role) {
        if ("Org1MSP".equals(org) && "admin".equals(role)) {
            return "Full access - can create, read all data, add documents";
        } else if ("Org2MSP".equals(org) && "admin".equals(role)) {
            return "Admin access - can read basic + sensitive data";
        } else if ("Org1MSP".equals(org)) {
            return "Member access - can create records, read basic + sensitive";
        } else if ("Org2MSP".equals(org)) {
            return "Query access - can read basic data only";
        }
        return "No access";
    }
}