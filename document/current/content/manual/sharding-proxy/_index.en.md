+++
pre = "<b>4.2. </b>"
title = "Sharding-Proxy"
weight = 2
chapter = true
+++

## Introduction

Sharding-Proxy is second project of Sharding-Sphere.
It is a database proxy, is deployed as a stateless server, and supports MySQL protocol now.

* Use standard MySQL protocol, application do not care about whether proxy or real MySQL.
* Any MySQL command line and UI workbench supported in theoretically. MySQL Workbench are fully compatible right now.

![Sharding-Proxy Architecture](http://ovfotjrsi.bkt.clouddn.com/sharding-proxy-brief_v2.png)

## Comparison

|                        | *Sharding-JDBC* | *Sharding-Proxy* | *Sharding-Sidecar* |
| ---------------------- | --------------- | ---------------- | ------------------ |
| Database               | Any             | `MySQL`          | MySQL              |
| Connections Cost       | More            | `Less`           | More               |
| Heterogeneous Language | Java Only       | `Any`            | Any                |
| Performance            | Low loss        | `High loss`      | Low loss           |
| Centre-less            | Yes             | `No`             | No                 |
| Static Entry           | No              | `Yes`            | No                 |

Sharding-Proxy can support heterogeneous languages and provide an operation entry for DBA.
