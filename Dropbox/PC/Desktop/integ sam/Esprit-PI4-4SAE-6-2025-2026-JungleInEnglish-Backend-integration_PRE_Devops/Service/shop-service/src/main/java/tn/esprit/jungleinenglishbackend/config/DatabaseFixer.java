package tn.esprit.jungleinenglishbackend.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabaseFixer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseFixer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void fixDatabaseSchema() {
        log.info("Checking database schema for legacy user_id columns...");
        try {
            dropForeignKey("cart", "user_id");
            dropForeignKey("product_order", "user_id");

            log.info("Altering cart table user_id to VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE cart MODIFY COLUMN user_id VARCHAR(255);");

            log.info("Altering product_order table user_id to VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE product_order MODIFY COLUMN user_id VARCHAR(255);");

            log.info("Database schema fix completed successfully!");
        } catch (Exception e) {
            log.warn("Database schema fix skipped or encountered error (this is usually fine if already migrated): "
                    + e.getMessage());
        }
    }

    private void dropForeignKey(String tableName, String columnName) {
        String query = "SELECT CONSTRAINT_NAME " +
                "FROM information_schema.KEY_COLUMN_USAGE " +
                "WHERE TABLE_SCHEMA = DATABASE() " +
                "AND TABLE_NAME = '" + tableName + "' " +
                "AND COLUMN_NAME = '" + columnName + "' " +
                "AND REFERENCED_TABLE_NAME IS NOT NULL;";

        List<String> fkNames = jdbcTemplate.queryForList(query, String.class);
        for (String fkName : fkNames) {
            log.info("Dropping foreign key {} on table {}", fkName, tableName);
            jdbcTemplate.execute("ALTER TABLE " + tableName + " DROP FOREIGN KEY " + fkName);
        }
    }
}
