package com.github.mxsm.rain.uid.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.mxsm.rain.uid.config.SnowflakeUidGeneratorConfig;
import com.github.mxsm.rain.uid.core.exception.UidUnavailableException;
import com.github.mxsm.rain.uid.dao.SnowflakeNodeDao;
import com.github.mxsm.rain.uid.entity.SnowflakeNodeEntity;
import com.github.mxsm.rain.uid.observability.UidMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

class DefaultSnowflakeServerUidGeneratorImplTest {

    @Test
    void parsesStatefulSetOrdinalAsMachineId() {
        SnowflakeUidGeneratorConfig config = new SnowflakeUidGeneratorConfig();
        config.setContainer(true);
        config.setPodName("rain-uidgenerator-7");
        DefaultSnowflakeServerUidGeneratorImpl generator = new DefaultSnowflakeServerUidGeneratorImpl(
            config, noDatabaseDao(), new UidMetrics(new SimpleMeterRegistry()));

        assertEquals(7, generator.getMachineId());
    }

    @Test
    void rejectsInvalidPodNameInContainerMode() {
        SnowflakeUidGeneratorConfig config = new SnowflakeUidGeneratorConfig();
        config.setContainer(true);
        config.setPodName("rain-uidgenerator");
        DefaultSnowflakeServerUidGeneratorImpl generator = new DefaultSnowflakeServerUidGeneratorImpl(
            config, noDatabaseDao(), new UidMetrics(new SimpleMeterRegistry()));

        assertThrows(UidUnavailableException.class, generator::getMachineId);
    }

    private SnowflakeNodeDao noDatabaseDao() {
        return new SnowflakeNodeDao() {
            @Override
            public SnowflakeNodeEntity selectSnowflakeNode(long hostName, int port) {
                return null;
            }

            @Override
            public void insertSnowflakeNode(SnowflakeNodeEntity sf) {
            }
        };
    }
}
