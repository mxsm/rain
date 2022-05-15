DROP DATABASE IF EXISTS `uidgenerator`;
CREATE DATABASE `uidgenerator` ;
use `uidgenerator`;

DROP TABLE IF EXISTS mxsm_allocation;
CREATE TABLE `mxsm_allocation` (
 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
 `biz_code` varchar(128) COLLATE utf8mb4_general_ci NOT NULL COMMENT '业务编码(用户ID,使用业务方编码)',
 `max_id` bigint NOT NULL DEFAULT '1' COMMENT '最大值',
 `step` int NOT NULL COMMENT '步长',
 `description` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '说明',
 `create_time` timestamp NOT NULL COMMENT '创建时间',
 `update_time` timestamp NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
 PRIMARY KEY (`id`),
 UNIQUE KEY `biz_code_index` (`biz_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS mxsm_snowfalke_node;
CREATE TABLE `mxsm_snowfalke_node` (
 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
 `host_name` bigint NOT NULL COMMENT 'IP地址',
 `port` int NOT NULL DEFAULT '1' COMMENT '端口',
 `deploy_env_type` enum('ACTUAL','CONTAINER') COLLATE utf8mb4_general_ci DEFAULT 'ACTUAL' COMMENT '部署环境类型',
 `description` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '说明',
 `create_time` timestamp NOT NULL COMMENT '创建时间',
 `update_time` timestamp NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
 PRIMARY KEY (`id`),
 UNIQUE KEY `mix_index` (`host_name`,`port`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;