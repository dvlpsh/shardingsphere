+++
title = "数据分片"
weight = 1
+++

## 配置入口

类名称：org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration

可配置属性：

| *名称*                               | *数据类型*                                          | *说明*                | *默认值* |
| ----------------------------------- | --------------------------------------------------- | -------------------- | ------- |
| tables (+)                          | Collection\<ShardingTableRuleConfiguration\>        | 分片表规则列表         | -       |
| autoTables (+)                      | Collection\<ShardingAutoTableRuleConfiguration\>    | 自动化分片表规则列表    | -       |
| bindingTableGroups (*)              | Collection\<String\>                                | 绑定表规则列表         | 无       |
| broadcastTables (*)                 | Collection\<String\>                                | 广播表规则列表         | 无       |
| defaultDatabaseShardingStrategy (?) | ShardingStrategyConfiguration                       | 默认分库策略           | 不分片   |
| defaultTableShardingStrategy (?)    | ShardingStrategyConfiguration                       | 默认分表策略           | 不分片   |
| defaultKeyGenerateStrategy (?)      | KeyGeneratorConfiguration                           | 默认自增列生成器配置    | 雪花算法 |
| shardingAlgorithms (+)              | Map\<String, ShardingSphereAlgorithmConfiguration\> | 分片算法名称和配置      | 无      |
| keyGenerators (?)                   | Map\<String, ShardingSphereAlgorithmConfiguration\> | 自增列生成算法名称和配置 | 无      |

## 分片表配置

类名称：org.apache.shardingsphere.sharding.api.config.ShardingTableRuleConfiguration

可配置属性：

| *名称*                        | *数据类型*                     | *说明*                                                            | *默认值*                                                                            |
| ---------------------------- | ----------------------------- | ----------------------------------------------------------------- | ---------------------------------------------------------------------------------- |
| logicTable                   | String                        | 分片逻辑表名称                                                      | -                                                                                  |
| actualDataNodes (?)          | String                        | 由数据源名 + 表名组成，以小数点分隔。<br />多个表以逗号分隔，支持行表达式 | 使用已知数据源与逻辑表名称生成数据节点，用于广播表或只分库不分表且所有库的表结构完全一致的情况 |
| databaseShardingStrategy (?) | ShardingStrategyConfiguration | 分库策略                                                           | 使用默认分库策略                                                                     |
| tableShardingStrategy (?)    | ShardingStrategyConfiguration | 分表策略                                                           | 使用默认分表策略                                                                     |
| keyGenerateStrategy (?)      | KeyGeneratorConfiguration     | 自增列生成器                                                        | 使用默认自增主键生成器                                                               |

## 自动分片表配置

类名称：org.apache.shardingsphere.sharding.api.config.ShardingAutoTableRuleConfiguration

可配置属性：

| *名称*                   | *数据类型*                     | *说明*                       | *默认值*            |
| ----------------------- | ----------------------------- | ---------------------------- | ------------------ |
| logicTable              | String                        | 分片逻辑表名称                 | -                  |
| actualDataSources (?)   | String                        | 数据源名称，多个数据源以逗号分隔 | 使用全部配置的数据源  |
| shardingStrategy (?)    | ShardingStrategyConfiguration | 分片策略                      | 使用默认分片策略      |
| keyGenerateStrategy (?) | KeyGeneratorConfiguration     | 自增列生成器                   | 使用默认自增主键生成器 |

## 分片策略配置

### 标准分片策略配置

类名称：org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration

可配置属性：

| *名称*                 | *数据类型* | *说明*      |
| --------------------- | ---------- | ---------- |
| shardingColumn        | String     | 分片列名称   |
| shardingAlgorithmName | String     | 分片算法名称 |

### 复合分片策略配置

类名称：org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration

可配置属性：

| *名称*                 | *数据类型* | *说明*                    |
| --------------------- | ---------- | ------------------------ |
| shardingColumns       | String     | 分片列名称，多个列以逗号分隔 |
| shardingAlgorithmName | String     | 分片算法名称               |

### Hint 分片策略配置

类名称：org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration

可配置属性：

| *名称*                 | *数据类型*  | *说明*      |
| --------------------- | ---------- | ----------- |
| shardingAlgorithmName | String     | 分片算法名称  |

### 不分片策略配置

类名称：org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration

可配置属性：无

