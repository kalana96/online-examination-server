package com.examination.online_examination_server.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage JWT token blacklisting for logout functionality
 */
@Service
public class TokenBlacklistService {
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    /**
     * Add a token to the blacklist
     * @param token JWT token to blacklist
     */
    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }

    /**
     * Check if a token is blacklisted
     * @param token JWT token to check
     * @return true if token is blacklisted, false otherwise
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    /**
     * Remove expired tokens from blacklist (optional cleanup method)
     * This should be called periodically to prevent memory leaks
     */
    public void cleanupExpiredTokens() {
        // Implementation depends on your token expiration strategy
        // For now, this is a placeholder for future implementation
    }
}
