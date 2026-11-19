CREATE TABLE IF NOT EXISTS `mxsm_allocation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `biz_code` varchar(128) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'business code',
  `max_id` bigint NOT NULL DEFAULT '1' COMMENT 'allocated max id cursor',
  `step` int NOT NULL COMMENT 'segment length',
  `description` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'description',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `biz_code_index` (`biz_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `mxsm_snowfalke_node` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `host_name` bigint NOT NULL COMMENT 'ip address as long',
  `port` int NOT NULL DEFAULT '1' COMMENT 'service port',
  `deploy_env_type` enum('ACTUAL','CONTAINER') COLLATE utf8mb4_general_ci DEFAULT 'ACTUAL' COMMENT 'deployment type',
  `description` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'description',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `mix_index` (`host_name`,`port`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
