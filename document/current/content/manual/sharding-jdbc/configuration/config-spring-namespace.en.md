+++
toc = true
title = "Spring Namespace"
weight = 4
+++

## Attention

Inline expression identifier can use `${...}` or `$->{...}`, but `${...}` is conflict with spring placeholder of properties, so use `$->{...}` on spring environment is better.

## Example

### Sharding

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:sharding="http://shardingsphere.io/schema/shardingsphere/sharding"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://shardingsphere.io/schema/shardingsphere/sharding 
                        http://shardingsphere.io/schema/shardingsphere/sharding/sharding.xsd
                        http://www.springframework.org/schema/context
                        http://www.springframework.org/schema/context/spring-context.xsd
                        http://www.springframework.org/schema/tx
                        http://www.springframework.org/schema/tx/spring-tx.xsd">
    <context:annotation-config />
    <context:component-scan base-package="io.shardingsphere.example.spring.namespace.jpa" />
    
    <bean id="entityManagerFactory" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
        <property name="dataSource" ref="shardingDataSource" />
        <property name="jpaVendorAdapter">
            <bean class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter" p:database="MYSQL" />
        </property>
        <property name="packagesToScan" value="io.shardingsphere.example.spring.namespace.jpa.entity" />
        <property name="jpaProperties">
            <props>
                <prop key="hibernate.dialect">org.hibernate.dialect.MySQLDialect</prop>
                <prop key="hibernate.hbm2ddl.auto">create</prop>
                <prop key="hibernate.show_sql">true</prop>
            </props>
        </property>
    </bean>
    <bean id="transactionManager" class="org.springframework.orm.jpa.JpaTransactionManager" p:entityManagerFactory-ref="entityManagerFactory" />
    <tx:annotation-driven />
    
    <bean id="ds0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="ds1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="preciseModuloDatabaseShardingAlgorithm" class="io.shardingsphere.example.spring.namespace.jpa.algorithm.PreciseModuloDatabaseShardingAlgorithm" />
    <bean id="preciseModuloTableShardingAlgorithm" class="io.shardingsphere.example.spring.namespace.jpa.algorithm.PreciseModuloTableShardingAlgorithm" />
    
    <sharding:standard-strategy id="databaseShardingStrategy" sharding-column="user_id" precise-algorithm-ref="preciseModuloDatabaseShardingAlgorithm" />
    <sharding:standard-strategy id="tableShardingStrategy" sharding-column="order_id" precise-algorithm-ref="preciseModuloTableShardingAlgorithm" />
    
    <sharding:data-source id="shardingDataSource">
        <sharding:sharding-rule data-source-names="ds0,ds1">
            <sharding:table-rules>
                <sharding:table-rule logic-table="t_order" actual-data-nodes="ds$->{0..1}.t_order$->{0..1}" database-strategy-ref="databaseShardingStrategy" table-strategy-ref="tableShardingStrategy" generate-key-column-name="order_id" />
                <sharding:table-rule logic-table="t_order_item" actual-data-nodes="ds$->{0..1}.t_order_item$->{0..1}" database-strategy-ref="databaseShardingStrategy" table-strategy-ref="tableShardingStrategy" generate-key-column-name="order_item_id" />
            </sharding:table-rules>
        </sharding:sharding-rule>
    </sharding:data-source>
