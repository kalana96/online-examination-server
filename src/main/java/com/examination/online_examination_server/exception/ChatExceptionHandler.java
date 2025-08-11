package com.examination.online_examination_server.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class ChatExceptionHandler {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatExceptionHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handle WebSocket messaging exceptions
     */
    @MessageExceptionHandler
    public void handleWebSocketException(Exception ex, Principal principal) {
        log.error("WebSocket messaging error for user {}: {}",
                principal != null ? principal.getName() : "unknown", ex.getMessage(), ex);

        if (principal != null) {
            Map<String, Object> errorMessage = new HashMap<>();
            errorMessage.put("error", "Message processing failed");
            errorMessage.put("details", ex.getMessage());
            errorMessage.put("timestamp", LocalDateTime.now());

            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    errorMessage
            );
        }
    }

    /**
     * Handle REST API chat exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Invalid chat request: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("error", "Invalid request");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied for chat operation: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("error", "Access denied");
        response.put("message", "You don't have permission to access this chat");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurityException(SecurityException ex) {
        log.warn("Security violation in chat: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("error", "Security violation");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime error in chat: {}", ex.getMessage(), ex);

        Map<String, Object> response = new HashMap<>();
        response.put("error", "Internal server error");
        response.put("message", "An unexpected error occurred");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}