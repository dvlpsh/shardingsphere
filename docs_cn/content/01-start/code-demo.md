+++
toc = true
title = "使用示例"
weight = 2
prev = "/01-start/quick-start"
next = "/01-start/faq"

+++

Sharding-JDBC使用示例的github地址：https://github.com/shardingjdbc/sharding-jdbc-example

# 注意事项

1. 由于涉及到真实数据库环境，需要在准备测试的数据库上运行resources/manual_shcema.sql创建数据库，示例中使用的是MySQL环境，如需使用PostgreSQL、SQLServer或Oracle，请自行创建数据库脚本。

1. 使用示例均通过DDL语句自动创建数据表，无需手动创建。

1. 使用示例中关于数据库URL、驱动、用户名、密码的代码、yaml及Spring配置，需要用户自行修改。

1. 读写分离示例代码中的主库和从库需要用户自行在数据库层面配置主从关系，否则落到从库的读请求查询出来的数据会是空值。

# 基于Java代码的原生JDBC使用示例

## sharding-jdbc-raw-jdbc-java-example

### 读写分离：

```java
io.shardingjdbc.example.jdbc.java.RawJdbcJavaMasterSlaveOnlyMain 
```

### 分库分表：

```java
io.shardingjdbc.example.jdbc.java.RawJdbcJavaShardingDatabaseAndTableMain
```

### 仅分库：

```java
io.shardingjdbc.example.jdbc.java.RawJdbcJavaShardingDatabaseOnlyMain
```

### 仅分表：

```java
io.shardingjdbc.example.jdbc.java.RawJdbcJavaShardingTableOnlyMain
```

### 分库分表+读写分离：

```java
io.shardingjdbc.example.jdbc.java.RawJdbcJavaShardingAndMasterSlaveMain
```

# 基于Yaml的原生JDBC使用示例

## sharding-jdbc-raw-jdbc-yaml-example

### 读写分离：

```java
io.shardingjdbc.example.jdbc.yaml.RawJdbcYamlMasterSlaveOnlyMain 
```

### 分库分表：

```java
io.shardingjdbc.example.jdbc.yaml.RawJdbcYamlShardingDatabaseAndTableMain
```

### 仅分库：

```java
io.shardingjdbc.example.jdbc.yaml.RawJdbcYamlShardingDatabaseOnlyMain
```

### 仅分表：

```java
io.shardingjdbc.example.jdbc.yaml.RawJdbcYamlShardingTableOnlyMain
```

### 分库分表+读写分离：

```java
io.shardingjdbc.example.jdbc.yaml.RawJdbcYamlShardingAndMasterSlaveMain
```

# 基于JPA的Spring使用示例

## sharding-jdbc-spring-namespace-jpa-example

### 读写分离：

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringJpaMasterSlaveOnlyMain 
```

### 分库分表：

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringJpaShardingDatabaseAndTableMain
```

### 仅分库：

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringJpaShardingDatabaseOnlyMain
```

### 仅分表：

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringJpaShardingTableMain
```

### 分库分表+读写分离：

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringJpaShardingDatabaseAndMasterSlaveMain
```

# 基于Mybatis的Spring使用示例

## sharding-jdbc-spring-namespace-mybatis-example

### 读写分离：

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringMybatisMasterSlaveOnlyMain 
```

### 分库分表：

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringMybatisShardingDatabaseAndTableMain
```

### 仅分库：

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringMybatisShardingDatabaseOnlyMain
```

### 仅分表：

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringMybatisShardingTableMain
```

### 分库分表+读写分离：

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringMybatisShardingDatabaseAndMasterSlaveMain
```

# 基于Spring Data JPA的Spring Boot使用示例

## sharding-jdbc-spring-boot-data-jpa-example

### 启动入口类

```java
io.shardingjdbc.example.spring.boot.starter.jpa.SpringBootDataJpaMain
```

### 配置说明
通过修改resources/applicaiton.properties文件中的spring.profiles.active来切换示例配置

```xml
spring.profiles.active=sharding
#spring.profiles.active=sharding-db
#spring.profiles.active=sharding-tbl
#spring.profiles.active=masterslave
#spring.profiles.active=sharding-masterslave
```

# 基于Spring Data Mybatis的Spring Boot使用示例

## sharding-jdbc-spring-namespace-mybatis-example

### 启动入口类

```java
io.shardingjdbc.example.spring.boot.jpa.SpringBootDataMybatisMain
```

### 配置说明
通过修改resources/applicaiton.properties文件中的spring.profiles.active来切换示例配置

```xml
spring.profiles.active=sharding
#spring.profiles.active=sharding-db
#spring.profiles.active=sharding-tbl
#spring.profiles.active=masterslave
#spring.profiles.active=sharding-masterslave
```

# 数据库服务编排治理使用示例

准备Zookeeper环境，代码示例中使用的地址为localhost:2181

## sharding-jdbc-orchestration-java-example 

1. 运行

```java
io.shardingjdbc.example.orchestration.OrchestrationShardingMain
```

## sharding-jdbc-orchestration-yaml-example 

1. 运行

```java
io.shardingjdbc.example.orchestration.yaml.OrchestrationYamlShardingMain
```

## sharding-jdbc-orchestration-spring-namespace-example 

1. 运行

```java
io.shardingjdbc.example.orchestration.spring.namespace.OrchestrationSpringMybatisShardingShardingMain
```

## sharding-jdbc-orchestration-spring-boot-example 

1. 运行

```java
io.shardingjdbc.example.orchestration.spring.boot.OrchestrationSpringBootDataJpaMain
```

# 柔性事务使用示例

## sharding-jdbc-transaction-example 

```java
io.shardingjdbc.example.transaction.TransactionMain
```
