+++
toc = true
title = "Spring Boot配置"
weight = 3
+++

## 注意事项

行表达式标识符可以使用`${...}`或`$->{...}`，但前者与Spring本身的属性文件占位符冲突，因此在Spring环境中使用行表达式标识符建议使用`$->{...}`。

## 配置示例

### 数据分片

```properties
sharding.jdbc.datasource.names=ds0,ds1

sharding.jdbc.datasource.ds0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds0.url=jdbc:mysql://localhost:3306/ds0
sharding.jdbc.datasource.ds0.username=root
sharding.jdbc.datasource.ds0.password=

sharding.jdbc.datasource.ds1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds1.url=jdbc:mysql://localhost:3306/ds1
sharding.jdbc.datasource.ds1.username=root
sharding.jdbc.datasource.ds1.password=

sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column=user_id
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-expression=ds$->{user_id % 2}

sharding.jdbc.config.sharding.tables.t-order.actual-data-nodes=ds$->{0..1}.t_order$->{0..1}
sharding.jdbc.config.sharding.tables.t-order.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t-order.table-strategy.inline.algorithm-expression=t_order$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t-order.key-generator-column-name=order_id
sharding.jdbc.config.sharding.tables.t-order-item.actual-data-nodes=ds$->{0..1}.t_order_item$->{0..1}
sharding.jdbc.config.sharding.tables.t-order-item.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t-order-item.table-strategy.inline.algorithm-expression=t_order_item$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t-order-item.key-generator-column-name=order_item_id
```

### 读写分离

```properties
sharding.jdbc.datasource.names=ds_master,ds_slave0,ds_slave1

sharding.jdbc.datasource.ds-master.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds-master.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds-master.url=jdbc:mysql://localhost:3306/ds_master
sharding.jdbc.datasource.ds-master.username=root
sharding.jdbc.datasource.ds-master.password=

sharding.jdbc.datasource.ds-slave0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds-slave0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds-slave0.url=jdbc:mysql://localhost:3306/ds_slave0
sharding.jdbc.datasource.ds-slave0.username=root
sharding.jdbc.datasource.ds-slave0.password=

sharding.jdbc.datasource.ds-slave1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds-slave1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds-slave1.url=jdbc:mysql://localhost:3306/ds_slave1
sharding.jdbc.datasource.ds-slave1.username=root
sharding.jdbc.datasource.ds-slave1.password=

sharding.jdbc.config.masterslave.load-balance-algorithm-type=round_robin
sharding.jdbc.config.masterslave.name=ds_ms
sharding.jdbc.config.masterslave.master-data-source-name=ds_master
sharding.jdbc.config.masterslave.slave-data-source-names=ds_slave0,ds_slave1
```

### 数据分片 + 读写分离

```properties
sharding.jdbc.datasource.names=ds_master0,ds_master1,ds_master0_slave0,ds_master0_slave1,ds_master1_slave0,ds_master1_slave1

sharding.jdbc.datasource.ds-master0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds-master0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds-master0.url=jdbc:mysql://localhost:3306/ds_master0
sharding.jdbc.datasource.ds-master0.username=root
sharding.jdbc.datasource.ds-master0.password=

sharding.jdbc.datasource.ds-master0-slave0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds-master0-slave0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds-master0-slave0.url=jdbc:mysql://localhost:3306/ds_master0_slave0
sharding.jdbc.datasource.ds-master0-slave0.username=root
sharding.jdbc.datasource.ds-master0-slave0.password=
sharding.jdbc.datasource.ds-master0-slave1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds-master0-slave1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds-master0-slave1.url=jdbc:mysql://localhost:3306/ds_master0_slave1
sharding.jdbc.datasource.ds-master0-slave1.username=root
sharding.jdbc.datasource.ds-master0-slave1.password=

sharding.jdbc.datasource.ds-master1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds-master1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds-master1.url=jdbc:mysql://localhost:3306/ds_master1
sharding.jdbc.datasource.ds-master1.username=root
sharding.jdbc.datasource.ds-master1.password=

sharding.jdbc.datasource.ds-master1-slave0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds-master1-slave0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds-master1-slave0.url=jdbc:mysql://localhost:3306/ds_master1_slave0
sharding.jdbc.datasource.ds-master1-slave0.username=root
sharding.jdbc.datasource.ds-master1-slave0.password=
sharding.jdbc.datasource.ds-master1-slave1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds-master1-slave1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds-master1-slave1.url=jdbc:mysql://localhost:3306/ds_master1_slave1
sharding.jdbc.datasource.ds-master1-slave1.username=root
sharding.jdbc.datasource.ds-master1-slave1.password=

sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column=user_id
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-expression=ds$->{user_id % 2}

sharding.jdbc.config.sharding.tables.t-order.actual-data-nodes=ds$->{0..1}.t_order$->{0..1}
sharding.jdbc.config.sharding.tables.t-order.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t-order.table-strategy.inline.algorithm-expression=t_order$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t-order.key-generator-column-name=order_id
sharding.jdbc.config.sharding.tables.t-order-item.actual-data-nodes=ds$->{0..1}.t_order_item$->{0..1}
sharding.jdbc.config.sharding.tables.t-order-item.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t-order-item.table-strategy.inline.algorithm-expression=t_order_item$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t-order-item.key-generator-column-name=order_item_id

sharding.jdbc.config.sharding.master-slave-rules.ds0.master-data-source-name=ds_master0
sharding.jdbc.config.sharding.master-slave-rules.ds0.slave-data-source-names=ds_master0_slave0, ds_master0_slave1
sharding.jdbc.config.sharding.master-slave-rules.ds1.master-data-source-name=ds_master1
sharding.jdbc.config.sharding.master-slave-rules.ds1.slave-data-source-names=ds_master1_slave0, ds_master1_slave1
```

