+++
toc = true
title = "Java"
weight = 1
+++

## Example

### Sharding 

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

### Read-write splitting
 
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

### Sharding + Read-write splitting 

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

### Orchestration by registry

```java
    DataSource getDataSource() throws SQLException {
        return OrchestrationShardingDataSourceFactory.createDataSource(
                createDataSourceMap(), createShardingRuleConfig(), new HashMap<String, Object>(), new Properties(), 
                new OrchestrationConfiguration("orchestration-sharding-data-source", getRegistryCenterConfiguration(), false));
    }
    
    private RegistryCenterConfiguration getRegistryCenterConfiguration() {
        RegistryCenterConfiguration regConfig = new RegistryCenterConfiguration();
        regConfig.setServerLists("localhost:2181");
        regConfig.setNamespace("sharding-sphere-orchestration");
        return regConfig;
    }
``````

## Configuration reference

### Sharding

#### ShardingDataSourceFactory

| *Name*             | *DataType*                | *Description*               |
| ------------------ |  ------------------------ | --------------------------- |
| dataSourceMap      | Map\<String, DataSource\> | Data sources configuration  |
| shardingRuleConfig | ShardingRuleConfiguration | Sharding rule configuration |
| configMap (?)      | Map\<String, Object\>     | Config map                  |
| props (?)          | Properties                | Properties                  |

#### ShardingRuleConfiguration

| *Name*                                    | *DataType*           | *Description*                                                                                                   |
| ----------------------------------------- | ------------------------------------------ | ----------------------------------------------------------------------------------------- |
| tableRuleConfigs                          | Collection\<TableRuleConfiguration\>       | Table rule configuration                                                                  |
| bindingTableGroups (?)                    | Collection\<String\>                       | Binding table groups                                                                      |
| defaultDataSourceName (?)                 | String                                     | If table not configure at table rule, will route to defaultDataSourceName                 |
| defaultDatabaseShardingStrategyConfig (?) | ShardingStrategyConfiguration              | Default strategy for sharding databases                                                   |
| defaultTableShardingStrategyConfig (?)    | ShardingStrategyConfiguration              | Default strategy for sharding tables                                                      |
| defaultKeyGenerator (?)                   | KeyGenerator                               | Default key generator, default value is `io.shardingsphere.core.keygen.DefaultKeyGenerator` |
| masterSlaveRuleConfigs (?)                | Collection\<MasterSlaveRuleConfiguration\> | Read-write splitting rule configuration                                                   |

#### TableRuleConfiguration

| *Name*                             | *DataType*                    | *Description*                                                                                                                                                                                         |
| ---------------------------------- | ----------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| logicTable                         | String                        | Name of logic table                                                                                                                                                                                   |
| actualDataNodes (?)                | String                        | Describe data source names and actual tables, delimiter as point, multiple data nodes split by comma, support inline expression. Absent means sharding databases only. Example: ds${0..7}.tbl${0..7} |
| databaseShardingStrategyConfig (?) | ShardingStrategyConfiguration | Databases sharding strategy, use default databases sharding strategy if absent                                                                                                                        |
| tableShardingStrategyConfig (?)    | ShardingStrategyConfiguration | Tables sharding strategy, use default databases sharding strategy if absent                                                                                                                           |
| logicIndex (?)                     | String                        | Name if logic index. If use *DROP INDEX XXX* SQL in Oracle/PostgreSQL, This property needs to be set for finding the actual tables                                                                    |
| keyGeneratorColumnName (?)         | String                        | Key generator column name, do not use Key generator if absent                                                                                                                                         |
| keyGenerator (?)                   | KeyGenerator                  | Key generator, use default key generator if absent                                                                                                                                                    |

#### StandardShardingStrategyConfiguration

Subclass of ShardingStrategyConfiguration.

| *Name*                     | *DataType*               | *Description*                                    |
| -------------------------- | ------------------------ | ------------------------------------------------ |
| shardingColumn             | String                   | Name of sharding column                          |
| preciseShardingAlgorithm   | PreciseShardingAlgorithm | Precise sharding algorithm used for `=` and `IN` |
| rangeShardingAlgorithm (?) | RangeShardingAlgorithm   | Range sharding algorithm used for `BETWEEN`      |

#### ComplexShardingStrategyConfiguration

Subclass of ShardingStrategyConfiguration.

| *Name*            | *DataType*                   | *Description*                                                    |
| ----------------- | ---------------------------- | ---------------------------------------------------------------- |
| shardingColumns   | String                       | Names of sharding columns. Multiple columns separated with comma |
| shardingAlgorithm | ComplexKeysShardingAlgorithm | Complex sharding algorithm                                       |

#### InlineShardingStrategyConfiguration

Subclass of ShardingStrategyConfiguration.

| *Name*              | *DataType*  | *Description*                                                                                                                                    |
| ------------------- | ----------- | ------------------------------------------------------------------------------------------------------------------------------------------------ |
| shardingColumn      |  String     | Name of sharding column                                                                                                                          |
| algorithmExpression |  String     | Inline expression for sharding algorithm, more details please reference [Inline expression](/en/features/sharding/other-features/inline-expression) |

#### HintShardingStrategyConfiguration

Subclass of ShardingStrategyConfiguration.

| *Name*            | *DataType*            | *Description*           |
| ----------------- | --------------------- | ----------------------- |
| shardingAlgorithm | HintShardingAlgorithm | Hint sharding algorithm |

#### NoneShardingStrategyConfiguration

Subclass of ShardingStrategyConfiguration.

#### ShardingPropertiesConstant

Enumeration of properties.

| *Name*                             | *DataType* | *Description*                                                                  |
| ---------------------------------- | ---------- | ------------------------------------------------------------------------------ |
| sql.show (?)                       | boolean    | Print SQL parse and rewrite log, default value: false                          |
| executor.size (?)                  | int        | The number of SQL execution threads, zero means no limit. default value: 0     |
| max.connections.size.per.query (?) | int        | Max connection size for every query to every actual database. default value: 1 |

#### configMap

User-defined arguments.

### Read-write splitting

#### MasterSlaveDataSourceFactory

| *Name*                | *DataType*                   | *Description*                       |
| --------------------- | ---------------------------- | ----------------------------------- |
| dataSourceMap         | Map\<String, DataSource\>    | Map of data sources and their names |
| masterSlaveRuleConfig | MasterSlaveRuleConfiguration | Master slave rule configuration     |
| configMap (?)         | Map\<String, Object\>        | Config map                          |
| props (?)             | Properties                   | Properties                          |

#### MasterSlaveRuleConfiguration

| *Name*                   | *DataType*                      | *Description*                    |
| ------------------------ | ------------------------------- | -------------------------------- |
| name                     | String                          | Name of master slave data source |
| masterDataSourceName     | String                          | Name of master data source       |
| slaveDataSourceNames     | Collection\<String\>            | Names of Slave data sources      |
| loadBalanceAlgorithm (?) | MasterSlaveLoadBalanceAlgorithm | Load balance algorithm           |

#### configMap

User-defined arguments.

#### ShardingPropertiesConstant

Enumeration of properties.

| *Name*            | *DataType* | *Description*                                           |
| ----------------- | ---------- | ------------------------------------------------------- |
| sql.show (?)      | boolean    | To show SQLS or not, default value: false               |
| executor.size (?) | int        | The number of working threads, default value: CPU count |

### Orchestration

#### OrchestrationShardingDataSourceFactory

| *Name*              | *DataType*                 | *Description*                       |
| ------------------- |  ------------------------- | ----------------------------------- |
| dataSourceMap       | Map\<String, DataSource\>  | Same with ShardingDataSourceFactory |
| shardingRuleConfig  | ShardingRuleConfiguration  | Same with ShardingDataSourceFactory |
| configMap (?)       | Map\<String, Object\>      | Same with ShardingDataSourceFactory |
| props (?)           | Properties                 | Same with ShardingDataSourceFactory |
| orchestrationConfig | OrchestrationConfiguration | Orchestration configuration         |

#### OrchestrationMasterSlaveDataSourceFactory

| *Name*                | *DataType*                   | *Description*                          |
| --------------------- | ---------------------------- | -------------------------------------- |
| dataSourceMap         | Map\<String, DataSource\>    | Same with MasterSlaveDataSourceFactory |
| masterSlaveRuleConfig | MasterSlaveRuleConfiguration | Same with MasterSlaveDataSourceFactory |
| configMap (?)         | Map\<String, Object\>        | Same with MasterSlaveDataSourceFactory |
| props (?)             | Properties                   | Same with ShardingDataSourceFactory    |
| orchestrationConfig   | OrchestrationConfiguration   | Orchestration configuration            |
 
#### OrchestrationConfiguration

| *Name*          | *DataType*                  | *Description*                                                       |
| --------------- | --------------------------- | ------------------------------------------------------------------- |
| name            | String                      | Name of orchestration instance                                      |
| overwrite       | boolean                     | Use local configuration to overwrite registry center or not         |
| regCenterConfig | RegistryCenterConfiguration | Registry center configuration                                       |

#### RegistryCenterConfiguration

| *Name*                            | *DataType* | *Description*                                                                    |
| --------------------------------- | ---------- | -------------------------------------------------------------------------------- |
| serverLists                       | String     | Registry servers list, multiple split as comma. Example: host1:2181,host2:2181  |
| namespace                         | String     | Namespace of Registry                                                           |
| digest (?)                        | String     | Digest for Registry. Default is not need digest.                                |
| operationTimeoutMilliseconds (?)  | int        | Operation timeout time in milliseconds. Default is not timeout.                  |
| maxRetries (?)                    | int        | Max number of times to retry. Default value is 3                                 |
| retryIntervalMilliseconds (?)     | int        | Time interval in milliseconds on each retry. Default value is 1000 milliseconds. |
| timeToLiveSeconds (?)             | int        | Time to live in seconds of ephemeral keys. Default value is 60 seconds.          |
