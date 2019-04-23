+++
toc = true
title = "Route Engine"
weight = 2
+++

It is the sharding strategy that matches databases and tables according to the parsing context and generates route path. 
SQL with sharding keys can be divided into single-sharding route (equal mark as the operator of sharding key), multiple-sharding route (IN as the operator of sharding key) and range sharding route (BETWEEN as the operator of sharding key). 
SQL without sharding key adopts broadcast route.

Sharding strategies can usually be set in the database or by users. 
Cases built in the database is relatively simple, and strategies can generally be divided into last number modulo, hash, range, tag, time and so on. 
More flexible, sharding strategies set by users can be customized according to their needs. 
Together with automatic data migration, database middle layer can automatically shard and balance the data without users paying attention to sharding strategies. 
The elastic migration ability of the distributed database can be achieved thereby. 
In the roadmap of ShardingSphere, elastic migration will start from 4.x version.

## Sharding Route

It is used in the situation to route according to the sharding key, and can be sub-divided into 3 types, direct route, standard route and Cartesian product route.

### Direct Route
The conditions for direct route are relatively strict. 
It requires to shard through Hint (use HintAPI to appoint the route to databases and tables directly). 
On the premise of having database sharding but not table sharding, SQL parsing and the following result merging can be avoided. 
Therefore, with the highest compatibility, it can execute any SQL in complex situations including sub-queries, self-defined functions. 
Direct route can also be used in the situation where sharding keys are not in SQL. For example, set sharding key to`3`.

```java
hintManager.setDatabaseShardingValue(3);
```

If the routing algorithm is `value % 2`, when a logical table `t_order` corresponds to two physical tables `t_order_0` and `t_order_1`, the SQL will be executed on `t_order_1` after routing.
Below is a sample code using the API.

```java
String sql = "SELECT * FROM t_order";
try (
        HintManager hintManager = HintManager.getInstance();
        Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
    hintManager.setDatabaseShardingValue(3);
    try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
            //...
        }
    }
}
```

### Standard Route
Standard route is the most recommended sharding method by ShardingSphere, its application range is the SQL that does not include joint query or only includes joint query between binding tables. 
When the sharding operator is the equal mark, the route result will fall into single database (table); when sharding operators are BETWEEN or IN, the route result will not necessarily fall into the only database (table). 
So one logic SQL can finally be split into multiple real SQL to enforce. 
For example, if the data sharding is according to the odd number or even number, a single table query SQL is as the following:

```sql
SELECT * FROM t_order WHERE order_id IN (1, 2);
```

The route result will be:

```sql
SELECT * FROM t_order_0 WHERE order_id IN (1, 2);
SELECT * FROM t_order_1 WHERE order_id IN (1, 2);
```

The complexity and performance of the joint query are comparable with those of single-table query. 
For instance, if a joint query SQL that contains binding tables is as this:

```sql
SELECT * FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id  WHERE order_id IN (1, 2);
```

Then, the route result will be:

```sql
SELECT * FROM t_order_0 o JOIN t_order_item_0 i ON o.order_id=i.order_id  WHERE order_id IN (1, 2);
SELECT * FROM t_order_1 o JOIN t_order_item_1 i ON o.order_id=i.order_id  WHERE order_id IN (1, 2);
```

It can be seen that, the number of divided SQL is the same as the number of single tables.

### Cartesian Route
Cartesian product route has the most complex situation, it cannot locate sharding rules according to the binding table relationship, so the joint query between non-binding tables needs to be split into Cartesian product combination to execute. 
If SQL in the last case is not configured with binding table relationship, the route result will be:

```sql
SELECT * FROM t_order_0 o JOIN t_order_item_0 i ON o.order_id=i.order_id  WHERE order_id IN (1, 2);
SELECT * FROM t_order_0 o JOIN t_order_item_1 i ON o.order_id=i.order_id  WHERE order_id IN (1, 2);
SELECT * FROM t_order_1 o JOIN t_order_item_0 i ON o.order_id=i.order_id  WHERE order_id IN (1, 2);
SELECT * FROM t_order_1 o JOIN t_order_item_1 i ON o.order_id=i.order_id  WHERE order_id IN (1, 2);
```

Cartesian product route has a relatively low performance, so it should be careful to use.

## Broadcast Route

For SQL without sharding key, broadcast route is used. 
According to the types of SQL, it can be divided into database schema broadcast route, schema and table broadcast route, database instance broadcast route, unicast route and ignore route.

### Schema & Table Broadcast Route
Schema & table broadcast route is used to deal with all the operation of physical tables related to its logic table, including mainly DQL and DML without sharding key and DDL, etc.
For example.

```sql
SELECT * FROM t_order WHERE good_prority IN (1, 10);
```

It will traverse all the tables in all the databases, match the logical table and the physical table name one by one, and match it to execute. After routing

```sql
SELECT * FROM t_order_0 WHERE good_prority IN (1, 10);
SELECT * FROM t_order_1 WHERE good_prority IN (1, 10);
SELECT * FROM t_order_2 WHERE good_prority IN (1, 10);
SELECT * FROM t_order_3 WHERE good_prority IN (1, 10);
```

### Database Schema Broadcast Route
Database schema broadcast route is used to deal with the operation for database, including the SET database management order used to set the database and transaction control statement as TCL.
In this case, all physical databases matching the name match are traversed according to the name of the logical database, and the command is executed in the physical database. For example:

```sql
SET autocommit=0;
```

Executed in `t_order`, `t_order` has 2 physical database. This command will actually be executed on both `t_order_0` and `t_order_1`.

### Database Instance Broadcast Route
Database instance broadcast route is used in DCL operation, whose authorization statement aims at database cases. 
No matter how many schemas are included in one case, each case can only be executed once. For example:

```sql
CREATE USER customer@127.0.0.1 identified BY '123';
```

This command will be executed in all physical database instances to ensure that the customer user has access to each instance.

### Unicast Route
Unicast route is used in the situation of acquiring the information of some certain physical table. 
It only requires to acquire data from any physical table in any database. For example:

```sql
DESCRIBE t_order;
```

The descriptions of the two physical tables t_order_0 and t_order_1 of t_order are the same, so this command is selected to execute once on any physical table.

### Ignore Route
Ignore route is used to block the operation of SQL to the database. For example:

```sql
USE order_db;
```
This command will not be executed in the physical database.
Because ShardingSphere uses logic Schema, there is no need to send the database schema shift order to the database.

The overall structure division is as the following picture:

![Route Engine](https://shardingsphere.apache.org/document/current/img/sharding/route_architecture_en.png)