## 自增主键策略配置

类名称：org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration

可配置属性：

| *名称*           | *数据类型* | *说明*         |
| ---------------- | -------- | -------------- |
| column           | String   | 自增列名称      |
| keyGeneratorName | String   | 自增主键算法名称 |

## 分片算法配置

类名称：org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration

### 标准分片算法配置

Apache ShardingSphere 内置的标准分片算法实现类包括：

#### 行表达式分片算法

类型：INLINE

可配置属性：

| *属性名称*                                 | *数据类型* | *说明*                                              | *默认值* |
| ----------------------------------------- | --------- | --------------------------------------------------- | ------- |
| algorithm.expression                      | String    | 分片算法的行表达式                                    | -       |
| allow.range.query.with.inline.sharding (?)| boolean   | 是否允许范围查询。注意：范围查询会无视分片策略，进行全路由 | false   |

#### 取模分片算法

类型：MOD

可配置属性：

| *属性名称*      | *数据类型* | *说明*  |
| -------------- | --------- | ------- |
| sharding.count | int       | 分片数量 |

#### 哈希取模分片算法

类型：HASH_MOD

可配置属性：

| *属性名称*      | *数据类型* | *说明*  |
| -------------- | --------- | ------- |
| sharding.count | int       | 分片数量 |

#### 基于分片容量的范围分片算法

类型：VOLUME_RANGE

可配置属性：

| *属性名称*       | *数据类型* | *说明*                      |
| --------------- | --------- | -------------------------- |
| range.lower     | long      | 范围下界，超过边界的数据会报错 |
| range.upper     | long      | 范围上界，超过边界的数据会报错 |
| sharding.volume | long      | 分片容量                    |

#### 基于分片边界的范围分片算法

类型：BOUNDARY_RANGE

可配置属性：

| *属性名称*       | *数据类型* | *说明*                            |
| --------------- | --------- | --------------------------------- |
| sharding.ranges | String    | 分片的范围边界，多个范围边界以逗号分隔 |

#### 定长时间段分片算法

类型：FIXED_INTERVAL

可配置属性：

| *属性名称*        | *数据类型* | *说明*                                          |
| ---------------- | --------- | ----------------------------------------------- |
| datetime.lower   | String    | 分片的起始时间范围，时间戳格式：yyyy-MM-dd HH:mm:ss |
| datetime.upper   | String    | 分片的结束时间范围，时间戳格式：yyyy-MM-dd HH:mm:ss |
| sharding.seconds | long      | 单一分片所能承载的最大时间，单位：秒                |

#### 基于可变时间范围的分片算法

类型：MUTABLE_INTERVAL

可配置属性：

| *属性名称*            | *数据类型* | *说明*                              |
| -------------------- | --------- | ----------------------------------- |
| datetime.format      | String    | 时间戳格式，例如：yyyy-MM-dd HH:mm:ss |
| table.suffix.format  | String    | TODO                                |
| datetime.lower       | String    | TODO                                |
| datetime.upper       | String    | TODO                                |
| datetime.step.unit   | String    | TODO                                |
| datetime.step.amount | String    | TODO                                |

### 复合分片算法配置

Apache ShardingSphere 暂无内置复合分片算法。

### Hint 分片算法配置

Apache ShardingSphere 暂无内置 Hint 分片算法。

## 自增主键算法配置

Apache ShardingSphere 内置的自增主键算法包括：

### 雪花算法

类型：SNOWFLAKE

可配置属性：

| *属性名称*                                     | *数据类型* | *说明*                                                                                                                                                                                         | *默认值* |
| --------------------------------------------- | --------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------ |
| worker.id (?)                                 | long      | 工作机器唯一标识                                                                                                                                                                                 | 0      |
| max.vibration.offset (?)                      | int       | 最大抖动上限值，范围[0, 4096)。注：若使用此算法生成值作分片值，建议配置此属性。此算法在不同毫秒内所生成的 key 取模 2^n (2^n一般为分库或分表数) 之后结果总为 0 或 1。为防止上述分片问题，建议将此属性值配置为 (2^n)-1 | 1      |
| max.tolerate.time.difference.milliseconds (?) | long      | 最大容忍时钟回退时间，单位：毫秒                                                                                                                                                                   | 10 毫秒 |

### UUID

类型：UUID

可配置属性：无