### 使用Zookeeper的数据治理

```properties
sharding.jdbc.datasource.names=ds,ds0,ds1
sharding.jdbc.datasource.ds.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds.driver-class-name=org.h2.Driver
sharding.jdbc.datasource.ds.url=jdbc:mysql://localhost:3306/ds
sharding.jdbc.datasource.ds.username=root
sharding.jdbc.datasource.ds.password=

sharding.jdbc.datasource.ds0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds0.url=jdbc:mysql://localhost:3306/ds0
sharding.jdbc.datasource.ds0.username=root
sharding.jdbc.datasource.ds0.password=

sharding.jdbc.datasource.ds1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds1.url=jdbc:mysql://localhost:3306/ds1
sharding.jdbc.datasource.ds1.username=root
sharding.jdbc.datasource.ds1.password=

sharding.jdbc.config.sharding.default-data-source-name=ds
sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column=user_id
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-inline-expression=ds$->{user_id % 2}
sharding.jdbc.config.sharding.tables.t-order.actual-data-nodes=ds$->{0..1}.t_order$->{0..1}
sharding.jdbc.config.sharding.tables.t-order.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t-order.table-strategy.inline.algorithm-inline-expression=t_order$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t-order.key-generator-column-name=order_id
sharding.jdbc.config.sharding.tables.t-order-item.actual-data-nodes=ds$->{0..1}.t_order_item$->{0..1}
sharding.jdbc.config.sharding.tables.t-order-item.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t-order-item.table-strategy.inline.algorithm-inline-expression=t_order_item$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t-order-item.key-generator-column-name=order_item_id

sharding.jdbc.config.orchestration.name=spring_boot_ds_sharding
sharding.jdbc.config.orchestration.overwrite=true
sharding.jdbc.config.orchestration.zookeeper.namespace=orchestration-spring-boot-sharding-test
sharding.jdbc.config.orchestration.zookeeper.server-lists=localhost:2181
```

### 使用Etcd的数据治理

```properties
sharding.jdbc.datasource.names=ds,ds0,ds1
sharding.jdbc.datasource.ds.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds.driver-class-name=org.h2.Driver
sharding.jdbc.datasource.ds.url=jdbc:mysql://localhost:3306/ds
sharding.jdbc.datasource.ds.username=root
sharding.jdbc.datasource.ds.password=

sharding.jdbc.datasource.ds0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds0.url=jdbc:mysql://localhost:3306/ds0
sharding.jdbc.datasource.ds0.username=root
sharding.jdbc.datasource.ds0.password=

sharding.jdbc.datasource.ds1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds1.url=jdbc:mysql://localhost:3306/ds1
sharding.jdbc.datasource.ds1.username=root
sharding.jdbc.datasource.ds1.password=

sharding.jdbc.config.sharding.default-data-source-name=ds
sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column=user_id
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-inline-expression=ds$->{user_id % 2}
sharding.jdbc.config.sharding.tables.t-order.actual-data-nodes=ds$->{0..1}.t_order$->{0..1}
sharding.jdbc.config.sharding.tables.t-order.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t-order.table-strategy.inline.algorithm-inline-expression=t_order$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t-order.key-generator-column-name=order_id
sharding.jdbc.config.sharding.tables.t-order-item.actual-data-nodes=ds$->{0..1}.t_order_item$->{0..1}
sharding.jdbc.config.sharding.tables.t-order-item.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t-order-item.table-strategy.inline.algorithm-inline-expression=t_order_item$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t-order-item.key-generator-column-name=order_item_id

sharding.jdbc.config.orchestration.name=spring_boot_ds_sharding
sharding.jdbc.config.orchestration.overwrite=true
sharding.jdbc.config.orchestration.etcd.server-lists=localhost:2379
```

