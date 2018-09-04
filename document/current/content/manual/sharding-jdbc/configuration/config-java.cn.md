+++
toc = true
title = "Java配置"
weight = 1
+++

## 配置示例

### 数据分片

```java
     DataSource getShardingDataSource() throws SQLException {
         ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
         shardingRuleConfig.getTableRuleConfigs().add(getOrderTableRuleConfiguration());
         shardingRuleConfig.getTableRuleConfigs().add(getOrderItemTableRuleConfiguration());
         shardingRuleConfig.getBindingTableGroups().add("t_order, t_order_item");
         shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("user_id", "ds${user_id % 2}"));
         shardingRuleConfig.setDefaultTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("order_id", new ModuloShardingTableAlgorithm()));
         return ShardingDataSourceFactory.createDataSource(createDataSourceMap(), shardingRuleConfig);
     }
     
     TableRuleConfiguration getOrderTableRuleConfiguration() {
         TableRuleConfiguration result = new TableRuleConfiguration();
         result.setLogicTable("t_order");
         result.setActualDataNodes("ds${0..1}.t_order${0..1}");
         result.setKeyGeneratorColumnName("order_id");
         return result;
     }
     
     TableRuleConfiguration getOrderItemTableRuleConfiguration() {
         TableRuleConfiguration result = new TableRuleConfiguration();
         result.setLogicTable("t_order_item");
         result.setActualDataNodes("ds${0..1}.t_order_item${0..1}");
         return result;
     }
     
     Map<String, DataSource> createDataSourceMap() {
         Map<String, DataSource> result = new HashMap<>();
         result.put("ds0", DataSourceUtil.createDataSource("ds0"));
         result.put("ds1", DataSourceUtil.createDataSource("ds1"));
         return result;
     }
```

### 读写分离

```java
     DataSource getMasterSlaveDataSource() throws SQLException {
         MasterSlaveRuleConfiguration masterSlaveRuleConfig = new MasterSlaveRuleConfiguration();
         masterSlaveRuleConfig.setName("ds_master_slave");
         masterSlaveRuleConfig.setMasterDataSourceName("ds_master");
         masterSlaveRuleConfig.setSlaveDataSourceNames(Arrays.asList("ds_slave0", "ds_slave1"));
         return MasterSlaveDataSourceFactory.createDataSource(createDataSourceMap(), masterSlaveRuleConfig, new LinkedHashMap<String, Object>(), new Properties());
     }
     
     Map<String, DataSource> createDataSourceMap() {
         Map<String, DataSource> result = new HashMap<>();
         result.put("ds_master", DataSourceUtil.createDataSource("ds_master"));
         result.put("ds_slave0", DataSourceUtil.createDataSource("ds_slave0"));
         result.put("ds_slave1", DataSourceUtil.createDataSource("ds_slave1"));
         return result;
     }
```

### 数据分片 + 读写分离

```java
    DataSource getDataSource() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(getOrderTableRuleConfiguration());
        shardingRuleConfig.getTableRuleConfigs().add(getOrderItemTableRuleConfiguration());
        shardingRuleConfig.getBindingTableGroups().add("t_order, t_order_item");
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("user_id", new ModuloShardingDatabaseAlgorithm()));
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("order_id", new ModuloShardingTableAlgorithm()));
        shardingRuleConfig.setMasterSlaveRuleConfigs(getMasterSlaveRuleConfigurations());
        return ShardingDataSourceFactory.createDataSource(createDataSourceMap(), shardingRuleConfig, new HashMap<String, Object>(), new Properties());
    }
    
    TableRuleConfiguration getOrderTableRuleConfiguration() {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("t_order");
        result.setActualDataNodes("ds_${0..1}.t_order_${[0, 1]}");
        result.setKeyGeneratorColumnName("order_id");
        return result;
    }
    
    TableRuleConfiguration getOrderItemTableRuleConfiguration() {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("t_order_item");
        result.setActualDataNodes("ds_${0..1}.t_order_item_${[0, 1]}");
        return result;
    }
    
    List<MasterSlaveRuleConfiguration> getMasterSlaveRuleConfigurations() {
        MasterSlaveRuleConfiguration masterSlaveRuleConfig1 = new MasterSlaveRuleConfiguration("ds_0", "demo_ds_master_0", Arrays.asList("demo_ds_master_0_slave_0", "demo_ds_master_0_slave_1"));
        MasterSlaveRuleConfiguration masterSlaveRuleConfig2 = new MasterSlaveRuleConfiguration("ds_1", "demo_ds_master_1", Arrays.asList("demo_ds_master_1_slave_0", "demo_ds_master_1_slave_1"));
        return Lists.newArrayList(masterSlaveRuleConfig1, masterSlaveRuleConfig2);
    }
    
    Map<String, DataSource> createDataSourceMap() {
        final Map<String, DataSource> result = new HashMap<>();
        result.put("demo_ds_master_0", DataSourceUtil.createDataSource("demo_ds_master_0"));
        result.put("demo_ds_master_0_slave_0", DataSourceUtil.createDataSource("demo_ds_master_0_slave_0"));
        result.put("demo_ds_master_0_slave_1", DataSourceUtil.createDataSource("demo_ds_master_0_slave_1"));
        result.put("demo_ds_master_1", DataSourceUtil.createDataSource("demo_ds_master_1"));
        result.put("demo_ds_master_1_slave_0", DataSourceUtil.createDataSource("demo_ds_master_1_slave_0"));
        result.put("demo_ds_master_1_slave_1", DataSourceUtil.createDataSource("demo_ds_master_1_slave_1"));
        return result;
    }
```

