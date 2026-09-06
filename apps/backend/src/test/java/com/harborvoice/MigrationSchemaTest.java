package com.harborvoice;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class MigrationSchemaTest {
    @Test
    void conversationCompositeForeignKeysHaveMatchingUniqueKey() throws Exception {
        String sql = Files.readString(Path.of("src/main/resources/db/migration/V4__conversation_and_action_state.sql"));
        assertTrue(sql.contains("UNIQUE (id, business_id)"));
        assertTrue(sql.contains("FOREIGN KEY (conversation_id, business_id)"));
    }
}
