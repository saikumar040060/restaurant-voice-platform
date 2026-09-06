package com.harborvoice.platform.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcBusinessProfileRepository implements BusinessProfilePort {
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;

    public JdbcBusinessProfileRepository(JdbcTemplate jdbc, ObjectMapper json) {
        this.jdbc = jdbc; this.json = json;
    }

    @Override
    public BusinessProfile approved(UUID businessId) {
        return jdbc.query("""
                SELECT version, locale, timezone, config, approval_state
                FROM business_profiles
                WHERE business_id = ? AND approval_state = 'APPROVED'
                ORDER BY version DESC LIMIT 1
                """, rs -> {
            if (!rs.next()) throw new IllegalArgumentException("no approved business profile");
            try {
                Map<String, Object> config = json.readValue(rs.getString("config"), new TypeReference<>() { });
                return new BusinessProfile(businessId, rs.getInt("version"), rs.getString("locale"),
                        rs.getString("timezone"), config, BusinessProfile.ApprovalState.APPROVED);
            } catch (Exception ex) {
                throw new IllegalStateException("invalid stored business profile", ex);
            }
        }, businessId);
    }
}
