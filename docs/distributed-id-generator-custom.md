### 1. 背景

为什么要自己设计一个分布式ID生成器？这个主要是用于生成IM消息的ID。IM消息有个特点跟用户结合比较紧密，单聊是跟用户相关，群聊是跟群相关。进一步分析群聊的群也可以看做一个特殊的用户，所以分布式ID生成器一个条件就是以人作为区分纬度。同时也需要兼顾业务类型的ID生成。

### 2.分布式ID要求

#### 2.1 业务要求

- 支持以用户为纬度生成全局ID,对于一个用户在分布式ID生成器中生成的ID是唯一的
- 支持以业务作为纬度生成全局ID,例如电商业务，仓储业务。
- 支持生成全局ID,以使用者为纬度。

#### 2.2 性能要求

- 支持客户端本地生成，就算服务端在使用过程中宕机不可用，在一段时间内不影响本地ID的生成
- 支持HTTP模式获取生成ID
- 支持容器化部署
- 支持每秒百万ID生成

### 3. mxsm-uidgenerator实现

为了实现上面的业务需求以及性能要求，提供了segment和snowflake两种模式

#### 3.1 segment模式

segment模式使用的数据库方案，数据库表设计如下：

```sql
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
```

根据biz_code从数据库中获取对应的分配信息，然后在内存中生成ID。每一个biz_code的ID生成是相互隔离的。例如biz_code的编码为mxsm，step = 100,那么获取的生成ID的号段为：1-101，当消耗完成从数据库中根据如下SQL继续加载：

```sql
begin;
SELECT id, max_id as maxId, step FROM mxsm_allocation WHERE biz_code = #{bizCode} FOR UPDATE;
UPDATE mxsm_allocation SET max_id = max_id + step WHERE  biz_code = #{bizCode}
commit;
```

为了提高生成的并发以及效率，可以一次性加载多个号段到内存中，当内存中的的号段消耗了，剩余的号段低于设置的阈值，然后去数据库获取消耗的数量的号段填充到缓存中：

![https://www.liuchengtu.com/lct/#X9f51cc114a7edcae9f46b07cb799e2e2](https://raw.githubusercontent.com/mxsm/picture/main/docs/im/DistributedIDGenerator/%E5%88%86%E6%AE%B5%E6%A8%A1%E5%BC%8F%E7%BC%93%E5%AD%98%E7%94%9F%E6%88%90.png)

对于mxsm_allocation表的数量过大，可以采用分库分表，通过预测公司的biz_code数量来判断分库分表。力度最小就是以用户作为单位进行分库分表如果以公司有20亿用户来计算(这个已经算是很大了)，单表2000w数据，那么就需要部署100台数据库服务。大部分公司以单表2000w数据，部署3-5台基本上能够全部满足。

![分布式ID生成器-分段模式](https://raw.githubusercontent.com/mxsm/picture/main/docs/im/DistributedIDGenerator/%E5%88%86%E5%B8%83%E5%BC%8FID%E7%94%9F%E6%88%90%E5%99%A8-%E5%88%86%E6%AE%B5%E6%A8%A1%E5%BC%8F.png)

**分段式分布式ID生成器的优点：**

- segment模式可以很方便的进行线性拓展，性能能够支持绝大部分场景
- 生成的ID在biz_code趋势递增。
- 有很好的容灾性，对于客户端模式即使服务器发生宕机，在一定时间内客户端还是可以提供ID生成，如果数据库宕机，由于生成ID的服务内部存在缓存，在一定时间内还能对外正常的提供服务。(服务时间长短取决于消耗ID的数据和换成的大小)

**分段式分布式ID生成器的缺点：**

- ID不够随机，对于用作订单号会让人估算出订单数量，但是如果用在消息ID这个就可以
- 数据库宕机会导致服务一段时间过后不能使用。依赖数据库。
- segment缓存设置的太大或者step设置太大如果服务宕机或者重启会造成浪费。

#### 3.2 snowflake模式

雪花算法的位图结构如下：

![雪花算法ID的结构](https://raw.githubusercontent.com/mxsm/picture/main/docs/im/DistributedIDGenerator/%E9%9B%AA%E8%8A%B1%E7%AE%97%E6%B3%95ID%E7%9A%84%E7%BB%93%E6%9E%84.png)

雪花算法是趋势递增的ID生成方案。由于snowflake方案的bit位设计，即是“1+41+10+12”的方式组装ID号，时间戳使用的是毫秒，且其他的位数都固定。这里提供了调整时间戳、机器ID、序列号的位数。以及时间戳单位是秒还是毫秒的解决方案(方案参考[uid-generator](https://github.com/baidu/uid-generator)设计)。同时对于机器ID,每次服务启动将IP和port存入数据库，以主键ID作为机器号。如果是用标准的雪花算法最多能部署1024台机器。

```sql
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

**时间回拨解决：**

- 在服务器时间回拨1S内，通过等待实现时间同步
- 如果超过设置的1s，直接报错生产ID失败

**雪花算法位数长度可修改变动好处：**

- 在机器能够满足调用的情况下，可以小机器的ID的位数，增加序列号来提高单位时间内生成的最大数量。可以根据个人的需求进行调整。
- 时间戳的单位是毫秒还是秒，这个能够决定当前服务使用的长短。

**客户端本地生成ID: **

将雪花算法集成到客户端，机器ID在机器Bits位数的最大值和最小值中随机生成一个。好处：能够在本地生成ID，无网络的消耗、并发高。缺点：如果随机生成的机器ID重复的数量太多。并且高并发生成的情况下会出现ID重复的情况。不适合全局唯一的情况。

> Tips: 本地通过雪花算法随机机器ID生成UID的方式，笔者公司就在使用。重复ID的概率比较低，毕竟需要同一个业务中的同一张表的ID一样。

### 4. 总结

- segment+本地Cache的模式实现了搞并发和搞可用，即使数据库不可用也不会让服务对外立马不可用。
- snowflake模式对位数的调整，能够满足绝大部分人的需要，高并发以及服务可用时间长短。