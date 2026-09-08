package com.harborvoice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harborvoice.identity.Actor;
import com.harborvoice.identity.SessionService;
import com.harborvoice.tenancy.TenantService;
import com.harborvoice.modules.restaurant.JdbcMenuReviewRepository;
import com.harborvoice.modules.restaurant.MenuReviewDecision;
import com.harborvoice.modules.restaurant.MenuReviewDraft;
import java.util.UUID;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ContextConfiguration(initializers = VoicePlatformApplicationTest.DatabaseGuard.class)
class VoicePlatformApplicationTest {
    public static class DatabaseGuard implements org.springframework.context.ApplicationContextInitializer<
            org.springframework.context.ConfigurableApplicationContext> {
        @Override
        public void initialize(org.springframework.context.ConfigurableApplicationContext context) {
            String url = context.getEnvironment().getProperty("spring.datasource.url", "");
            if (!url.matches("jdbc:postgresql://127[.]0[.]0[.]1:[0-9]+/voice_test")) {
                throw new IllegalStateException("Tests require a disposable loopback voice_test database before migration");
            }
        }
    }

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder encoder;
    @Autowired ObjectMapper json;
    @Autowired TenantService tenants;
    @Autowired com.harborvoice.identity.BootstrapService bootstrap;
    @Autowired SessionService sessions;
    @Autowired JdbcMenuReviewRepository menuReviews;
    private final UUID tenantA = UUID.randomUUID();
    private final UUID tenantB = UUID.randomUUID();
    private final UUID ownerA = UUID.randomUUID();
    private final UUID ownerB = UUID.randomUUID();
    private final UUID restaurantA = UUID.randomUUID();
    private final UUID restaurantB = UUID.randomUUID();
    private final UUID locationA = UUID.randomUUID();
    private final UUID locationB = UUID.randomUUID();
    private static String passwordHash;
    private static final String PASSWORD = "fictional-test-password";

    @BeforeEach
    void fixture() {
        // This suite MUST use a disposable database: never point it at a developer/customer database.
        String database = jdbc.queryForObject("SELECT current_database()", String.class);
        assertThat(database).as("Disposable test database name guard").isEqualTo("voice_test");
        jdbc.execute("TRUNCATE tenants CASCADE");
        jdbc.execute("TRUNCATE login_limits");
        if (passwordHash == null) {
            passwordHash = encoder.encode(PASSWORD);
        }
        jdbc.update("INSERT INTO tenants VALUES (?, ?), (?, ?)", tenantA, "Tenant A", tenantB, "Tenant B");
        jdbc.update("INSERT INTO businesses(id, business_type) VALUES (?, ?), (?, ?)", tenantA, "restaurant", tenantB, "restaurant");
        employee(ownerA, tenantA, "owner-a", Actor.Role.OWNER);
        employee(ownerB, tenantB, "owner-b", Actor.Role.OWNER);
        jdbc.update("INSERT INTO restaurants VALUES (?, ?, ?), (?, ?, ?)",
                restaurantA, tenantA, "Harbor A", restaurantB, tenantB, "Harbor B");
        jdbc.update("INSERT INTO locations VALUES (?, ?, ?, ?, ?), (?, ?, ?, ?, ?)",
                locationA, tenantA, restaurantA, "A", "America/Detroit",
                locationB, tenantB, restaurantB, "B", "America/Detroit");
    }

    private void employee(UUID id, UUID tenant, String username, Actor.Role role) {
        jdbc.update("INSERT INTO employees(id, tenant_id, username, password_hash, role) VALUES (?, ?, ?, ?, ?)",
                id, tenant, username, passwordHash, role.name());
    }

