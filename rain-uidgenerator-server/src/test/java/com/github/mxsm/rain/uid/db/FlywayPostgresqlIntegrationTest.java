package com.github.mxsm.rain.uid.db;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers(disabledWithoutDocker = true)
class FlywayPostgresqlIntegrationTest extends DatabaseIntegrationTest {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16");

    @Override
    PostgreSQLContainer database() {
        return POSTGRES;
    }
}