### 使用Zookeeper的数据治理

```java
    DataSource dataSource = new OrchestrationShardingDataSource(
                 createShardingDataSource(createDataSourceMap(), createShardingRuleConfig(), new HashMap<String, Object>(), new Properties()), 
                     new OrchestrationConfiguration("orchestration-sharding-data-source", getRegistryCenterConfiguration(), false, OrchestrationType.SHARDING)););
    
    private RegistryCenterConfiguration getRegistryCenterConfiguration() {
        ZookeeperConfiguration result = new ZookeeperConfiguration();
        result.setServerLists("localhost:2181");
        result.setNamespace("orchestration-demo");
        return result;
    }
```

### 使用Etcd的数据治理

```java
    DataSource dataSource = new OrchestrationShardingDataSource (
                 createShardingDataSource(createDataSourceMap(), createShardingRule(), new HashMap<String, Object>(), new Properties()), 
                 new OrchestrationConfiguration("orchestration-sharding-data-source", getRegistryCenterConfiguration(), false, OrchestrationType.SHARDING));
    
    private RegistryCenterConfiguration getRegistryCenterConfiguration() {
        EtcdConfiguration result = new EtcdConfiguration();
        result.setServerLists("http://localhost:2379");
        return result;
    }
```

## 配置项说明

### 数据分片

#### ShardingDataSourceFactory

数据分片的数据源创建工厂。

| *名称*             | *数据类型*                 | *说明*          |
| ------------------ |  ------------------------ | -------------- |
| dataSourceMap      | Map\<String, DataSource\> | 数据源配置      |
| shardingRuleConfig | ShardingRuleConfiguration | 数据分片配置规则 |
| configMap (?)      | Map\<String, Object\>     | 用户自定义配置   |
| props (?)          | Properties                | 属性配置        |

#### ShardingRuleConfiguration

分片规则配置对象。

| *名称*                                     | *数据类型*                                  | *说明*                                                                  |
| ----------------------------------------- | ------------------------------------------ | ----------------------------------------------------------------------- |
| tableRuleConfigs                          | Collection\<TableRuleConfiguration\>       | 分片规则列表                                                              |
| bindingTableGroups (?)                    | Collection\<String\>                       | 绑定表规则列表                                                            |
| defaultDataSourceName (?)                 | String                                     | 未配置分片规则的表将通过默认数据源定位                                       |
| defaultDatabaseShardingStrategyConfig (?) | ShardingStrategyConfiguration              | 默认分库策略                                                              |
| defaultTableShardingStrategyConfig (?)    | ShardingStrategyConfiguration              | 默认分表策略                                                              |
| defaultKeyGenerator (?)                   | KeyGenerator                               | 默认自增列值生成器，缺省使用io.shardingsphere.core.keygen.DefaultKeyGenerator |
| masterSlaveRuleConfigs (?)                | Collection\<MasterSlaveRuleConfiguration\> | 读写分离规则，缺省表示不使用读写分离                                         |

#### TableRuleConfiguration

