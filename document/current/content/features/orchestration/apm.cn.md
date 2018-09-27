+++
pre = "<b>3.3.4. </b>"
toc = true
title = "应用性能监控"
weight = 4
+++

## 背景

`APM`是应用性能监控的缩写。目前`APM`的主要功能着眼于分布式系统的性能诊断，其主要功能包括调用链展示，应用拓扑分析等。

[Sharding-Sphere](http://shardingsphere.io)团队与[SkyWalking](http://skywalking.io)团队共同合作，推出了`Sharding-Sphere`自动探针，可以将`Sharding-Sphere`的性能数据发送到`SkyWalking`中。

## 使用方法

### 使用SkyWalking插件

请参考[SkyWalking部署手册](https://github.com/apache/incubator-skywalking/blob/5.x/docs/cn/Quick-start-CN.md)。

### 使用OpenTracing插件

如果想使用其他的APM系统，且该系统支持[OpenTracing](http://opentracing.io)。可以使用Sharding-Sphere提供的API配合该APM系统使用。

* 通过读取系统参数注入APM系统提供的Tracer实现类
```
    启动时添加参数：-Dshardingsphere.opentracing.tracer.class=org.apache.skywalking.apm.toolkit.opentracing.SkywalkingTracer
    调用初始化方法：ShardingTracer.init()                          
```

* 通过参数注入APM系统提供的Tracer实现类 
```
    shardingTracer.init(new SkywalkingTracer())   
```

*注意:使用SkyWalking的OpenTracing探针时，应将原Sharding-Sphere探针插件禁用，以防止两种插件互相冲突*


## 效果展示

### 应用架构

该应用是一个`SpringBoot`应用，使用`Sharding-Sphere`访问两个数据库`ds0`和`ds1`，且每个数据库中有两个分表。

### 拓扑图展示

![拓扑图](http://ovfotjrsi.bkt.clouddn.com/apm/apm-topology-new.png)

从图中看，虽然用户访问一次应用，但是每个数据库访问了两次。这是由于本次访问涉及到每个库中的两个分表，所以一共访问了四张表。

### 跟踪数据展示

![拓扑图](http://ovfotjrsi.bkt.clouddn.com/apm/apm-trace-new.png)

从跟踪图中可以能够看到SQL路由、执行和最终结果归并的情况。

`/SHARDING-SPHERE/ROUTING/` : 表示本次SQL的解析路由性能。

![解析路由节点](http://ovfotjrsi.bkt.clouddn.com/apm/apm-route-span.png)

`/SHARDING-SPHERE/EXECUTE/{SQLType}` : 表示本次SQL的总体执行性能。

![逻辑执行节点](http://ovfotjrsi.bkt.clouddn.com/apm/apm-execute-overall-span.png)

`/SHARDING-SPHERE/EXECUTE/` : 表示具体执行的实际SQL的性能。

![实际访问节点](http://ovfotjrsi.bkt.clouddn.com/apm/apm-execute-span.png)

`/SHARDING-SPHERE/MERGE/` : 表示执行结果归并的性能。

![结果归并节点](http://ovfotjrsi.bkt.clouddn.com/apm/apm-merge-span.png)