</beans>
```

### Read-write splitting

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:master-slave="http://shardingsphere.io/schema/shardingsphere/masterslave"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd 
                        http://www.springframework.org/schema/context 
                        http://www.springframework.org/schema/context/spring-context.xsd
                        http://www.springframework.org/schema/tx 
                        http://www.springframework.org/schema/tx/spring-tx.xsd
                        http://shardingsphere.io/schema/shardingsphere/masterslave  
                        http://shardingsphere.io/schema/shardingsphere/masterslave/master-slave.xsd">
    <context:annotation-config />
    <context:component-scan base-package="io.shardingsphere.example.spring.namespace.jpa" />
    
    <bean id="entityManagerFactory" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
        <property name="dataSource" ref="masterSlaveDataSource" />
        <property name="jpaVendorAdapter">
            <bean class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter" p:database="MYSQL" />
        </property>
        <property name="packagesToScan" value="io.shardingsphere.example.spring.namespace.jpa.entity" />
        <property name="jpaProperties">
            <props>
                <prop key="hibernate.dialect">org.hibernate.dialect.MySQLDialect</prop>
                <prop key="hibernate.hbm2ddl.auto">create</prop>
                <prop key="hibernate.show_sql">true</prop>
            </props>
        </property>
    </bean>
    <bean id="transactionManager" class="org.springframework.orm.jpa.JpaTransactionManager" p:entityManagerFactory-ref="entityManagerFactory" />
    <tx:annotation-driven />
    
    <bean id="ds_master" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_master" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="ds_slave0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_slave0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="ds_slave1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_slave1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="randomStrategy" class="io.shardingsphere.core.api.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithm" />
    <master-slave:data-source id="masterSlaveDataSource" master-data-source-name="ds_master" slave-data-source-names="ds_slave0, ds_slave1" strategy-ref="randomStrategy">
            <master-slave:props>
                <prop key="sql.show">${sql_show}</prop>
                <prop key="executor.size">10</prop>
                <prop key="foo">bar</prop>
            </master-slave:props>
    </master-slave:data-source>
    
</beans>
```

### Sharding + Read-write splitting

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:sharding="http://shardingsphere.io/schema/shardingsphere/sharding"
       xmlns:master-slave="http://shardingsphere.io/schema/shardingsphere/masterslave"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://www.springframework.org/schema/context
                        http://www.springframework.org/schema/context/spring-context.xsd
                        http://www.springframework.org/schema/tx
                        http://www.springframework.org/schema/tx/spring-tx.xsd
                        http://shardingsphere.io/schema/shardingsphere/sharding 
                        http://shardingsphere.io/schema/shardingsphere/sharding/sharding.xsd
                        http://shardingsphere.io/schema/shardingsphere/masterslave
                        http://shardingsphere.io/schema/shardingsphere/masterslave/master-slave.xsd">
    <context:annotation-config />
    <context:component-scan base-package="io.shardingsphere.example.spring.namespace.jpa" />
    
    <bean id="entityManagerFactory" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
        <property name="dataSource" ref="shardingDataSource" />
        <property name="jpaVendorAdapter">
            <bean class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter" p:database="MYSQL" />
        </property>
        <property name="packagesToScan" value="io.shardingsphere.example.spring.namespace.jpa.entity" />
        <property name="jpaProperties">
            <props>
                <prop key="hibernate.dialect">org.hibernate.dialect.MySQLDialect</prop>
                <prop key="hibernate.hbm2ddl.auto">create</prop>
                <prop key="hibernate.show_sql">true</prop>
            </props>
        </property>
    </bean>
    <bean id="transactionManager" class="org.springframework.orm.jpa.JpaTransactionManager" p:entityManagerFactory-ref="entityManagerFactory" />
    <tx:annotation-driven />
    
    <bean id="ds_master0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_master0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>

    <bean id="ds_master0_slave0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_master0_slave0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>

    <bean id="ds_master0_slave1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_master0_slave1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>

    <bean id="ds_master1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_master1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>

    <bean id="ds_master1_slave0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_master1_slave0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>

    <bean id="ds_master1_slave1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_master1_slave1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>

    <bean id="randomStrategy" class="io.shardingsphere.core.api.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithm" />

    <master-slave:data-source id="ds_ms0" master-data-source-name="ds_master0" slave-data-source-names="ds_master0_slave0, ds_master0_slave1" strategy-ref="randomStrategy" />
    <master-slave:data-source id="ds_ms1" master-data-source-name="ds_master1" slave-data-source-names="ds_master1_slave0, ds_master1_slave1" strategy-ref="randomStrategy" />

    <sharding:inline-strategy id="databaseStrategy" sharding-column="user_id" algorithm-expression="ds_ms$->{user_id % 2}" />
    <sharding:inline-strategy id="orderTableStrategy" sharding-column="order_id" algorithm-expression="t_order$->{order_id % 2}" />
    <sharding:inline-strategy id="orderItemTableStrategy" sharding-column="order_id" algorithm-expression="t_order_item$->{order_id % 2}" />

    <sharding:data-source id="shardingDataSource">
        <sharding:sharding-rule data-source-names="ds_ms0,ds_ms1">
            <sharding:table-rules>
                <sharding:table-rule logic-table="t_order" actual-data-nodes="ds_ms$->{0..1}.t_order$->{0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderTableStrategy" generate-key-column-name="order_id" />
                <sharding:table-rule logic-table="t_order_item" actual-data-nodes="ds_ms$->{0..1}.t_order_item$->{0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderItemTableStrategy" generate-key-column-name="order_item_id" />
            </sharding:table-rules>
        </sharding:sharding-rule>
    </sharding:data-source>