表分片规则配置对象。

| *名称*                              | *数据类型*                     | *说明*                                                                                                                                                                                                      |
| ---------------------------------- | ----------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| logicTable                         | String                        | 逻辑表名称                                                                                                                                                                                                   |
| actualDataNodes (?)                | String                        | 由数据源名 + 表名组成，以小数点分隔。多个表以逗号分隔，支持inline表达式。缺省表示使用已知数据源与逻辑表名称生成数据节点。用于广播表（即每个库中都需要一个同样的表用于关联查询，多为字典表）或只分库不分表且所有库的表结构完全一致的情况 |
| databaseShardingStrategyConfig (?) | ShardingStrategyConfiguration | 分库策略，缺省表示使用默认分库策略                                                                                                                                                                              |
| tableShardingStrategyConfig (?)    | ShardingStrategyConfiguration | 分表策略，缺省表示使用默认分表策略                                                                                                                                                                              |
| logicIndex (?)                     | String                        | 逻辑索引名称，对于分表的Oracle/PostgreSQL数据库中DROP INDEX XXX语句，需要通过配置逻辑索引名称定位所执行SQL的真实分表                                                                                                  |
| keyGeneratorColumnName (?)         | String                        | 自增列名称，缺省表示不使用自增主键生成器                                                                                                                                                                         |
| keyGenerator (?)                   | KeyGenerator                  | 自增列值生成器，缺省表示使用默认自增主键生成器                                                                                                                                                                    |

#### StandardShardingStrategyConfiguration

ShardingStrategyConfiguration的实现类，用于单分片键的标准分片场景。

| *名称*                      | *数据类型*                | *说明*                  |
| -------------------------- | ------------------------ | ----------------------- |
| shardingColumn             | String                   | 分片列名称               |
| preciseShardingAlgorithm   | PreciseShardingAlgorithm | 精确分片算法，用于=和IN   |
| rangeShardingAlgorithm (?) | RangeShardingAlgorithm   | 范围分片算法，用于BETWEEN |

#### ComplexShardingStrategyConfiguration

ShardingStrategyConfiguration的实现类，用于多分片键的复合分片场景。

| *名称*             | *数据类型*                    | *说明*                   |
| ----------------- | ---------------------------- | ------------------------ |
| shardingColumns   | String                       | 分片列名称，多个列以逗号分隔 |
| shardingAlgorithm | ComplexKeysShardingAlgorithm | 复合分片算法               |

#### InlineShardingStrategyConfiguration

ShardingStrategyConfiguration的实现类，用于配置行表达式分片策略。

| *名称*               | *数据类型*  | *说明*                                                                                                   |
| ------------------- | ----------- | ------------------------------------------------------------------------------------------------------- |
| shardingColumn      |  String     | 分片列名称                                                                                               |
| algorithmExpression |  String     | 分片算法行表达式，需符合groovy语法，详情请参考[行表达式](/cn/features/sharding/other-features/inline-expression) |

#### HintShardingStrategyConfiguration

ShardingStrategyConfiguration的实现类，用于配置Hint方式分片策略。

| *名称*             | *数据类型*             | *说明*      |
| ----------------- | --------------------- | ----------- |
| shardingAlgorithm | HintShardingAlgorithm | Hint分片算法 |

#### NoneShardingStrategyConfiguration

ShardingStrategyConfiguration的实现类，用于配置不分片的策略。

#### PropertiesConstant

属性配置项，可以为以下属性。

| *名称*             | *数据类型* | *说明*                      |
| ----------------- | --------- | --------------------------- |
| sql.show (?)      | boolean   | 是否开启SQL显示，默认值: false |
| executor.size (?) | int       | 工作线程数量，默认值: CPU核数  |

#### configMap

用户自定义配置。

### 读写分离

#### MasterSlaveDataSourceFactory

读写分离的数据源创建工厂。

| *名称*                 | *数据类型*                    | *说明*             |
| --------------------- | ---------------------------- | ------------------ |
| dataSourceMap         | Map\<String, DataSource\>    | 数据源与其名称的映射  |
| masterSlaveRuleConfig | MasterSlaveRuleConfiguration | 读写分离规则         |
| configMap (?)         | Map\<String, Object\>        | 用户自定义配置       |
| props (?)             | Properties                   | 属性配置            |

