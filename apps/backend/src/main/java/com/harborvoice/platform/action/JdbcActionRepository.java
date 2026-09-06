package com.harborvoice.platform.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcActionRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;

    public JdbcActionRepository(JdbcTemplate jdbc, ObjectMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    @Transactional
    public ActionRequest propose(ActionRequest request, String requestHash) {
        try {
            jdbc.update("""
                    INSERT INTO action_requests(id, business_id, conversation_id, tool_id, idempotency_key, request_hash, status, arguments)
                    VALUES (?, ?, ?, ?, ?, ?, 'PROPOSED', ?::jsonb)
                    """, request.requestId(), request.businessId(), request.conversationId(), request.toolId(),
                    request.idempotencyKey(), requestHash, json.writeValueAsString(request.arguments()));
            return request;
        } catch (DuplicateKeyException duplicate) {
            return jdbc.query("SELECT id, request_hash FROM action_requests WHERE business_id = ? AND idempotency_key = ?",
                    rs -> {
                        if (!rs.next()) throw duplicate;
                        if (!requestHash.equals(rs.getString("request_hash"))) {
                            throw new IllegalArgumentException("idempotency key reused for different request");
                        }
                        return request;
                    }, request.businessId(), request.idempotencyKey());
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("action arguments cannot be serialized", ex);
        }
    }
}
