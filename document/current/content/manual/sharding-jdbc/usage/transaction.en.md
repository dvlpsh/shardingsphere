+++
toc = true
title = "Distribute Transaction"
weight = 5
+++

## Introduce Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-transaction-xa-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

XA transaction manager will be uploaded by Sharding-JDBC in SPI form.

## Transaction Type Switch

ShardingSphere stores transaction types in local thread variable `TransactionTypeHolder`. 
Modifying its value before the database creates connections can have the effect of switching transaction types freely.

Notice: after the database connection is created, transactions are not able to be modified.

### API Usage

```java
TransactionTypeHolder.set(TransactionType.LOCAL);
```

Or

```java
TransactionTypeHolder.set(TransactionType.XA);
```

### SpringBootStarter Usage

Introduce Maven Dependency:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-transaction-spring-boot-starter</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>
```

### SpringBoot Usage

Introduce Maven Dependency:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-transaction-spring</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
    <version>${spring-boot.version}</version>
</dependency>

<spring-boot.version>[1.5.0.RELEASE,2.0.0.M1)</spring-boot.version>
```

AutoConfiguration:

```java
@SpringBootApplication(exclude = JtaAutoConfiguration.class)
@ComponentScan("org.apache.shardingsphere.transaction.aspect")
public class StartMain {
}
```

### Spring Namespace Usage

Introduce Maven Dependency:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-transaction-spring</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>

<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>${aspectjweaver.version}</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context-support</artifactId>
    <version>${springframework.version}</version>
</dependency>

<aspectjweaver.version>1.8.9</aspectjweaver.version>
<springframework.version>[4.3.6.RELEASE,5.0.0.M1)</springframework.version>
```

Load section configuration information:

```xml
<import resource="classpath:META-INF/shardingTransaction.xml"/>
```

### Business Code

Add relevant notes in methods or types that need transactions. For example:

```java
@ShardingTransactionType(TransactionType.LOCAL)
@Transactional
```

Or

```java
@ShardingTransactionType(TransactionType.XA)
@Transactional
```

Notice: `@ShardingTransactionType`needs to be used along with Spring `@Transactional` to make transactions take effect.

## Atomikos Parameter Configuration

Default XA transaction manager of ShardingSphere is Atomikos. 
Add `jta.properties` in `classpath` of the program to customize Atomikos configuration items. 
For detailed configuration rules, please refer to [official documentation](https://www.atomikos.com/Documentation/JtaProperties) of Atomikos.
