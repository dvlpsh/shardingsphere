+++
toc = true
title = "Configuration"
weight = 3
+++

## Sharding Rule

Main entrance of sharding configuration. Includes data sources configurations, tables configurations, binding tables configurations and master-slave configurations.

## Data Sources Configurations

Physical data sources list.

## Tables Configurations

Configure logic table, data nodes and sharding strategies.

## Data Node Configurations

Configure relationship mapping with logic table and actual data nodes, can use uniform distribution or user-defined distribution.

- Uniform Distribution

Tables are evenly distributed in each data source, for example: 

```
db0
  ├── t_order_0 
  └── t_order_1 
db1
  ├── t_order_0 
  └── t_order_1
```

Then, configuration of data nodes are: 

```
db0.t_order_0, db0.t_order_1, db1.t_order_0, db1.t_order_1
```

- User-defined Distribution

Tables are distributed by user defined in each data source, for example: 

```
db0
  ├── t_order_0 
  └── t_order_1 
db1
  ├── t_order_2
  ├── t_order_3
  └── t_order_4
```

Then, configuration of data nodes are: 

```
db0.t_order_0, db0.t_order_1, db1.t_order_2, db1.t_order_3, db1.t_order_4
```

## Sharding Strategies Configurations

Include database sharding strategy and table sharding strategy. API of them are same.

## Key Generator Strategy Configurations

Generate distribute unique primary keys by this.

## Config Map

ConfigMap allows you to configure metadata information for data source of Sharding or Read-write splitting. The information of shardingConfig and masterSlaveConfig in ConfigMap can be obtained by calling ConfigMapContext.getInstance (). e.g. Differet weight for machines, different traffic on machines. The metadata for machines' weight can be configured through the ConfigMap.
