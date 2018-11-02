+++
toc = true
title = "Configuration"
weight = 3
+++

## Sharding Rule

The main entrance for Sharding rules includes the configurations of data source, tables, binding tables and read-write split.

## Data Sources Configuration

Real data sources list.

## Tables Configuration

Configurations of logic table names, data node and table sharding rules.

## Data Node Configuration

It is used in the configurations of the mapping relationship between logic tables and actual tables, can be divided into two kinds: uniform distribution and user-defined distribution.

- Uniform Distribution

It means that data tables are evenly distributed in each data source, for example: 

```
db0
  ├── t_order0 
  └── t_order1 
db1
  ├── t_order0 
  └── t_order1
```

So the data node configurations will be as follow:

```
db0.t_order0, db0.t_order1, db1.t_order0, db1.t_order1
```

- User-defined Distribution

It means that data tables are distributed with certain rules, for example:

```
db0
  ├── t_order0 
  └── t_order1 
db1
  ├── t_order2
  ├── t_order3
  └── t_order4
```

So the data node configurations will be as follow:

```
db0.t_order0, db0.t_order1, db1.t_order2, db1.t_order3, db1.t_order4
```

## Sharding Strategy Configuration

There are two dimensions of sharding strategies, database sharding and table sharding.

- Database Sharding Strategy

`DatabaseShardingStrategy` is used to configure data in the targeted database.

- Table Sharding Strategy

`TableShardingStrategy` is used to configure data in the targeted table that exists in the database. 
So the table sharding strategy relies on the result of the database sharding strategy.

API of those two kinds of strategies are totally same.

## Generation Strategy of Auto-increment Key

Replacing the original database auto-increment key with that generated in the server can make distributed key not repeat.

## Config Map

When configuring the metadata from the source of sharding databases and tables, `ConfigMapContext.getInstance()` can be used to acquire shardingConfig data in ConfigMap. 
For instance, machines of different weight may have different data traffic and `ConfigMap` can be used to set the metadata.