#### MasterSlaveRuleConfiguration

读写分离规则配置对象。

| *名称*                    | *数据类型*                       | *说明*           |
| ------------------------ | ------------------------------- | ---------------- |
| name                     | String                          | 读写分离数据源名称 |
| masterDataSourceName     | String                          | 主库数据源名称    |
| slaveDataSourceNames     | Collection\<String\>            | 从库数据源名称列表 |
| loadBalanceAlgorithm (?) | MasterSlaveLoadBalanceAlgorithm | 从库负载均衡算法   |

#### configMap

用户自定义配置。

#### PropertiesConstant

属性配置项，可以为以下属性。

| *名称*             | *数据类型* | *说明*                      |
| ----------------- | --------- | --------------------------- |
| sql.show (?)      | boolean   | 是否开启SQL显示，默认值: false |
| executor.size (?) | int       | 工作线程数量，默认值: CPU核数  |


### 数据治理

#### OrchestrationShardingDataSource

数据分片 + 数据治理的数据源。

| *名称*               | *数据类型*                  | *说明*                      |
| ------------------- |  ------------------------- | --------------------------- |
| shardingDataSource  | ShardingDataSource         | 被治理的数据源                |
| orchestrationConfig | OrchestrationConfiguration | 数据治理规则配置              |

#### OrchestrationMasterSlaveDataSource

读写分离 + 数据治理的数据源。

| *名称*                 | *数据类型*                    | *说明*                         |
| --------------------- | ---------------------------- | ------------------------------ |
| masterSlaveDataSource | MasterSlaveDataSource        | 被治理的数据源                   |
| orchestrationConfig   | OrchestrationConfiguration   | 数据治理规则配置                 |
 
#### OrchestrationConfiguration

数据治理规则配置对象。

| *名称*           | *数据类型*                   | *说明*                                                     |
| --------------- | --------------------------- | ---------------------------------------------------------- |
| name            | String                      | 数据治理实例名称                                             |
| overwrite       | boolean                     | 本地配置是否覆盖注册中心配置，如果可覆盖，每次启动都以本地配置为准 |
| type            | OrchestrationType           | 数据源类型，可选值：SHARDING，MASTER_SLAVE                    |
| regCenterConfig | RegistryCenterConfiguration | 注册中心配置                                                |

#### ZookeeperConfiguration

RegistryCenterConfiguration的实现类，用于配置Zookeeper注册中心。

| *名称*                             | *数据类型* | *说明*                                                                                 |
| --------------------------------- | ---------- | ------------------------------------------------------------------------------------- |
| serverLists                       | String     | 连接Zookeeper服务器的列表。包括IP地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181 |
| namespace                         | String     | Zookeeper的命名空间                                                                    |
| baseSleepTimeMilliseconds (?)     | int        | 等待重试的间隔时间的初始毫秒数，默认1000毫秒                                               |
| maxSleepTimeMilliseconds (?)      | int        | 等待重试的间隔时间的最大毫秒数，默认3000毫秒                                               |
| maxRetries (?)                    | int        | 连接失败后的最大重试次数，默认3次                                                         |
| sessionTimeoutMilliseconds (?)    | int        | 会话超时毫秒数，默认60000毫秒                                                            |
| connectionTimeoutMilliseconds (?) | int        | 连接超时毫秒数，默认15000毫秒                                                            |
| digest (?)                        | String     | 连接Zookeeper的权限令牌。缺省为不需要权限验证                                              |

#### EtcdConfiguration

RegistryCenterConfiguration的实现类，用于配置Etcd注册中心。

| *名称*                         | *数据类型* | *说明*                                                                                          |
| ----------------------------- | ---------- | ---------------------------------------------------------------------------------------------- |
| serverLists                   | String     | 连接Etcd服务器的列表。包括IP地址和端口号。多个地址用逗号分隔。如: http://host1:2379,http://host2:2379 |
| timeToLiveSeconds (?)         | int        | 临时节点存活秒数，默认60秒                                                                        |
| timeoutMilliseconds (?)       | int        | 请求超时毫秒数，默认500毫秒                                                                       |
| retryIntervalMilliseconds (?) | int        | 重试间隔毫秒数，默认200毫秒                                                                       |
| maxRetries (?)                | int        | 请求失败后的最大重试次数，默认3次                                                                  |
