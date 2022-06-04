# rain

[![Publish package to the Maven Central Repository and GitHub Packages](https://github.com/mxsm/rain/actions/workflows/maven-publish.yml/badge.svg?branch=main)](https://github.com/mxsm/rain/actions/workflows/maven-publish.yml)

分布式全局ID生成服务，ID生成分为两个模式：

- **segment（分段模式）**
- **snowflake（雪花算法）**

如何使用看如下介绍。



## Quick Start

### 1. 安装依赖

- JDK 11
- MySQL8
- Maven 3.8.5

安装好相关的依赖。

### 2. 数据库初始化

#### 2.1 创建表

运行一下sql脚本创建对应的数据库和表，脚本如下：

```sql
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
```

### 3. rain部署启动

#### 3.1  通过提供的Package

**第一步：下载 binary package**

可以从最新的[最新的稳定版本](https://github.com/mxsm/rain/releases)页面下载。获取  **`rain-server-1.0.1-SNAPSHOT.tar.gz`**

```shell
tar -zxvf rain-server-1.0.1-SNAPSHOT.tar.gz
cd rain-server-1.0.1-SNAPSHOT/
```

**第二步：修改conf/application.properties**

修改application.properties配置中数据库相关配置：

```properties
spring.datasource.url=jdbc:mysql://ip:port/uidgenerator?useUnicode=true&characterEncoding=utf-8
spring.datasource.username=xxx
spring.datasource.password=xxxxx
```

> Tips:  确保库地址, 名称, 端口号, 用户名和密码正确

**第三步：启动服务**

```shell
sh bin/start.sh
```

![image-20220604145105893](C:\Users\mxsm\AppData\Roaming\Typora\typora-user-images\image-20220604145105893.png)

### 4. Segment模式UID生成配置

修改配置conf/application.properties文件，以下是配置说明

| 配置                        | 默认值 | 说明                                                         |
| --------------------------- | ------ | ------------------------------------------------------------ |
| mxsm.uid.segment.threshold  | 40     | 缓存模式下当本地缓存阈值低于或者等于40%后就会去数据库加载segment填充，取值范围0-100 |
| mxsm.uid.segment.cache-size | 16     | 缓存模式下默认加载的缓存segment的数量                        |

threshold和cache-size的大小影响从数据中获取的segment的频率，cache-size如果设置的过大在停机维护项目的时候会造成UID的浪费，但是cache-size大可以在数据库发生宕机的情况下能够继续服务之前已经加载内存中的bizCode。

### 5. Snowflake模式UID生成配置

修改配置conf/application.properties文件，以下是配置说明:

| 配置                                | 默认值     | 说明                                                    |
| ----------------------------------- | ---------- | ------------------------------------------------------- |
| mxsm.uid.snowflake.timestamp-bits   | 41         | 雪花算法timestamp的位数                                 |
| mxsm.uid.snowflake.machine-id-bits  | 10         | 雪花算法machine id的位数                                |
| mxsm.uid.snowflake.sequence-bits    | 12         | 雪花算法序列号的位数                                    |
| mxsm.uid.snowflake.container        | false      | 是否为容器化部署                                        |
| mxsm.uid.snowflake.time-bits-second | false      | timestamp是否为秒                                       |
| mxsm.uid.snowflake.epoch            | 2022-05-01 | timestamp的相对时间，格式yyyy-MM-dd，并且在当前时间以前 |

timestamp-bits、machine-id-bits、sequence-bits三个位数和加起来要等于63。

### 6. Java SDK

maven client依赖：

```xml
<dependency>
  <groupId>com.github.mxsm</groupId>
  <artifactId>rain-uidgenerator-client</artifactId>
  <version>${latest version}</version>
</dependency>
```

使用例子

```java
UidClient client = UidClient.builder()
            .setUidGeneratorServerUir("http://172.29.250.21:8080") //设置服务地址
            .setSegmentNum(10) //设置获取的segment数量
            .setThreshold(20) //设置阈值
            .isSegmentUidFromRemote(false) //设置是否直接从服务器通过Restful接口的方式获取
            .build();
long uid = client.getSegmentUid("mxsm");
long uidRemote = client.getSegmentUid("mxsm", true);
long snowflake =  client.getSnowflakeUid();
```



## Source Code Quick Start

**第一步： clone代码**

```shell
git clone https://github.com/mxsm/rain.git
cd rain
```

**第二步：修改rain-uidgenerator-server项目中的application.properties**

```properties
spring.datasource.url=jdbc:mysql://ip:port/uidgenerator?useUnicode=true&characterEncoding=utf-8
spring.datasource.username=xxx
spring.datasource.password=xxxxx
```

**第三步：maven打包服务**

```shell
mvn clean package -DskipTests=true
```

**第四步：启动服务**

```shell
java -Xms1g -Xmx1g -jar ./rain-uidgenerator-server/target/rain-uidgenerator-server-1.0.1-SNAPSHOT.jar
```

## Documentation

**TODO**
