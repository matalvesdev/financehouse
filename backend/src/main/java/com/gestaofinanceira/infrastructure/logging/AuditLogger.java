package com.gestaofinanceira.infrastructure.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Audit logger for tracking financial operations and security events.
 * All audit logs are structured and include contextual information.
 */
@Component
public class AuditLogger {
    
    private static final Logger auditLog = LoggerFactory.getLogger("com.gestaofinanceira.audit");
    private static final Logger securityLog = LoggerFactory.getLogger("com.gestaofinanceira.security");
    
    /**
     * Log a financial operation for audit trail
     */
    public void logFinancialOperation(
            String operation,
            String entityType,
            String entityId,
            String userId,
            Map<String, Object> details
    ) {
        try {
            MDC.put("operation", operation);
            MDC.put("entityType", entityType);
            MDC.put("entityId", entityId);
            MDC.put("userId", userId);
            MDC.put("timestamp", LocalDateTime.now().toString());
            
            Map<String, Object> auditData = new HashMap<>();
            auditData.put("operation", operation);
            auditData.put("entityType", entityType);
            auditData.put("entityId", entityId);
            auditData.put("userId", userId);
            auditData.put("timestamp", LocalDateTime.now());
            auditData.putAll(details);
            
            auditLog.info("Financial operation: {}", auditData);
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Log transaction creation
     */
    public void logTransactionCreated(String transactionId, String userId, String amount, String category) {
        Map<String, Object> details = new HashMap<>();
        details.put("amount", amount);
        details.put("category", category);
        
        logFinancialOperation("CREATE_TRANSACTION", "TRANSACTION", transactionId, userId, details);
    }
    
    /**
     * Log transaction update
     */
    public void logTransactionUpdated(String transactionId, String userId, Map<String, Object> changes) {
        logFinancialOperation("UPDATE_TRANSACTION", "TRANSACTION", transactionId, userId, changes);
    }
    
    /**
     * Log transaction deletion
     */
    public void logTransactionDeleted(String transactionId, String userId) {
        logFinancialOperation("DELETE_TRANSACTION", "TRANSACTION", transactionId, userId, Map.of());
    }
    
    /**
     * Log budget creation
     */
    public void logBudgetCreated(String budgetId, String userId, String category, String limit) {
        Map<String, Object> details = new HashMap<>();
        details.put("category", category);
        details.put("limit", limit);
        
        logFinancialOperation("CREATE_BUDGET", "BUDGET", budgetId, userId, details);
    }
    
    /**
     * Log goal creation
     */
    public void logGoalCreated(String goalId, String userId, String name, String targetAmount) {
        Map<String, Object> details = new HashMap<>();
        details.put("name", name);
        details.put("targetAmount", targetAmount);
        
        logFinancialOperation("CREATE_GOAL", "GOAL", goalId, userId, details);
    }
    
    /**
     * Log data import
     */
    public void logDataImport(String userId, String fileName, int recordCount, boolean success) {
        Map<String, Object> details = new HashMap<>();
        details.put("fileName", fileName);
        details.put("recordCount", recordCount);
        details.put("success", success);
        
        logFinancialOperation("IMPORT_DATA", "IMPORT", fileName, userId, details);
    }
    
    /**
     * Log authentication event
     */
    public void logAuthentication(String userId, String email, boolean success, String ipAddress) {
        try {
            MDC.put("event", "AUTHENTICATION");
            MDC.put("userId", userId);
            MDC.put("email", email);
            MDC.put("success", String.valueOf(success));
            MDC.put("ipAddress", ipAddress);
            MDC.put("timestamp", LocalDateTime.now().toString());
            
            if (success) {
                securityLog.info("User authenticated successfully: userId={}, email={}, ip={}", 
                    userId, email, ipAddress);
            } else {
                securityLog.warn("Authentication failed: email={}, ip={}", email, ipAddress);
            }
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Log authorization failure
     */
    public void logAuthorizationFailure(String userId, String resource, String action, String reason) {
        try {
            MDC.put("event", "AUTHORIZATION_FAILURE");
            MDC.put("userId", userId);
            MDC.put("resource", resource);
            MDC.put("action", action);
            MDC.put("timestamp", LocalDateTime.now().toString());
            
            securityLog.warn("Authorization failed: userId={}, resource={}, action={}, reason={}", 
                userId, resource, action, reason);
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Log security event
     */
    public void logSecurityEvent(String eventType, String userId, String description, Map<String, Object> details) {
        try {
            MDC.put("event", eventType);
            MDC.put("userId", userId);
            MDC.put("timestamp", LocalDateTime.now().toString());
            
            details.forEach((key, value) -> MDC.put(key, String.valueOf(value)));
            
            securityLog.warn("Security event: type={}, userId={}, description={}, details={}", 
                eventType, userId, description, details);
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Log suspicious activity
     */
    public void logSuspiciousActivity(String userId, String activityType, String ipAddress, String details) {
        Map<String, Object> eventDetails = new HashMap<>();
        eventDetails.put("activityType", activityType);
        eventDetails.put("ipAddress", ipAddress);
        eventDetails.put("details", details);
        
        logSecurityEvent("SUSPICIOUS_ACTIVITY", userId, activityType, eventDetails);
    }
    
    /**
     * Log rate limit exceeded
     */
    public void logRateLimitExceeded(String userId, String endpoint, String ipAddress) {
        Map<String, Object> details = new HashMap<>();
        details.put("endpoint", endpoint);
        details.put("ipAddress", ipAddress);
        
        logSecurityEvent("RATE_LIMIT_EXCEEDED", userId, "Rate limit exceeded", details);
    }
}