## 配置项说明

### 数据分片

```properties
sharding.jdbc.datasource.names= #数据源名称，多数据源以逗号分隔

sharding.jdbc.datasource.<data-source-name>.type= #数据库连接池类名称
sharding.jdbc.datasource.<data-source-name>.driver-class-name= #数据库驱动类名
sharding.jdbc.datasource.<data-source-name>.url= #数据库url连接
sharding.jdbc.datasource.<data-source-name>.username= #数据库用户名
sharding.jdbc.datasource.<data-source-name>.password= #数据库密码
sharding.jdbc.datasource.<data-source-name>.xxx= #数据库连接池的其它属性

sharding.jdbc.config.sharding.tables.<logic-table-name>.actual-data-nodes= #由数据源名 + 表名组成，以小数点分隔。多个表以逗号分隔，支持inline表达式。缺省表示使用已知数据源与逻辑表名称生成数据节点。用于广播表（即每个库中都需要一个同样的表用于关联查询，多为字典表）或只分库不分表且所有库的表结构完全一致的情况

#分库策略，缺省表示使用默认分库策略，以下的分片策略只能选其一

#用于单分片键的标准分片场景
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.standard.sharding-column= #分片列名称
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.standard.precise-algorithm-class-name= #精确分片算法类名称，用于=和IN。该类需实现PreciseShardingAlgorithm接口并提供无参数的构造器
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.standard.range-algorithm-class-name= #范围分片算法类名称，用于BETWEEN，可选。该类需实现RangeShardingAlgorithm接口并提供无参数的构造器

#用于多分片键的复合分片场景
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.complex.sharding-columns= #分片列名称，多个列以逗号分隔
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.complex.algorithm-class-name= #复合分片算法类名称。该类需实现ComplexKeysShardingAlgorithm接口并提供无参数的构造器

#行表达式分片策略
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.inline.sharding-column= #分片列名称
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.inline.algorithm-inline-expression= #分片算法行表达式，需符合groovy语法

#Hint分片策略
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.hint.algorithm-class-name= #Hint分片算法类名称。该类需实现HintShardingAlgorithm接口并提供无参数的构造器

#分表策略，同分库策略
sharding.jdbc.config.sharding.tables.<logic-table-name>.table-strategy.xxx= #省略

sharding.jdbc.config.sharding.tables.<logic-table-name>.key-generator-column-name= #自增列名称，缺省表示不使用自增主键生成器
sharding.jdbc.config.sharding.tables.<logic-table-name>.key-generator-class-name= #自增列值生成器类名称，缺省表示使用默认自增列值生成器。该类需提供无参数的构造器

sharding.jdbc.config.sharding.tables.<logic-table-name>.logic-index= #逻辑索引名称，对于分表的Oracle/PostgreSQL数据库中DROP INDEX XXX语句，需要通过配置逻辑索引名称定位所执行SQL的真实分表

sharding.jdbc.config.sharding.binding-tables[0]= #绑定表规则列表
sharding.jdbc.config.sharding.binding-tables[1]= #绑定表规则列表
sharding.jdbc.config.sharding.binding-tables[x]= #绑定表规则列表

sharding.jdbc.config.sharding.default-data-source-name= #未配置分片规则的表将通过默认数据源定位
sharding.jdbc.config.sharding.default-database-strategy.xxx= #默认数据库分片策略，同分库策略
sharding.jdbc.config.sharding.default-table-strategy.xxx= #默认表分片策略，同分表策略
sharding.jdbc.config.sharding.default-key-generator-class-name= #默认自增列值生成器类名称，缺省使用io.shardingsphere.core.keygen.DefaultKeyGenerator。该类需实现KeyGenerator接口并提供无参数的构造器

sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.master-data-source-name= #详见读写分离部分
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[0]= #详见读写分离部分
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[1]= #详见读写分离部分
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[x]= #详见读写分离部分
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.load-balance-algorithm-class-name= #详见读写分离部分
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.load-balance-algorithm-type= #详见读写分离部分
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.config.map.key1= #详见读写分离部分
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.config.map.key2= #详见读写分离部分
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.config.map.keyx= #详见读写分离部分

sharding.jdbc.config.sharding.props.sql.show= #是否开启SQL显示，默认值: false
sharding.jdbc.config.sharding.props.executor.size= #工作线程数量，默认值: CPU核数

sharding.jdbc.config.sharding.config.map.key1= #用户自定义配置
sharding.jdbc.config.sharding.config.map.key2= #用户自定义配置
sharding.jdbc.config.sharding.config.map.keyx= #用户自定义配置
```

