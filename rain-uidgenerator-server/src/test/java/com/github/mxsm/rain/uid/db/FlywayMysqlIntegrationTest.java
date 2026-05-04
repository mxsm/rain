package com.github.mxsm.rain.uid.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
class FlywayMysqlIntegrationTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
        .withDatabaseName("uidgenerator")
        .withUsername("rain")
        .withPassword("rain");

    @Test
    void flywayCreatesSchemaAndConcurrentAllocationsDoNotOverlap() throws Exception {
        Flyway.configure()
            .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
            .locations("classpath:db/migration")
            .load()
            .migrate();
        try (Connection connection = connection()) {
            connection.createStatement().executeUpdate(
                "INSERT INTO mxsm_allocation (biz_code, max_id, step) VALUES ('biz', 1, 100)");
        }

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<Range> first = executor.submit(allocationTask());
        Future<Range> second = executor.submit(allocationTask());
        Range firstRange = first.get();
        Range secondRange = second.get();
        executor.shutdownNow();

        assertFalse(firstRange.overlaps(secondRange));
        assertEquals(300, firstRange.length());
        assertEquals(300, secondRange.length());
    }

    private Callable<Range> allocationTask() {
        return () -> {
            try (Connection connection = connection()) {
                connection.setAutoCommit(false);
                try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE mxsm_allocation SET max_id = LAST_INSERT_ID(max_id + step * ?) WHERE biz_code = ?")) {
                    statement.setInt(1, 3);
                    statement.setString(2, "biz");
                    statement.executeUpdate();
                }
                long newMax;
                try (ResultSet resultSet = connection.createStatement().executeQuery("SELECT LAST_INSERT_ID()")) {
                    resultSet.next();
                    newMax = resultSet.getLong(1);
                }
                connection.commit();
                return new Range(newMax - 300, newMax);
            }
        };
    }

    private Connection connection() throws Exception {
        return DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
    }

    private record Range(long startInclusive, long endExclusive) {

        boolean overlaps(Range other) {
            return startInclusive < other.endExclusive && other.startInclusive < endExclusive;
        }

        long length() {
            return endExclusive - startInclusive;
        }
    }
}