    private String login(String username) throws Exception {
        String response = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new LoginInput(username, PASSWORD))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("token").asText();
    }

    private record LoginInput(String username, String password) { }

    @Test
    void syntheticCompleteMenuReviewRequiresEveryDecisionAndLocksTheUnpublishedRevision() {
        Actor owner = new Actor(ownerA, tenantA, Actor.Role.OWNER);
        assertThatThrownBy(() -> menuReviews.complete(owner))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("all current draft entries");

        var writes = java.util.stream.IntStream.rangeClosed(1, MenuReviewDraft.ITEM_COUNT)
                .mapToObj(index -> new JdbcMenuReviewRepository.DecisionWrite(index,
                        MenuReviewDecision.Decision.APPROVED, null, null, 0)).toList();
        menuReviews.decideAll(owner, writes);
        var completion = menuReviews.complete(owner);

        assertThat(completion.businessId()).isEqualTo(tenantA);
        assertThat(completion.draftRevision()).isEqualTo(MenuReviewDraft.REVISION);
        assertThat(completion.publicationState()).isEqualTo("UNPUBLISHED");
        assertThat(completion.decisionSetHash()).matches("[0-9a-f]{64}");
        assertThat(menuReviews.complete(owner)).isEqualTo(completion);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_events WHERE tenant_id = ? AND action = 'MENU_REVIEW_COMPLETED'",
                Integer.class, tenantA)).isEqualTo(1);
        assertThatThrownBy(() -> menuReviews.decide(owner, 1, MenuReviewDecision.Decision.APPROVED,
                null, null, 1)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("immutable");
    }

    @Test
    void menuReviewDecisionsAreOwnerOnlyTenantScopedVersionedAndAudited() {
        Actor owner = new Actor(ownerA, tenantA, Actor.Role.OWNER);
        Actor foreignOwner = new Actor(ownerB, tenantB, Actor.Role.OWNER);
        Actor manager = new Actor(UUID.randomUUID(), tenantA, Actor.Role.MANAGER);

        assertThatThrownBy(() -> menuReviews.decide(manager, 1, MenuReviewDecision.Decision.APPROVED, null, null, 0))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> menuReviews.decide(owner, MenuReviewDraft.ITEM_COUNT + 1,
                MenuReviewDecision.Decision.APPROVED, null, null, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM menu_review_decisions WHERE business_id = ?", Integer.class, tenantA)).isZero();
        assertThatThrownBy(() -> menuReviews.decide(owner, 2, MenuReviewDecision.Decision.REJECTED, null, null, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rationale required");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM menu_review_decisions WHERE business_id = ?", Integer.class, tenantA)).isZero();

        var first = menuReviews.decide(owner, 1, MenuReviewDecision.Decision.CORRECTED, "Clarify the price.", null, 0);
        var otherTenant = menuReviews.decide(foreignOwner, 1, MenuReviewDecision.Decision.REJECTED, null, "Duplicate entry", 0);
        assertThat(first.businessId()).isEqualTo(tenantA);
        assertThat(first.version()).isEqualTo(1);
        assertThat(first.publicationState()).isEqualTo("UNPUBLISHED");
        assertThat(first.draftRevision()).isEqualTo(MenuReviewDraft.REVISION);
        assertThat(otherTenant.businessId()).isEqualTo(tenantB);
        assertThat(menuReviews.decisions(owner)).containsExactly(first);
        assertThat(menuReviews.decisions(foreignOwner)).containsExactly(otherTenant);
        assertThat(menuReviews.summary(owner, 256)).isEqualTo(new com.harborvoice.modules.restaurant.MenuReviewSummary(0, 1, 0, 255, "UNPUBLISHED"));
        assertThat(menuReviews.summary(foreignOwner, 256)).isEqualTo(new com.harborvoice.modules.restaurant.MenuReviewSummary(0, 0, 1, 255, "UNPUBLISHED"));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_events WHERE tenant_id = ? AND action = 'MENU_REVIEW_CORRECTED'", Integer.class, tenantA)).isEqualTo(1);

        assertThatThrownBy(() -> menuReviews.decide(owner, 1, MenuReviewDecision.Decision.APPROVED, null, null, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("version conflict");
        assertThat(jdbc.queryForObject("SELECT version FROM menu_review_decisions WHERE business_id = ? AND item_index = 1", Integer.class, tenantA)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_events WHERE tenant_id = ?", Integer.class, tenantA)).isEqualTo(1);

        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO menu_review_decisions(business_id, item_index, decision, correction, version, publication_state, actor_id)
                VALUES (?, 2, 'APPROVED', NULL, 1, 'PUBLISHED', ?)
                """, tenantA, ownerA)).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> menuReviews.decideAll(owner, java.util.List.of(
                new JdbcMenuReviewRepository.DecisionWrite(3, MenuReviewDecision.Decision.APPROVED, null, null, 0),
                new JdbcMenuReviewRepository.DecisionWrite(1, MenuReviewDecision.Decision.APPROVED, null, null, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("version conflict");
        assertThatThrownBy(() -> menuReviews.decideAll(owner, java.util.List.of(
                new JdbcMenuReviewRepository.DecisionWrite(1, MenuReviewDecision.Decision.APPROVED, null, null, 1))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("previously undecided");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM menu_review_decisions WHERE business_id = ? AND item_index = 3", Integer.class, tenantA)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_events WHERE tenant_id = ?", Integer.class, tenantA)).isEqualTo(1);

        jdbc.update("UPDATE menu_review_decisions SET draft_revision = ? WHERE business_id = ? AND item_index = 1",
                "a".repeat(64), tenantA);
        assertThat(menuReviews.decisions(owner)).isEmpty();
        assertThatThrownBy(() -> menuReviews.decide(owner, 1, MenuReviewDecision.Decision.APPROVED, null, null, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("version conflict");
    }

    @Test
    void noAnonymousOrCallerSuppliedIdentity() throws Exception {
        mvc.perform(get("/api/v1/restaurants").header("X-Tenant-ID", tenantA)
                .header("X-Employee-ID", ownerA)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/me").header("Authorization", "Bearer guessed"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ownerSeesOnlyOwnTenantAndForeignLocationIsNotFound() throws Exception {
        String token = login("owner-a");
        String body = mvc.perform(get("/api/v1/restaurants").header("Authorization", "Bearer " + token)
                .header("X-Tenant-ID", tenantB)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(body).contains("Harbor A").doesNotContain("Harbor B");
        mvc.perform(get("/api/v1/locations/" + locationB).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/locations/" + locationA).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @EnumSource(value = Actor.Role.class, names = {"MANAGER", "EMPLOYEE"})
    void staffNeedLocationAssignmentAndCannotCreateRestaurant(Actor.Role role) throws Exception {
        UUID id = UUID.randomUUID();
        employee(id, tenantA, "staff", role);
        String token = login("staff");
        mvc.perform(get("/api/v1/locations/" + locationA).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
        jdbc.update("INSERT INTO employee_locations VALUES (?, ?, ?)", tenantA, id, locationA);
        mvc.perform(get("/api/v1/locations/" + locationA).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/locations/" + locationB).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/restaurants").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Unauthorized\"}"))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @EnumSource(value = Actor.Role.class, names = {"SUPPORT", "SYSTEM"})
    void supportAndSystemHaveNoInteractiveLoginOrDefaultServiceAccess(Actor.Role role) throws Exception {
        UUID id = UUID.randomUUID();
        employee(id, tenantA, "restricted", role);
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new LoginInput("restricted", PASSWORD))))
                .andExpect(status().isUnauthorized());
        assertThatThrownBy(() -> tenants.restaurants(new Actor(id, tenantA, role)))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
    }

    @Test
    void createUsesAuthenticatedTenantAndWritesCorrelatedAudit() throws Exception {
        String token = login("owner-a");
        var response = mvc.perform(post("/api/v1/restaurants").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"New Fictional Restaurant\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse();
        UUID id = UUID.fromString(json.readTree(response.getContentAsString()).get("id").asText());
        assertThat(jdbc.queryForObject("SELECT tenant_id FROM restaurants WHERE id = ?", UUID.class, id))
                .isEqualTo(tenantA);
        assertThat(jdbc.queryForObject("SELECT correlation_id FROM audit_events WHERE target_id = ? AND action = 'RESTAURANT_CREATED'",
                UUID.class, id)).isEqualTo(UUID.fromString(response.getHeader("X-Correlation-ID")));
        mvc.perform(post("/api/v1/restaurants").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Spoof\",\"tenantId\":\"" + tenantB + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void emptyNamesAndSqlInjectionDoNotBypassValidation() throws Exception {
        String token = login("owner-a");
        mvc.perform(post("/api/v1/restaurants").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"  \"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new LoginInput("' OR 1=1 --", PASSWORD))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRevokesAndDatabaseStoresOnlyTokenHash() throws Exception {
        String token = login("owner-a");
        assertThat(jdbc.queryForObject("SELECT token_hash FROM auth_sessions WHERE employee_id = ?", String.class, ownerA))
                .hasSize(64).isNotEqualTo(token);
        mvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredDisabledAndMfaRequiredSessionsFailClosed() throws Exception {
        String expired = login("owner-a");
        jdbc.update("UPDATE auth_sessions SET expires_at = CURRENT_TIMESTAMP - INTERVAL '1 second'");
        mvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
        String active = login("owner-a");
        jdbc.update("UPDATE employees SET enabled = FALSE WHERE id = ?", ownerA);
        mvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + active))
                .andExpect(status().isUnauthorized());
        jdbc.update("UPDATE employees SET enabled = TRUE, mfa_required = TRUE WHERE id = ?", ownerA);
        mvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + active))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new LoginInput("owner-a", PASSWORD))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cleanupRemovesExpiredSessionsButKeepsActiveSessions() throws Exception {
        String expired = login("owner-a");
        jdbc.update("UPDATE auth_sessions SET expires_at = CURRENT_TIMESTAMP - INTERVAL '1 second'");
        String active = login("owner-a");

        sessions.removeExpiredSessions();

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM auth_sessions WHERE expires_at <= CURRENT_TIMESTAMP",
                Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM auth_sessions WHERE expires_at > CURRENT_TIMESTAMP",
                Integer.class)).isEqualTo(1);
    }

    @Test
    void roleChangesApplyToExistingSessionImmediately() throws Exception {
        String token = login("owner-a");
        jdbc.update("UPDATE employees SET role = 'EMPLOYEE' WHERE id = ?", ownerA);
        mvc.perform(post("/api/v1/restaurants").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Denied\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void databaseRejectsCrossTenantRelationshipsAndAuditMutation() throws Exception {
        assertThatThrownBy(() -> jdbc.update("INSERT INTO employee_locations VALUES (?, ?, ?)",
                tenantA, ownerA, locationB)).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> jdbc.update("INSERT INTO locations VALUES (?, ?, ?, ?, ?)",
                UUID.randomUUID(), tenantA, restaurantB, "Wrong", "America/Detroit"))
                .isInstanceOf(DataAccessException.class);
        login("owner-a");
        assertThatThrownBy(() -> jdbc.update("UPDATE audit_events SET outcome = 'FORGED'"))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> jdbc.update("DELETE FROM audit_events"))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void concurrentRequestsDoNotMixTenantContexts() throws Exception {
        String a = login("owner-a");
        String b = login("owner-b");
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var jobs = java.util.stream.IntStream.range(0, 40).mapToObj(i ->
                    executor.submit(() -> {
                        String token = i % 2 == 0 ? a : b;
                        String expected = i % 2 == 0 ? tenantA.toString() : tenantB.toString();
                        String body = mvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
                        assertThat(json.readTree(body).get("tenantId").asText()).isEqualTo(expected);
                        return true;
                    })).toList();
            for (var job : jobs) {
                assertThat(job.get()).isTrue();
            }
        }
        mvc.perform(get("/api/v1/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void noCookieSessionOrCrossOriginAuthentication() throws Exception {
        String token = login("owner-a");
        var response = mvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + token)
                .header("Origin", "https://untrusted.example")).andExpect(status().isOk())
                .andReturn().getResponse();
        assertThat(response.getHeader("Set-Cookie")).isNull();
        assertThat(response.getHeader("Access-Control-Allow-Origin")).isNull();
        assertThat(response.getHeader("Cache-Control")).contains("no-store");
    }
    @Test
    void repeatedLoginAttemptsAreLimitedEvenWithForgedForwardedHeaders() throws Exception {
        for (int i = 0; i < 10; i++) {
            mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .header("X-Forwarded-For", "spoof-" + i)
                    .content(json.writeValueAsString(new LoginInput("missing", "wrong"))))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new LoginInput("missing", "wrong"))))
                .andExpect(status().isTooManyRequests());
        jdbc.update("UPDATE login_limits SET resets_at = CURRENT_TIMESTAMP - INTERVAL '1 second'");
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new LoginInput("missing", "wrong"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void businessMutationRollsBackWhenAuditCannotPersist() throws Exception {
        String token = login("owner-a");
        jdbc.execute("""
                CREATE FUNCTION test_reject_audit_insert() RETURNS TRIGGER LANGUAGE plpgsql AS $$
                BEGIN RAISE EXCEPTION 'simulated audit outage'; END;
                $$
                """);
        jdbc.execute("CREATE TRIGGER test_reject_audit BEFORE INSERT ON audit_events "
                + "FOR EACH ROW EXECUTE FUNCTION test_reject_audit_insert()");
        try {
            mvc.perform(post("/api/v1/restaurants").header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Must Roll Back\"}"))
                    .andExpect(status().isServiceUnavailable());
            assertThat(jdbc.queryForObject("SELECT count(*) FROM restaurants WHERE name = 'Must Roll Back'",
                    Integer.class)).isZero();
        } finally {
            jdbc.execute("DROP TRIGGER test_reject_audit ON audit_events");
            jdbc.execute("DROP FUNCTION test_reject_audit_insert()");
        }
    }

    @Test
    void invalidPasswordPayloadIsNotEchoedInResponse() throws Exception {
        String secret = "x".repeat(73);
        var response = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new LoginInput("owner-a", secret))))
                .andExpect(status().isBadRequest()).andReturn().getResponse();
        assertThat(response.getContentAsString()).doesNotContain(secret);
    }

    private String body(Object value) throws Exception {
        return json.writeValueAsString(value);
    }

    @Test
    void ownerCreatesStaffWithoutLeakingPasswords() throws Exception {
        String token = login("owner-a");
        String response = mvc.perform(post("/api/v1/employees").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body(java.util.Map.of(
                        "username", "new-staff", "password", PASSWORD, "role", "EMPLOYEE"))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        assertThat(response).doesNotContain("password", PASSWORD, "hash", "tenantId");
        String hash = jdbc.queryForObject("SELECT password_hash FROM employees WHERE username = 'new-staff'", String.class);
        assertThat(hash).isNotEqualTo(PASSWORD);
        assertThat(encoder.matches(PASSWORD, hash)).isTrue();
        String staffToken = login("new-staff");
        mvc.perform(get("/api/v1/locations/" + locationA).header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isNotFound());
        String list = mvc.perform(get("/api/v1/employees").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(list).contains("new-staff").doesNotContain("owner-b", "password", "hash");
    }

    @ParameterizedTest
    @EnumSource(value = Actor.Role.class, names = {"OWNER", "SYSTEM", "SUPPORT"})
    void ownerCannotMintPrivilegedRoles(Actor.Role role) throws Exception {
        String token = login("owner-a");
        mvc.perform(post("/api/v1/employees").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body(java.util.Map.of(
                        "username", "privileged", "password", PASSWORD, "role", role.name()))))
                .andExpect(status().isForbidden());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM employees WHERE username = 'privileged'", Integer.class))
                .isZero();
    }

    @Test
    void locationCreationChecksTenantAndTimezone() throws Exception {
        String token = login("owner-a");
        mvc.perform(post("/api/v1/locations").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body(java.util.Map.of(
                        "restaurantId", restaurantA, "name", "Second Location", "timezone", "America/Detroit"))))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/locations").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body(java.util.Map.of(
                        "restaurantId", restaurantB, "name", "Foreign", "timezone", "America/Detroit"))))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/locations").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body(java.util.Map.of(
                        "restaurantId", restaurantA, "name", "Invalid", "timezone", "Mars/Unknown"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void assignmentsRejectForeignLocationsWithoutLosingExistingAccess() throws Exception {
        UUID staff = UUID.randomUUID();
        employee(staff, tenantA, "staff", Actor.Role.EMPLOYEE);
        String owner = login("owner-a");
        String token = login("staff");
        mvc.perform(put("/api/v1/employees/" + staff + "/locations").header("Authorization", "Bearer " + owner)
                .contentType(MediaType.APPLICATION_JSON).content(body(java.util.Map.of("locationIds", java.util.List.of(locationA)))))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/locations/" + locationA).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(put("/api/v1/employees/" + staff + "/locations").header("Authorization", "Bearer " + owner)
                .contentType(MediaType.APPLICATION_JSON).content(body(java.util.Map.of("locationIds", java.util.List.of(locationB)))))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/locations/" + locationA).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(put("/api/v1/employees/" + staff + "/locations").header("Authorization", "Bearer " + owner)
                .contentType(MediaType.APPLICATION_JSON).content(body(java.util.Map.of("locationIds", java.util.List.of()))))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/locations/" + locationA).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void accessChangesRevokeSessionsAndCannotChangeOwnersOrOtherTenants() throws Exception {
        UUID staff = UUID.randomUUID();
        employee(staff, tenantA, "staff", Actor.Role.EMPLOYEE);
        String owner = login("owner-a");
        String old = login("staff");
        mvc.perform(put("/api/v1/employees/" + staff + "/access").header("Authorization", "Bearer " + owner)
                .contentType(MediaType.APPLICATION_JSON).content(body(java.util.Map.of("role", "MANAGER", "enabled", true))))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + old)).andExpect(status().isUnauthorized());
        String fresh = login("staff");
        mvc.perform(post("/api/v1/employees/" + staff + "/revoke-sessions").header("Authorization", "Bearer " + owner))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + fresh)).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/v1/employees/" + ownerA + "/access").header("Authorization", "Bearer " + owner)
                .contentType(MediaType.APPLICATION_JSON).content(body(java.util.Map.of("role", "EMPLOYEE", "enabled", false))))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/employees/" + ownerB + "/access").header("Authorization", "Bearer " + owner)
                .contentType(MediaType.APPLICATION_JSON).content(body(java.util.Map.of("role", "EMPLOYEE", "enabled", false))))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @EnumSource(value = Actor.Role.class, names = {"MANAGER", "EMPLOYEE"})
    void nonOwnersCannotManageStaffLocationsOrAudit(Actor.Role role) throws Exception {
        UUID staff = UUID.randomUUID();
        employee(staff, tenantA, "staff", role);
        String token = login("staff");
        mvc.perform(get("/api/v1/employees").header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/audit-events").header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/locations").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body(java.util.Map.of(
                        "restaurantId", restaurantA, "name", "Denied", "timezone", "America/Detroit"))))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/employees/" + ownerA + "/revoke-sessions").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void auditViewerIsTenantScopedAndBounded() throws Exception {
        String owner = login("owner-a");
        login("owner-b");
        String response = mvc.perform(get("/api/v1/audit-events?limit=1").header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(json.readTree(response).size()).isEqualTo(1);
        assertThat(response).contains(ownerA.toString()).doesNotContain(ownerB.toString());
    }

    @Test
    void bootstrapIsSingleUseAndDoesNotExposeHttpSignup() throws Exception {
        assertThatThrownBy(() -> bootstrap.initialize("Fictional", "first-owner", PASSWORD))
                .isInstanceOf(IllegalStateException.class);
        jdbc.execute("TRUNCATE tenants CASCADE");
        UUID created = bootstrap.initialize("Fictional", "first-owner", PASSWORD);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM tenants", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT tenant_id FROM employees WHERE username = 'first-owner'", UUID.class))
                .isEqualTo(created);
        String token = login("first-owner");
        mvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        assertThatThrownBy(() -> bootstrap.initialize("Second", "second-owner", PASSWORD))
                .isInstanceOf(IllegalStateException.class);
        mvc.perform(post("/api/v1/bootstrap").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

}
