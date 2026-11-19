package com.github.mxsm.rain.uid.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.mxsm.rain.uid.UidGeneratorBootstrap;
import com.github.mxsm.rain.uid.common.DeployEnvType;
import com.github.mxsm.rain.uid.core.common.ErrorCode;
import com.github.mxsm.rain.uid.core.exception.UidGenerateException;
import com.github.mxsm.rain.uid.core.segment.Segment;
import com.github.mxsm.rain.uid.dao.SnowflakeNodeDao;
import com.github.mxsm.rain.uid.entity.SnowflakeNodeEntity;
import com.github.mxsm.rain.uid.service.AllocationService;
import com.github.mxsm.rain.uid.service.DefaultSnowflakeServerUidGeneratorImpl;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.JdbcDatabaseContainer;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class DatabaseIntegrationTest {

    private ConfigurableApplicationContext application;
    private AllocationService allocations;
    private JdbcTemplate jdbc;

    abstract JdbcDatabaseContainer<?> database();

    static ConfigurableApplicationContext startApplication(JdbcDatabaseContainer<?> database) {
        return new SpringApplicationBuilder(UidGeneratorBootstrap.class).run(
            "--spring.profiles.active=local", "--server.address=127.0.0.1", "--server.port=0",
            "--RAIN_DATASOURCE_URL=" + database.getJdbcUrl(), "--RAIN_DATASOURCE_USERNAME=" + database.getUsername(),
            "--RAIN_DATASOURCE_PASSWORD=" + database.getPassword());
    }

    @BeforeAll
    void startServer() {
        application = startApplication(database());
        allocations = application.getBean(AllocationService.class);
        jdbc = application.getBean(JdbcTemplate.class);
    }

    @AfterAll
    void stopServer() {
        if (application != null) {
            application.close();
        }
    }

    @Test
    void reservesDisjointSegmentsInConcurrentTransactions() throws Exception {
        allocations.registerBizCode("orders", 100);
        List<List<Segment>> batches = concurrently(32, () -> allocations.getSegments("orders", 3));
        List<Segment> segments = batches.stream().flatMap(List::stream)
            .sorted(Comparator.comparingLong(Segment::getSegmentStartNum)).toList();
        assertEquals(96, segments.size());
        for (int i = 0; i < segments.size(); i++) {
            assertEquals(1 + i * 100L, segments.get(i).getSegmentStartNum());
            assertEquals(100, segments.get(i).getLength());
        }
        assertEquals(9601L, cursor("orders"));
    }

    @Test
    void appliesTheSameBusinessKeyEqualityToRegistrationAndAllocation() {
        allocations.registerBizCode("Café ", 10);
        assertThrows(DuplicateKeyException.class, () -> allocations.registerBizCode("CAFE", 20));
        assertEquals(1, allocations.getSegments("cafe", 1).getFirst().getSegmentStartNum());
        assertEquals(11, allocations.getSegments("Café  ", 1).getFirst().getSegmentStartNum());
        UidGenerateException missing = assertThrows(UidGenerateException.class,
            () -> allocations.getSegments("unregistered", 1));
        assertEquals(ErrorCode.BIZ_CODE_NOT_FOUND, missing.getErrorCode());
    }

    @Test
    void advancesLargeStepsAndRollsBackAnOverflow() {
        allocations.registerBizCode("large", Integer.MAX_VALUE);
        List<Segment> segments = allocations.getSegments("large", 2);
        assertEquals(1L + Integer.MAX_VALUE, segments.getLast().getSegmentStartNum());
        assertEquals(1L + 2L * Integer.MAX_VALUE, cursor("large"));

        jdbc.update("UPDATE mxsm_allocation SET max_id = ? WHERE biz_code = ?", Long.MAX_VALUE - 1, "large");
        UidGenerateException overflow = assertThrows(UidGenerateException.class,
            () -> allocations.getSegments("large", 1));
        assertEquals(ErrorCode.SEGMENT_ALLOCATE_FAILED, overflow.getErrorCode());
        assertEquals(Long.MAX_VALUE - 1, cursor("large"));
    }

    @Test
    void reservesFreshRangesWithinOneTransactionAndRollsThemBackTogether() {
        allocations.registerBizCode("rollback", 10);
        application.getBean(TransactionTemplate.class).executeWithoutResult(transaction -> {
            assertEquals(1, allocations.getSegments("rollback", 1).getFirst().getSegmentStartNum());
            assertEquals(11, allocations.getSegments("rollback", 1).getFirst().getSegmentStartNum());
            transaction.setRollbackOnly();
        });
        assertEquals(1, cursor("rollback"));
    }

    @Test
    void concurrentRegistrationsReturnOneWorkerIdPerAddressAndPort() throws Exception {
        SnowflakeNodeDao nodes = application.getBean(SnowflakeNodeDao.class);
        List<Long> ids = concurrently(16, () -> registerNode(nodes, 9001));
        assertNotNull(ids.getFirst());
        assertTrue(ids.stream().allMatch(ids.getFirst()::equals));
        assertEquals(ids.getFirst(), nodes.selectSnowflakeNode(123L, 9001).getId());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM mxsm_snowfalke_node WHERE host_name = 123 AND port = 9001",
            Integer.class));
        assertNotEquals(ids.getFirst(), registerNode(nodes, 9002));
    }

    @Test
    void servesIdsOverHttpAndReusesTheWorkerAfterRestart() {
        RestClient http = RestClient.create("http://127.0.0.1:" + application.getEnvironment().getProperty("local.server.port"));
        assertEquals(true, http.post().uri("/api/v1/segment/rg").body(Map.of("bizCode", "http", "step", 10))
            .retrieve().body(Map.class).get("data"));
        assertEquals(1, http.post().uri("/api/v1/segment/uid/http").retrieve().body(Map.class).get("data"));
        Number snowflake = (Number) http.post().uri("/api/v1/snowflake/uid").retrieve().body(Map.class).get("data");
        assertTrue(snowflake.longValue() > 0);

        long worker = application.getBean(DefaultSnowflakeServerUidGeneratorImpl.class).getMachineId();
        stopServer();
        startServer();
        assertEquals(worker, application.getBean(DefaultSnowflakeServerUidGeneratorImpl.class).getMachineId());
    }

    private long cursor(String bizCode) {
        return jdbc.queryForObject("SELECT max_id FROM mxsm_allocation WHERE biz_code = ?", Long.class, bizCode);
    }

    private static Long registerNode(SnowflakeNodeDao nodes, int port) {
        SnowflakeNodeEntity node = new SnowflakeNodeEntity();
        node.setHostName(123L);
        node.setPort(port);
        node.setDeployEnvType(DeployEnvType.ACTUAL);
        nodes.insertSnowflakeNode(node);
        return node.getId();
    }

    private static <T> List<T> concurrently(int count, Callable<T> task) throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<T>> futures = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    return task.call();
                }));
            }
            start.countDown();
            List<T> results = new ArrayList<>();
            for (Future<T> future : futures) {
                results.add(future.get(30, TimeUnit.SECONDS));
            }
            return results;
        }
    }
}
