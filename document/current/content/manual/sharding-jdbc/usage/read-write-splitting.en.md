+++
toc = true
title = "Read-write Splitting"
weight = 2
+++

## Without spring

### Add maven dependency

```xml
<dependency>
    <groupId>io.shardingsphere</groupId>
    <artifactId>sharding-jdbc-core</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>
```

### Configure read-write splitting rule with java

```java
    // Configure actual data sources
    Map<String, DataSource> dataSourceMap = new HashMap<>();
    
    // Configure master data source
    BasicDataSource masterDataSource = new BasicDataSource();
    masterDataSource.setDriverClassName("com.mysql.jdbc.Driver");
    masterDataSource.setUrl("jdbc:mysql://localhost:3306/ds_master");
    masterDataSource.setUsername("root");
    masterDataSource.setPassword("");
    dataSourceMap.put("ds_master", masterDataSource);
    
    // Configure first slave data source
    BasicDataSource slaveDataSource1 = new BasicDataSource();
    slaveDataSource1.setDriverClassName("com.mysql.jdbc.Driver");
    slaveDataSource1.setUrl("jdbc:mysql://localhost:3306/ds_slave0");
    slaveDataSource1.setUsername("root");
    slaveDataSource1.setPassword("");
    dataSourceMap.put("ds_slave0", slaveDataSource1);
    
    // Configure second slave data source
    BasicDataSource slaveDataSource2 = new BasicDataSource();
    slaveDataSource2.setDriverClassName("com.mysql.jdbc.Driver");
    slaveDataSource2.setUrl("jdbc:mysql://localhost:3306/ds_slave1");
    slaveDataSource2.setUsername("root");
    slaveDataSource2.setPassword("");
    dataSourceMap.put("ds_slave1", slaveDataSource2);
    
    // Configure read-write splitting rule
    MasterSlaveRuleConfiguration masterSlaveRuleConfig = new MasterSlaveRuleConfiguration("ds_master_slave", "ds_master", Arrays.asList("ds_slave0", "ds_slave1"));
    
    // Get data source
    DataSource dataSource = MasterSlaveDataSourceFactory.createDataSource(createDataSourceMap(), masterSlaveRuleConfig, new HashMap<String, Object>());
```

### Configure read-write splitting rule with yaml

To configure by yaml, similar with the configuration method of java codes:

```yaml
dataSources:
  ds_master: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_master
    username: root
    password: 
  ds_slave0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_slave0
    username: root
    password:
  ds_slave1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_slave1
    username: root
    password: 

masterSlaveRule:
  name: ds_ms
  masterDataSourceName: ds_master
  slaveDataSourceNames: [ds_slave0, ds_slave1]
  
  props:
    sql.show: true
  configMap:
    key1: value1
```

```java
    DataSource dataSource = MasterSlaveDataSourceFactory.createDataSource(yamlFile);
```

### Use raw JDBC

By using MasterSlaveDataSourceFactory factory class and rule configuration object, we can obtain MasterSlaveDataSource which implements the standard interface of DataSource in JDBC. Thus you can choose to use native JDBC DataSource for development, or using JPA, MyBatis ORM tools, etc.
Take DataSource in JDBC as an example:

```java
DataSource dataSource = MasterSlaveDataSourceFactory.createDataSource(yamlFile);
String sql = "SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id=? AND o.order_id=?";
try (
        Connection conn = dataSource.getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
    preparedStatement.setInt(1, 10);
    preparedStatement.setInt(2, 1001);
    try (ResultSet rs = preparedStatement.executeQuery()) {
        while(rs.next()) {
            System.out.println(rs.getInt(1));
            System.out.println(rs.getInt(2));
        }
    }
}
```

## Using spring

### Add maven dependency

```xml
<!-- for spring boot -->
<dependency>
    <groupId>io.shardingsphere</groupId>
    <artifactId>sharding-jdbc-spring-boot-starter</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>

<!-- for spring namespace -->
<dependency>
    <groupId>io.shardingsphere</groupId>
    <artifactId>sharding-jdbc-spring-namespace</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>
```

### Configure read-write splitting rule with spring boot

```properties
sharding.jdbc.datasource.names=master,slave0,slave1

sharding.jdbc.datasource.ds-master.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds-master.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds-master.url=jdbc:mysql://localhost:3306/master
sharding.jdbc.datasource.ds-master.username=root
sharding.jdbc.datasource.ds-master.password=

sharding.jdbc.datasource.ds-slave0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds-slave0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds-slave0.url=jdbc:mysql://localhost:3306/slave0
sharding.jdbc.datasource.ds-slave0.username=root
sharding.jdbc.datasource.ds-slave0.password=

sharding.jdbc.datasource.ds-slave1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds-slave1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds-slave1.url=jdbc:mysql://localhost:3306/slave1
sharding.jdbc.datasource.ds-slave1.username=root
sharding.jdbc.datasource.ds-slave1.password=

sharding.jdbc.config.masterslave.name=ms
sharding.jdbc.config.masterslave.master-data-source-name=master
sharding.jdbc.config.masterslave.slave-data-source-names=slave0,slave1

sharding.jdbc.config.masterslave.props.sql.show=true
```

### Configure read-write splitting rule with spring namespace

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:master-slave="http://shardingsphere.io/schema/shardingsphere/masterslave" 
    xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://shardingsphere.io/schema/shardingsphere/masterslave 
                        http://shardingsphere.io/schema/shardingsphere/sharding/master-slave.xsd 
                        ">
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
    
    <master-slave:data-source id="masterSlaveDataSource" master-data-source-name="ds_master" slave-data-source-names="ds_slave0, ds_slave1" >
            <master-slave:props>
                    <prop key="sql.show">${sql_show}</prop>
                    <prop key="executor.size">10</prop>
                    <prop key="foo">bar</prop>
                </master-slave:props>
    </master-slave:data-source>
</beans>
```

### Use DataSource on spring

Just inject or configure data source to JPA, Hibernate orMyBatis.

```java
@Resource
private DataSource dataSource;
```

More details please reference [configuration manual](/en/manual/sharding-jdbc/configuration/).