</beans>
```

### Orchestration by Zookeeper

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
       xmlns:sharding="http://shardingsphere.io/schema/shardingsphere/orchestration/sharding"
       xmlns:master-slave="http://shardingsphere.io/schema/shardingsphere/orchestration/masterslave"
       xmlns:reg="http://shardingsphere.io/schema/shardingsphere/orchestration/reg"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingsphere.io/schema/shardingsphere/orchestration/reg 
                           http://shardingsphere.io/schema/shardingsphere/orchestration/reg/reg.xsd
                           http://shardingsphere.io/schema/shardingsphere/orchestration/sharding 
                           http://shardingsphere.io/schema/shardingsphere/orchestration/sharding/sharding.xsd
                           http://shardingsphere.io/schema/shardingsphere/orchestration/masterslave  
                           http://shardingsphere.io/schema/shardingsphere/orchestration/masterslave/master-slave.xsd">
    
    <reg:zookeeper id="regCenter" server-lists="localhost:2181" namespace="orchestration-spring-namespace-demo" overwtite="false" />
    <sharding:data-source id="shardingMasterSlaveDataSource" registry-center-ref="regCenter" />
    <master-slave:data-source id="masterSlaveDataSource" registry-center-ref="regCenter" />
</beans>
```

### Orchestration by Etcd

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
       xmlns:sharding="http://shardingsphere.io/schema/shardingsphere/orchestration/sharding"
       xmlns:master-slave="http://shardingsphere.io/schema/shardingsphere/orchestration/masterslave"
       xmlns:reg="http://shardingsphere.io/schema/shardingsphere/orchestration/reg"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingsphere.io/schema/shardingsphere/orchestration/reg 
                           http://shardingsphere.io/schema/shardingsphere/orchestration/reg/reg.xsd
                           http://shardingsphere.io/schema/shardingsphere/orchestration/sharding 
                           http://shardingsphere.io/schema/shardingsphere/orchestration/sharding/sharding.xsd
                           http://shardingsphere.io/schema/shardingsphere/orchestration/masterslave  
                           http://shardingsphere.io/schema/shardingsphere/orchestration/masterslave/master-slave.xsd">
    
    <reg:etcd id="regCenter" server-lists="http://localhost:2379" />
    <sharding:data-source id="shardingMasterSlaveDataSource" registry-center-ref="regCenter" />
    <master-slave:data-source id="masterSlaveDataSource" registry-center-ref="regCenter" />
