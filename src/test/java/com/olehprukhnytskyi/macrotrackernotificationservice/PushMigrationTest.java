package com.olehprukhnytskyi.macrotrackernotificationservice;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.UUID;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PushMigrationTest {
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void migratesAndAcceptsOriginalChecksum(boolean existingLocks) throws Exception {
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        try (Connection connection = DriverManager.getConnection(url, "sa", "")) {
            if (existingLocks) {
                try (var statement = connection.createStatement()) {
                    statement.execute("CREATE TABLE shedlock (name VARCHAR(64) PRIMARY KEY, "
                            + "lock_until TIMESTAMP(3) NOT NULL, locked_at TIMESTAMP(3) NOT NULL, "
                            + "locked_by VARCHAR(255) NOT NULL)");
                    statement.execute("INSERT INTO shedlock VALUES "
                            + "('existing-job', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, "
                            + "'other-service')");
                }
            }
        }
        migrate(url);
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
                var statement = connection.createStatement()) {
            for (String table : new String[]{"notification_preferences", "push_devices",
                    "push_delivery_log", "shedlock"}) {
                try (var result = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
                    assertThat(result.next()).isTrue();
                    assertThat(result.getInt(1))
                            .isEqualTo(existingLocks && table.equals("shedlock") ? 1 : 0);
                }
            }
            try (var result = statement.executeQuery("SELECT EXECTYPE FROM DATABASECHANGELOG "
                    + "WHERE ID = '02-create-shedlock-if-missing'")) {
                assertThat(result.next()).isTrue();
                assertThat(result.getString(1)).isEqualTo(existingLocks ? "MARK_RAN" : "EXECUTED");
            }
            // Simulate an installation that recorded the original successful migration.
            statement.executeUpdate("UPDATE DATABASECHANGELOG "
                    + "SET MD5SUM = '9:00d9abeddad3423acb2cc41e3a2a3b8f' "
                    + "WHERE ID = '01-create-push-notifications'");
        }
        migrate(url);
    }

    private void migrate(String url) throws Exception {
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
                var resources = new ClassLoaderResourceAccessor()) {
            var database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            try (var liquibase = new Liquibase(
                    "db/changelog/db.changelog-master.yaml", resources, database)) {
                liquibase.update(new Contexts(), new LabelExpression());
            }
        }
    }
}
