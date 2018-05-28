+++
pre = "<b>2.1. </b>"
toc = true
title = "Sharding-JDBC"
weight = 1
+++

## 1. 引入maven依赖

```xml
<dependency>
    <groupId>io.shardingsphere</groupId>
    <artifactId>sharding-jdbc</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

## 2. 规则配置

Sharding-JDBC可以通过`Java`，`YAML`，`Spring命名空间`和`Spring Boot Starter`四种方式配置，开发者可根据场景选择适合的配置方式。详情请参见[配置手册](/manual/sharding-jdbc/configuration/)。

## 3. 创建DataSource

通过ShardingDataSourceFactory工厂和规则配置对象获取ShardingDataSource，ShardingDataSource实现自JDBC的标准接口DataSource。然后即可通过DataSource选择使用原生JDBC开发，或者使用JPA, MyBatis等ORM工具。

```java
DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig);
```
