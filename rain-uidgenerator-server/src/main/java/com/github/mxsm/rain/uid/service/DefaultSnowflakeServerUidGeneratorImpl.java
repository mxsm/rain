package com.github.mxsm.rain.uid.service;

import com.github.mxsm.rain.uid.common.DeployEnvType;
import com.github.mxsm.rain.uid.core.common.ErrorCode;
import com.github.mxsm.rain.uid.core.exception.UidUnavailableException;
import com.github.mxsm.rain.uid.core.snowflake.AbstractSnowflakeUidGenerator;
import com.github.mxsm.rain.uid.dao.SnowflakeNodeDao;
import com.github.mxsm.rain.uid.utils.NetUtils;
import com.github.mxsm.rain.uid.config.SnowflakeUidGeneratorConfig;

import com.github.mxsm.rain.uid.entity.SnowflakeNodeEntity;
import com.github.mxsm.rain.uid.observability.UidMetrics;
import java.util.Optional;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author mxsm
 * @date 2022/5/1 19:43
 * @Since 1.0.0
 */
@Service("defaultSnowflakeUidGeneratorImpl")
public class DefaultSnowflakeServerUidGeneratorImpl extends AbstractSnowflakeUidGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSnowflakeServerUidGeneratorImpl.class);

    private static final String EPOCH_DEFAULT = "2022-05-01";

    private final SnowflakeNodeDao snowflakeNodeDao;

    private final SnowflakeUidGeneratorConfig config;

    private final UidMetrics uidMetrics;

    @Value("${server.port:8080}")
    private int port;

    @Value("${server.address:}")
    private String hostName;

    private final DeployEnvType deployEnvType;

    public DefaultSnowflakeServerUidGeneratorImpl(SnowflakeUidGeneratorConfig config,
        SnowflakeNodeDao snowflakeNodeDao, UidMetrics uidMetrics) {
        super(Optional.ofNullable(config.getEpoch()).orElse(EPOCH_DEFAULT), config.isTimeBitsSecond(),
            config.getTimestampBits(), config.getMachineIdBits(), config.getSequenceBits(),
            config.getMaxBackwardMillis());
        this.config = config;
        this.snowflakeNodeDao = snowflakeNodeDao;
        this.uidMetrics = uidMetrics;
        this.deployEnvType = config.isContainer() ? DeployEnvType.CONTAINER : DeployEnvType.ACTUAL;
    }

    @PostConstruct
    private void init() {
        long machineId = getMachineId();
        getBitsAllocator().setMachineId(machineId);
        uidMetrics.setWorkerId(machineId);
    }

    @Override
    public long getMachineId() {
        Long configuredMachineId = config.getMachineId();
        if (configuredMachineId != null) {
            return validateMachineId(configuredMachineId);
        }
        if (config.isContainer()) {
            return validateMachineId(parsePodOrdinal(config.getPodName()));
        }
        try {
            if (StringUtils.isEmpty(hostName)) {
                hostName = NetUtils.getLocalAddress();
            }
            SnowflakeNodeEntity sf = snowflakeNodeDao.selectSnowflakeNode(NetUtils.address4Long(hostName), port);
            if (sf == null) {
                sf = new SnowflakeNodeEntity();
                sf.setHostName(NetUtils.address4Long(hostName));
                sf.setPort(port);
                sf.setDescription("");
                sf.setDeployEnvType(deployEnvType);
                snowflakeNodeDao.insertSnowflakeNode(sf);
            }
            long machineId = sf.getId().longValue();
            return validateMachineId(machineId);
        } catch (Exception e) {
            LOGGER.error("get machine id from db error", e);
            throw new UidUnavailableException(ErrorCode.WORKER_ID_UNAVAILABLE,
                "Unable to allocate snowflake worker id", e);
        }
    }

    @Override
    protected void onClockMovedBackwards(long rollbackMillis) {
        super.onClockMovedBackwards(rollbackMillis);
        uidMetrics.recordClockRollback();
    }

    private long validateMachineId(long machineId) {
        if (machineId < 0 || machineId > getBitsAllocator().getMaxMachineId()) {
            throw new UidUnavailableException(ErrorCode.WORKER_ID_UNAVAILABLE,
                "machine id " + machineId + " is outside 0-" + getBitsAllocator().getMaxMachineId());
        }
        return machineId;
    }

    private long parsePodOrdinal(String podName) {
        if (StringUtils.isBlank(podName)) {
            throw new UidUnavailableException(ErrorCode.WORKER_ID_UNAVAILABLE,
                "mxsm.uid.snowflake.pod-name must be configured for container deployment");
        }
        int dashIndex = podName.lastIndexOf('-');
        if (dashIndex < 0 || dashIndex == podName.length() - 1) {
            throw new UidUnavailableException(ErrorCode.WORKER_ID_UNAVAILABLE,
                "Unable to parse StatefulSet ordinal from pod name: " + podName);
        }
        try {
            return Long.parseLong(podName.substring(dashIndex + 1));
        } catch (NumberFormatException ex) {
            throw new UidUnavailableException(ErrorCode.WORKER_ID_UNAVAILABLE,
                "Unable to parse StatefulSet ordinal from pod name: " + podName, ex);
        }
    }

}