### 读写分离

```properties
#省略数据源配置，与数据分片一致

sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.master-data-source-name= #主库数据源名称
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[0]= #从库数据源名称列表
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[1]= #从库数据源名称列表
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[x]= #从库数据源名称列表
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.load-balance-algorithm-class-name= #从库负载均衡算法类名称。该类需实现MasterSlaveLoadBalanceAlgorithm接口且提供无参数构造器
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.load-balance-algorithm-type= #从库负载均衡算法类型，可选值：ROUND_ROBIN，RANDOM。若`load-balance-algorithm-class-name`存在则忽略该配置

sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.config.map.key1= #用户自定义配置
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.config.map.key2= #用户自定义配置
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.config.map.keyx= #用户自定义配置
```

### 使用Zookeeper的数据治理

```properties
#省略数据源、数据分片和读写分离配置

sharding.jdbc.config.sharding.orchestration.name= #数据治理实例名称
sharding.jdbc.config.sharding.orchestration.overwrite= #本地配置是否覆盖注册中心配置。如果可覆盖，每次启动都以本地配置为准
sharding.jdbc.config.sharding.orchestration.type= #数据源类型，可选值：sharding，masterslave
sharding.jdbc.config.sharding.orchestration.zookeeper.server-lists= #连接Zookeeper服务器的列表。包括IP地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181
sharding.jdbc.config.sharding.orchestration.zookeeper.namespace= #Zookeeper的命名空间
sharding.jdbc.config.sharding.orchestration.zookeeper.base-sleep-time-milliseconds= #等待重试的间隔时间的初始毫秒数，默认1000毫秒
sharding.jdbc.config.sharding.orchestration.zookeeper.max-sleep-time-milliseconds= #等待重试的间隔时间的最大毫秒数，默认3000毫秒
sharding.jdbc.config.sharding.orchestration.zookeeper.max-retries= #连接失败后的最大重试次数，默认3次
sharding.jdbc.config.sharding.orchestration.zookeeper.session-timeout-milliseconds= #会话超时毫秒数，默认60000毫秒
sharding.jdbc.config.sharding.orchestration.zookeeper.connection-timeout-milliseconds= #连接超时毫秒数，默认15000毫秒
sharding.jdbc.config.sharding.orchestration.zookeeper.digest= #连接Zookeeper的权限令牌。缺省为不需要权限验证
```

### 使用Etcd的数据治理

```properties
#省略数据源、数据分片和读写分离配置

sharding.jdbc.config.sharding.orchestration.name= #同Zookeeper
sharding.jdbc.config.sharding.orchestration.overwrite= #同Zookeeper
sharding.jdbc.config.sharding.orchestration.type= #同Zookeeper
sharding.jdbc.config.sharding.orchestration.etcd.server-lists= #连接Etcd服务器的列表。包括IP地址和端口号。多个地址用逗号分隔。如: http://host1:2379,http://host2:2379
sharding.jdbc.config.sharding.orchestration.etcd.time-to-live-seconds= #临时节点存活秒数，默认60秒
sharding.jdbc.config.sharding.orchestration.etcd.timeout-milliseconds= #请求超时毫秒数，默认500毫秒
sharding.jdbc.config.sharding.orchestration.etcd.retry-interval-milliseconds= #重试间隔毫秒数，默认200毫秒
sharding.jdbc.config.sharding.orchestration.etcd.max-retries= #请求失败后的最大重试次数，默认3次
```