</beans>
```

## Configuration reference

### Sharding

Namespace: http://shardingsphere.io/schema/shardingsphere/sharding/sharding.xsd

#### \<sharding:data-source />

| *Name*         | *Type*    | *Description*               |
| -------------- | --------- | --------------------------- |
| id             | Attribute | Spring Bean Id              |
| sharding-rule  | Tag       | Sharding rule configuration |
| config-map (?) | Tag       | User-defined arguments      |
| props (?)      | Tag       | Properties                  |

#### \<sharding:sharding-rule />

| *Name*                            | *Type*    | *Description*                                                                                                                         |
| --------------------------------- | --------- | ------------------------------------------------------------------------------------------------------------------------------------- |
| data-source-names                 | Attribute | Data source bean list. Multiple data sources names separated with comma                                                               | 
| table-rules                       | Tag       | Table rule configurations                                                                                                             |
| binding-table-rules (?)           | Tag       | Binding table rule configurations                                                                                                     |
| default-data-source-name (?)      | Attribute | If table not configure at table rule, will route to defaultDataSourceName                                                             |
| default-database-strategy-ref (?) | Attribute | Default database sharding strategy, reference id of \<sharding:xxx-strategy>, Default for not sharding                                |
| default-table-strategy-ref (?)    | Attribute | Default table sharding strategy, reference id of \<sharding:xxx-strategy>, Default for not sharding                                   |
| default-key-generator (?)         | Attribute | Default key generator, default value is `io.shardingsphere.core.keygen.DefaultKeyGenerator`. This class need to implements KeyGenerator |

#### \<sharding:table-rules />

| *Name*         | *Type* | *Description*            |
| -------------- | ------ | ------------------------ |
| table-rule (+) | Tag    | Table rule configuration |

#### \<sharding:table-rule />

| *Name*                       | *Type*    | *Description*                                                                                                                                                                                               |
| ---------------------------- | --------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| logic-table                  | Attribute | Name of logic table                                                                                                                                                                                         |
| actual-data-nodes (?)        | Attribute | Describe data source names and actual tables, delimiter as point, multiple data nodes separated with comma, support inline expression. Absent means sharding databases only. Example: ds${0..7}.tbl${0..7} |
| database-strategy-ref (?)    | Attribute | Databases sharding strategy, use default databases sharding strategy if absent                                                                                                                              |
| table-strategy-ref (?)       | Attribute | Tables sharding strategy, use default tables sharding strategy if absent                                                                                                                                    |
| generate-key-column-name (?) | Attribute | Column name of key generator, do not use Key generator if absent                                                                                                                                            |
| key-generator (?)            | Attribute | Key generator, use default key generator if absent. This class need to implements KeyGenerator                                                                                                              |
| logic-index (?)              | Attribute | Name if logic index. If use `DROP INDEX XXX` SQL in Oracle/PostgreSQL, This property needs to be set for finding the actual tables                                                                          |

#### \<sharding:binding-table-rules />

| *Name*                 | *Type* | *Description*                    |
| ---------------------- | ------ | -------------------------------- |
| binding-table-rule (+) | Tag    | Binding table rule configuration |

#### \<sharding:binding-table-rule />

| *Name*       | *Type*    | *Description*                                            |
| ------------ | --------- | -------------------------------------------------------- |
| logic-tables | Attribute | Name of logic table. Multiple names separated with comma |

#### \<sharding:standard-strategy />

| *Name*                  | *Type*    | *Description*                                                                                              |
| ----------------------- | --------- | ---------------------------------------------------------------------------------------------------------- |
| id                      | Attribute | Spring Bean Id                                                                                             |
| sharding-column         | Attribute | Name of sharding column                                                                                    |
| precise-algorithm-ref   | Attribute | Reference of precise algorithm used for `=` and `IN`. This class need to implements PreciseShardingAlgorithm |
| range-algorithm-ref (?) | Attribute | Reference of range algorithm used for `BETWEEN`. This class need to implements RangeShardingAlgorithm      |

#### \<sharding:complex-strategy />

| *Name*           | *Type*    | *Description*                                                                                       |
| ---------------- | --------- | --------------------------------------------------------------------------------------------------- |
| id               | Attribute | Spring Bean Id                                                                                      |
| sharding-columns | Attribute | Names of sharding columns. Multiple columns separated with comma                                    |
| algorithm-ref    | Attribute | Reference of complex sharding algorithm. This class need to implements ComplexKeysShardingAlgorithm |

#### \<sharding:inline-strategy />

| *Name*               | *Type*    | *Description*                            |
| -------------------- | --------- | ---------------------------------------- |
| id                   | Attribute | Spring Bean Id                           |
| sharding-column      | Attribute | Name of sharding column                  |
| algorithm-expression | Attribute | Inline expression for sharding algorithm |

#### \<sharding:hint-database-strategy />

| *Name*        | *Type*    | *Description*                                                                             |
| ------------- | --------- | ----------------------------------------------------------------------------------------- |
| id            | Attribute | Spring Bean Id                                                                            |
| algorithm-ref | Attribute | Reference of hint sharding algorithm. This class need to implements HintShardingAlgorithm |

#### \<sharding:none-strategy />

| *Name* | *Type*    | *Description*  |
| ------ | --------- | -------------- |
| id     | Attribute | Spring Bean Id |

#### \<sharding:props />

| *Name*            | *Type*    | *Description*                                           |
| ----------------- | --------- | ------------------------------------------------------- |
| sql.show (?)      | Attribute | To show SQLS or not, default value: false               |
| executor.size (?) | Attribute | The number of working threads, default value: CPU count |

#### \<sharding:config-map />

### Read-write splitting

Namespace: http://shardingsphere.io/schema/shardingsphere/masterslave/master-slave.xsd

#### \<master-slave:data-source />

| *Name*                  | *Type*    | *Description*                                                                                                 |
| ----------------------- | --------- | ------------------------------------------------------------------------------------------------------------- |
| id                      | Attribute | Spring Bean Id                                                                                                |
| master-data-source-name | Attribute | Reference of master data source                                                                               |
| slave-data-source-names | Attribute | Reference of slave data sources. Multiple columns separated with comma                                        |
| strategy-ref (?)        | Attribute | Reference of load balance algorithm. This class need to implements MasterSlaveLoadBalanceAlgorithm            |
| strategy-type (?)       | Attribute | Load balance algorithm type, values should be: `ROUND_ROBIN` or `RANDOM`. Ignore if `strategy-ref` is present |
| config-map (?)          | Attribute | User-defined arguments                                                                                        |
| props (?)               | Tag       | Properties                                                                                                    |

#### \<master-slave:config-map />

#### \<master-slave:props />

| *Name*            | *Type*    | *Description*                                           |
| ----------------- | --------- | ------------------------------------------------------- |
| sql.show (?)      | Attribute | To show SQLS or not, default value: false               |
| executor.size (?) | Attribute | The number of working threads, default value: CPU count |

### Sharding + orchestration

Namespace: http://shardingsphere.io/schema/shardingsphere/orchestration/sharding/sharding.xsd

#### \<sharding:data-source />

| *Name*              | *Type*    | *Description*                                               |
| ------------------- | --------- | ----------------------------------------------------------- |
| id                  | Attribute | Same as sharding                                            |
| sharding-rule       | Tag       | Same as sharding                                            |
| props (?)           | Tag       | Same as sharding                                            |
| config-map (?)      | Tag       | Same as sharding                                            |
| registry-center-ref | Attribute | Reference of orchestration registry center                  |
| overwrite           | Attribute | Use local configuration to overwrite registry center or not |

### Read-write splitting + orchestration

Namespace: http://shardingsphere.io/schema/shardingsphere/orchestration/masterslave/master-slave.xsd

#### \<master-slave:data-source />

| *Name*                  | *Type*    | *Description*                    |
| ----------------------- | --------- | -------------------------------- |
| id                      | Attribute | Same as read-write splitting     |
| master-data-source-name | Attribute | Same as read-write splitting     |
| slave-data-source-names | Attribute | Same as read-write splitting     |
| strategy-ref (?)        | Attribute | Same as read-write splitting     |
| strategy-type (?)       | Attribute | Same as read-write splitting     |
| config-map (?)          | Tag       | Same as read-write splitting     |
| registry-center-ref     | Attribute | Same as sharding + orchestration |
| overwrite               | Attribute | Same as sharding + orchestration |

### Orchestration registry center

Namespace: http://shardingsphere.io/schema/shardingsphere/orchestration/reg/reg.xsd

#### \<reg:zookeeper />

| *Name*                              | *Type*    | *Description*                                                                   |
| ----------------------------------- | --------- | ------------------------------------------------------------------------------- |
| id                                  | Attribute | Spring Bean Id of registry center                                               |
| server-lists                        | Attribute | Zookeeper servers list, multiple split as comma. Example: host1:2181,host2:2181 |
| namespace                           | Attribute | Namespace of zookeeper                                                          |
| base-sleep-time-milliseconds (?)    | Attribute | Initial milliseconds of waiting for retry, default value is 1000 milliseconds   |
| max-sleep-time-milliseconds (?)     | Attribute | Maximum milliseconds of waiting for retry, default value is 3000 milliseconds   |
| max-retries (?)                     | Attribute | Max retries times if connect failure, default value is 3                        |
| session-timeout-milliseconds (?)    | Attribute | Session timeout milliseconds, default is 60000 milliseconds                     |
| connection-timeout-milliseconds (?) | Attribute | Connection timeout milliseconds, default is 15000 milliseconds                  |
| digest (?)                          | Attribute | Connection digest                                                               |

#### \<reg:etcd />

| *Name*                          | *Type*    | *Description*                                                                            |
| ------------------------------- | --------- | ---------------------------------------------------------------------------------------- |
| id                              | Attribute | Spring Bean Id of registry center                                                        |
| server-lists                    | Attribute | Etcd servers list, multiple split as comma. Example: http://host1:2379,http://host2:2379 |
| time-to-live-seconds (?)        | Attribute | Time to live of data, default is 60 seconds                                              |
| timeout-milliseconds (?)        | Attribute | Timeout milliseconds, default is 500 milliseconds                                        |
| max-retries (?)                 | Attribute | Milliseconds of retry interval, default is 200 milliseconds                              |
| retry-interval-milliseconds (?) | Attribute | Max retries times if request failure, default value is 3                                 |

### B.A.S.E Transaction

#### SoftTransactionConfiguration Configuration

For configuring transaction manager.

| *Name*                              | *Type*                                    | *Required* | *Default* | *Info*                                                                                                                                       |
| ----------------------------------- | ----------------------------------------- | ---------- | --------- | -------------------------------------------------------------------------------------------------------------------------------------------- |
| shardingDataSource                  | ShardingDataSource                        | Y          |           | The data source of transaction manager                                                                                                       |
| syncMaxDeliveryTryTimes             | int                                       | N          | 3         | The maximum number of attempts to send transactions                                                                                          |
| storageType                         | enum                                      | N          | RDB       | The storage type of transaction logs, The options are RDB(creating tables automatically) or MEMORY                                           |
| transactionLogDataSource            | DataSource                                | N          | null      | The data source to store the transaction log. if storageType is RDB, this item is required                                                   |
| bestEffortsDeliveryJobConfiguration | NestedBestEffortsDeliveryJobConfiguration | N          | null      | The config of embedded asynchronous jobs for the Best-Effort-Delivery transaction, please refer to NestedBestEffortsDeliveryJobConfiguration |

#### NestedBestEffortsDeliveryJobConfiguration Configuration (developing environment only)

It is for configuring embedded asynchronous jobs for development environment only. The production environment should adopt the deployed discrete jobs.

| *Name*                         | *Type* | *Required* | *Default*                 | *Info*                                                                                                                                                                      |
| ------------------------------ | ------ | ---------- | ------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| zookeeperPort                  | int    | N          | 4181                      | The port of the embedded registry                                                                                                                                           |
| zookeeperDataDir               | String | N          | target/test_zk_data/nano/ | The data directory of the embedded registry                                                                                                                                 |
| asyncMaxDeliveryTryTimes       | int    | N          | 3                         | The maximum number of attempts to send transactions asynchronously                                                                                                          |
| asyncMaxDeliveryTryDelayMillis | long   | N          | 60000                     | The number of delayed milliseconds to execute asynchronous transactions. The transactions whose creating time earlier than this value will be executed by asynchronous jobs |
