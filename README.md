# rain

[![Publish package to the Maven Central Repository and GitHub Packages](https://github.com/mxsm/rain/actions/workflows/maven-publish.yml/badge.svg?branch=main)](https://github.com/mxsm/rain/actions/workflows/maven-publish.yml)

分布式全局ID生成服务，ID生成分为两个模式：

- **segment**
- **snowflake**

如何使用看如下介绍。

### 1. Quick Start

这里介绍项目如何启动以及如何使用。clone项目到本地：

```shell
git clone https://github.com/mxsm/distributed-id-generator.git
```

### 2. 安装依赖

- JDK 17
- MySQL8
- Maven

安装好相关的依赖。

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

修改application.properties配置中数据库相关配置：

```properties
spring.datasource.url=jdbc:mysql://ip:prot/uidgenerator?useUnicode=true&characterEncoding=utf-8
spring.datasource.username=xxx
spring.datasource.password=xxxxx
```

> Tips:  确保库地址, 名称, 端口号, 用户名和密码正确

### 3. Segment模式UID生成配置

修改配置项目uidgenerator-server中application.properties文件，以下是配置说明

| 配置                        | 默认值 | 说明                                                         |
| --------------------------- | ------ | ------------------------------------------------------------ |
| mxsm.uid.segment.threshold  | 40     | 缓存模式下当本地缓存阈值低于或者等于40%后就会去数据库加载segment填充，取值范围0-100 |
| mxsm.uid.segment.cache-size | 16     | 缓存模式下默认加载的缓存segment的数量                        |

threshold和cache-size的大小影响从数据中获取的segment的频率，cache-size如果设置的过大在停机维护项目的时候会造成UID的浪费，但是cache-size大可以在数据库发生宕机的情况下能够继续服务之前已经加载内存中的bizCode。

### 4. Snowflake模式UID生成配置

修改配置项目uidgenerator-server中application.properties文件，以下是配置说明:

| 配置                                | 默认值     | 说明                                                    |
| ----------------------------------- | ---------- | ------------------------------------------------------- |
| mxsm.uid.snowflake.timestamp-bits   | 41         | 雪花算法timestamp的位数                                 |
| mxsm.uid.snowflake.machine-id-bits  | 10         | 雪花算法machine id的位数                                |
| mxsm.uid.snowflake.sequence-bits    | 12         | 雪花算法序列号的位数                                    |
| mxsm.uid.snowflake.container        | false      | 是否为容器化部署                                        |
| mxsm.uid.snowflake.time-bits-second | false      | timestamp是否为秒                                       |
| mxsm.uid.snowflake.epoch            | 2022-05-01 | timestamp的相对时间，格式yyyy-MM-dd，并且在当前时间以前 |

timestamp-bits、machine-id-bits、sequence-bits三个位数和加起来要等于63。

### 5. 启动mxsm-uidgenerator服务

```shell
cd distributed-id-generator/mxsm-uidgenerator
mvn clean package -DskipTests=true
java -Xms1g -Xmx1g -jar uidgenerator-server/target/uidgenerator-server-0.0.1-SNAPSHOT.jar
```

启动后：

![image-20220506082523363](https://raw.githubusercontent.com/mxsm/picture/main/docs/im/DistributedIDGenerator/image-20220506082523363.png)

### 6. Restful接口

#### 6.1 注册bizCode

**描述**

> 注册bizCode对应的UID初始数据

**请求类型** 

> POST

**请求URL**

> /api/v1/segment/rg

**请求body参数**

| 名称    | 类型   | 是否必须 | 描述                            |
| ------- | ------ | -------- | ------------------------------- |
| bizCode | string | 是       | 业务编码，作为获取UID的唯一标识 |
| step    | int    | 是       | segemt的长度                    |

**返回参数**

| 参数类型        | 描述         |
| --------------- | ------------ |
| Result<Boolean> | 注册返回结果 |

**示例：**

- 请求示例

  ```shell
  curl -L -X POST '172.29.250.21:8080/api/v1/segment/rg' \
  -H 'Content-Type: application/json' \
  --data-raw '{
      "bizCode":"mxsm",
      "step":10
  }'
  ```

- 返回示例

  ```json
  {
      "data": true,
      "status": "SUCCESS",
      "msg": "SUCCESS"
  }
  ```

  

#### 6.2 获取segment模式UID

**描述**

> 获取segment模式指定bizCode的UID

**请求类型** 

> GET

**请求URL**

> /api/v1/segment/uid/{bizCode}

**请求参数**

| 名称    | 类型   | 是否必须 | 描述                            |
| ------- | ------ | -------- | ------------------------------- |
| bizCode | string | 是       | 业务编码，作为获取UID的唯一标识 |

**返回参数**

| 参数类型 | 描述      |
| -------- | --------- |
| long     | 生成的UID |

**示例：**

- 请求示例

  ```shell
  curl -L -X GET '172.29.250.21:8080/api/v1/segment/uid/l0p999zwGn4l352lxyU3OcPg19093649'
  ```

- 返回示例

  ```json
  3
  ```

#### 6.3 获取segment

**描述**

> 获取指定bizCode的segment

**请求类型** 

> GET

**请求URL**

> /api/v1/segment/list/{bizCode}

**请求参数**

| 名称       | 类型   | 是否必须 | 描述                            |
| ---------- | ------ | -------- | ------------------------------- |
| bizCode    | string | 是       | 业务编码，作为获取UID的唯一标识 |
| segmentNum | int    | 是       | 获取segment的数量               |

**返回参数**

| 参数类型              | 描述        |
| --------------------- | ----------- |
| Result<List<Segment>> | segment列表 |

**示例：**

- 请求示例

  ```shell
  curl -L -X GET '172.29.250.21:8080/api/v1/segment/list/l0p999zwGn4l352lxyU3OcPg19093649?segmentNum=3'
  ```

- 返回示例

  ```json
  {
      "data": [
          {
              "segmentStartNum": 29182730,
              "stepSize": 1006301,
              "ok": true
          },
          {
              "segmentStartNum": 30189031,
              "stepSize": 1006301,
              "ok": true
          },
          {
              "segmentStartNum": 31195332,
              "stepSize": 1006301,
              "ok": true
          }
      ],
      "status": "SUCCESS",
      "msg": "SUCCESS"
  }
  ```

#### 6.4 获取snowflake模式UID

**描述**

> 获取snowflake模式下的UID

**请求类型** 

> GET

**请求URL**

> /api/v1/snowflake/uid

**请求参数**

无

**返回参数**

| 参数类型 | 描述 |
| -------- | ---- |
| long     | UID  |

**示例：**

- ```shell
  curl -L -X GET '172.29.250.21:8080/api/v1/snowflake/uid'
  ```

- 返回示例

  ```json
  1958215675834368
  ```

#### 6.4 解析snowflake的UID

**描述**

> 将snowflake模式下的UID解析成字符串

**请求类型** 

> GET

**请求URL**

> /api/v1/snowflake/parse/{uid}

**请求参数**

| 名称 | 类型 | 是否必须 | 描述                 |
| ---- | ---- | -------- | -------------------- |
| uid  | long | 是       | snowflake的生成的UID |

**返回参数**

| 参数类型 | 描述 |
| -------- | ---- |
| long     | UID  |

**示例：**

- ```shell
  curl -L -X GET '172.29.250.21:8080/api/v1/snowflake/parse/1958215675834368'
  ```

- 返回示例

  ```json
  {
      "data": {
          "uid": 1958215675834368,
          "timestamp": "2022-05-06 09:41:14",
          "machineId": 7,
          "sequence": 0
      },
      "status": "SUCCESS",
      "msg": "SUCCESS"
  }
  ```

### 7. Java SDK

maven坐标

```xml
<dependency>
    <groupId>com.github.mxsm</groupId>
    <artifactId>mxsm-uidgenerator</artifactId>
    <version>${version}</version>
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

