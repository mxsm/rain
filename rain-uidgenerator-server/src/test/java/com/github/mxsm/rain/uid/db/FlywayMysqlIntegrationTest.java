package com.github.mxsm.rain.uid.db;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.mxsm.rain.uid.service.AllocationService;
import com.github.mxsm.rain.uid.service.DefaultSnowflakeServerUidGeneratorImpl;
import java.nio.file.Files;
import java.nio.file.Path;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

@Testcontainers(disabledWithoutDocker = true)
class FlywayMysqlIntegrationTest extends DatabaseIntegrationTest {

    @Container
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4");

    @Override
    MySQLContainer database() {
        return MYSQL;
    }

    @Test
    void upgradesAnExistingV1WithoutResettingCursorsOrWorkerIds(@TempDir Path oldLocation) throws Exception {
        String script = "V1__init_uid_schema.sql";
        try (var source = new ClassPathResource("db/migration/mysql/" + script).getInputStream()) {
            Files.copy(source, oldLocation.resolve(script));
        }
        try (var legacy = new MySQLContainer("mysql:8.4")) {
            legacy.start();
            Flyway old = Flyway.configure().dataSource(legacy.getJdbcUrl(), legacy.getUsername(), legacy.getPassword())
                .locations("filesystem:" + oldLocation).load();
            old.migrate();
            // Checksum of the V1 shipped before migrations were split by vendor.
            assertEquals(-726151852, old.info().current().getChecksum());
            try (var connection = legacy.createConnection(""); var statement = connection.createStatement()) {
                statement.executeUpdate("INSERT INTO mxsm_allocation (biz_code, max_id, step) VALUES ('legacy', 9601, 100)");
                statement.executeUpdate("INSERT INTO mxsm_snowfalke_node (id, host_name, port) VALUES (42, 2130706433, 0)");
            }
            try (var upgraded = startApplication(legacy)) {
                Flyway flyway = upgraded.getBean(Flyway.class);
                flyway.validate();
                assertEquals(1, flyway.info().applied().length);
                assertEquals(9601, upgraded.getBean(AllocationService.class)
                    .getSegments("legacy", 1).getFirst().getSegmentStartNum());
                assertEquals(42, upgraded.getBean(DefaultSnowflakeServerUidGeneratorImpl.class).getMachineId());
            }
        }
    }
}
