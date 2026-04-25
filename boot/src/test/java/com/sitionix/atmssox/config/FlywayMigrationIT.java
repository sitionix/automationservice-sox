package com.sitionix.atmssox.config;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.TimeZone;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

class FlywayMigrationIT {

    @Test
    void givenLegacySchemaWithRule_whenMigrateToLatest_thenReplaceAuthorTypeWithReferenceAndCleanRules() throws Exception {
        final TimeZone originalTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        try (final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")) {
            postgres.start();

            final String flywayLocation = "filesystem:" + Path.of("..", "db-migration").toAbsolutePath().normalize();

            final Flyway flywayToV9 = Flyway.configure()
                    .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                    .locations(flywayLocation)
                    .target("9")
                    .load();

            final var migrateToV9Result = flywayToV9.migrate();
            assertThat(migrateToV9Result.migrationsExecuted).isEqualTo(9);

            try (Connection connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
                 Statement statement = connection.createStatement()) {
                statement.executeUpdate("""
                        INSERT INTO agents (agent_id, user_id, name, description, instruction, status_id, created_at, updated_at)
                        VALUES ('11111111-1111-1111-1111-111111111111', 1, 'Agent', 'desc', 'instr', 2, now(), now())
                        """);
                statement.executeUpdate("""
                        INSERT INTO agent_rules (rule_id, agent_id, title, content, status_id, author_type, created_at, updated_at)
                        VALUES ('22222222-2222-2222-2222-222222222222',
                                '11111111-1111-1111-1111-111111111111',
                                'Legacy title',
                                'Legacy content',
                                1,
                                'USER',
                                now(),
                                now())
                        """);
            }

            final Flyway flywayToLatest = Flyway.configure()
                    .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                    .locations(flywayLocation)
                    .load();

            final var migrateToLatestResult = flywayToLatest.migrate();
            assertThat(migrateToLatestResult.migrationsExecuted).isEqualTo(1);

            try (Connection connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
                 Statement statement = connection.createStatement()) {
                try (ResultSet authorTypeColumn = statement.executeQuery("""
                        SELECT COUNT(*)
                        FROM information_schema.columns
                        WHERE table_schema = 'public'
                          AND table_name = 'agent_rules'
                          AND column_name = 'author_type'
                        """)) {
                    authorTypeColumn.next();
                    assertThat(authorTypeColumn.getLong(1)).isZero();
                }

                try (ResultSet authorTypeIdColumn = statement.executeQuery("""
                        SELECT COUNT(*)
                        FROM information_schema.columns
                        WHERE table_schema = 'public'
                          AND table_name = 'agent_rules'
                          AND column_name = 'author_type_id'
                          AND is_nullable = 'NO'
                        """)) {
                    authorTypeIdColumn.next();
                    assertThat(authorTypeIdColumn.getLong(1)).isEqualTo(1);
                }

                try (ResultSet authorTypeFk = statement.executeQuery("""
                        SELECT COUNT(*)
                        FROM information_schema.table_constraints
                        WHERE table_schema = 'public'
                          AND table_name = 'agent_rules'
                          AND constraint_name = 'fk_agent_rules_author_type_id'
                          AND constraint_type = 'FOREIGN KEY'
                        """)) {
                    authorTypeFk.next();
                    assertThat(authorTypeFk.getLong(1)).isEqualTo(1);
                }

                try (ResultSet authorTypeRows = statement.executeQuery("SELECT COUNT(*) FROM agent_rule_author_types")) {
                    authorTypeRows.next();
                    assertThat(authorTypeRows.getLong(1)).isEqualTo(2);
                }

                try (ResultSet rulesRows = statement.executeQuery("SELECT COUNT(*) FROM agent_rules")) {
                    rulesRows.next();
                    assertThat(rulesRows.getLong(1)).isZero();
                }
            }
        } finally {
            TimeZone.setDefault(originalTimeZone);
        }
    }

    @Test
    void givenLegacySchemaWithoutRules_whenMigrateToLatest_thenKeepTableEmptyAndApplyAuthorTypeReferenceSchema() throws Exception {
        final TimeZone originalTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        try (final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")) {
            postgres.start();

            final String flywayLocation = "filesystem:" + Path.of("..", "db-migration").toAbsolutePath().normalize();

            final Flyway flywayToV9 = Flyway.configure()
                    .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                    .locations(flywayLocation)
                    .target("9")
                    .load();

            flywayToV9.migrate();

            final Flyway flywayToLatest = Flyway.configure()
                    .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                    .locations(flywayLocation)
                    .load();

            final var migrateToLatestResult = flywayToLatest.migrate();
            assertThat(migrateToLatestResult.migrationsExecuted).isEqualTo(1);

            try (Connection connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
                 Statement statement = connection.createStatement()) {
                try (ResultSet rulesRows = statement.executeQuery("SELECT COUNT(*) FROM agent_rules")) {
                    rulesRows.next();
                    assertThat(rulesRows.getLong(1)).isZero();
                }

                try (ResultSet authorTypeRows = statement.executeQuery("SELECT COUNT(*) FROM agent_rule_author_types")) {
                    authorTypeRows.next();
                    assertThat(authorTypeRows.getLong(1)).isEqualTo(2);
                }
            }
        } finally {
            TimeZone.setDefault(originalTimeZone);
        }
    }

    @Test
    void givenFullyMigratedSchema_whenMigrateAgain_thenExecuteZeroMigrations() {
        final TimeZone originalTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        try (final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")) {
            postgres.start();

            final String flywayLocation = "filesystem:" + Path.of("..", "db-migration").toAbsolutePath().normalize();

            final Flyway flyway = Flyway.configure()
                    .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                    .locations(flywayLocation)
                    .load();

            final var firstResult = flyway.migrate();
            final var secondResult = flyway.migrate();

            assertThat(firstResult.migrationsExecuted).isEqualTo(10);
            assertThat(secondResult.migrationsExecuted).isZero();
        } finally {
            TimeZone.setDefault(originalTimeZone);
        }
    }
}
