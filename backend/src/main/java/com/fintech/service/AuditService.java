package com.fintech.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void logAction(String action, String userId, Map<String, Object> details) {
        try {
            Map<String, Object> auditLog = Map.of(
                "timestamp", LocalDateTime.now(),
                "action", action,
                "userId", userId,
                "details", details
            );
            
            kafkaTemplate.send("audit-logs", objectMapper.writeValueAsString(auditLog));
        } catch (Exception e) {
            // Log error but don't disrupt main flow
            e.printStackTrace();
        }
    }
} 