package com.erp.service;

/**
 * Interface for UI authentication — queries app_users table from erp_ui_auth database.
 *
 * Schema:
 * CREATE TABLE app_users (
 *   user_id VARCHAR(40) PRIMARY KEY,
 *   username VARCHAR(100) NOT NULL UNIQUE,
 *   password_hash VARCHAR(255) NOT NULL,
 *   display_name VARCHAR(150) NOT NULL,
 *   role VARCHAR(30) NOT NULL,
 *   is_active TINYINT(1) NOT NULL DEFAULT 1,
 *   created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
 * )
 *
 * Implementations should:
 * 1. Use the erp-subsystem-sdk JAR to connect to RDS
 * 2. Query the erp_ui_auth.app_users table
 * 3. Hash password comparison (passwords are stored as password_hash)
 * 4. Audit login attempts to login_audit table
 * 5. Return authenticated user on success, null on failure
 */
public interface UIAuthenticator {

    /**
     * Authenticate a user by username and password.
     *
     * @param username  the login username
     * @param password  the plaintext password (will be hashed for comparison)
     * @param requestedRole  the role selected on login screen (may be null)
     * @return AuthResult with user details, or null if authentication fails
     */
    AuthResult authenticate(String username, String password, String requestedRole);

    /**
     * Result of a login attempt.
     */
    class AuthResult {
        public final String userId;        // e.g., USR-001
        public final String username;      // e.g., admin
        public final String displayName;   // e.g., Administrator
        public final String role;          // e.g., ADMIN
        public final boolean active;       // is_active flag

        public AuthResult(String userId, String username, String displayName, String role, boolean active) {
            this.userId = userId;
            this.username = username;
            this.displayName = displayName;
            this.role = role;
            this.active = active;
        }
    }
}
