package com.erp.service;

/**
 * Mock authenticator for testing and demo.
 * Accepts any username/password and returns a test user.
 * Replace with RDSUIAuthenticator when database is ready.
 */
public class MockUIAuthenticator implements UIAuthenticator {

    @Override
    public AuthResult authenticate(String username, String password, String requestedRole) {
        // Mock: accept any non-empty credentials
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            return null;
        }

        // Return test user based on requested role
        String role = requestedRole != null ? requestedRole : "ADMIN";
        return new AuthResult(
                "USR-" + System.nanoTime() % 10000,
                username,
                username.toUpperCase() + " (Test User)",
                role,
                true
        );
    }
}
