+++
pre = "<b>4.2.1. </b>"
toc = true
title = "User Manual"
weight = 1
+++

## Proxy

1. Download the latest version of Sharding-Proxy from https://github.com/sharding-sphere/sharding-sphere-doc/raw/master/dist/sharding-proxy-3.0.0.tar.gz
1. If using docker，execute command `docker pull shardingsphere/sharding-proxy` to get image. More details please reference[Docker Image](/en/manual/sharding-proxy/docker/).
1. Modify the `conf/server.yaml` and `conf/config-xxx.yaml` file after decompression, and configure the sharding rule and master-slave rule. Please reference [Configuration Manual](/en/manual/sharding-proxy/configuration/).
1. Run `bin/start.sh` on Linux, or `bin/start.bat` on Windows to start Sharding-Proxy. If you want to set port and configuration file, please refer to [quick-start](/en/quick-start/sharding-proxy-quick-start/).
1. Connect to it by means of any client tools, e.g. `mysql -u root -h 127.0.0.1 -P 3307`

## Registry usage

If you want to use the orchestration for Sharding-Proxy, the registry is necessary. Please refer to [Supported Registry Centers](/en/features/orchestration/supported-registry-repo/) for more detail.

## Zookeeper

1. Sharding-Proxy provides Zookeeper registry by default. You only need to configure the registry according to [the registry configuration](/en/manual/sharding-proxy/configuration/).

## Etcd

1. Delete `sharding-orchestration-reg-zookeeper-curator-${sharding-sphere.version}.jar` in the lib directory of Sharding-Proxy.
1. Download Etcd solution jar from Maven repository, here is [the latest version](http://central.maven.org/maven2/io/shardingsphere/sharding-orchestration-reg-etcd/).
1. Put the downloaded jar package in the lib directory of Sharding-Proxy.
1. Configure the registry according to [the registry configuration](/en/manual/sharding-proxy/configuration/).

## Others

1. Delete `sharding-orchestration-reg-zookeeper-curator-${sharding-sphere.version}.jar` in the lib directory of Sharding-Proxy.
1. Implement the logic coding using SPI to create jar package, and put it in the lib directory of Sharding-Proxy.
1. Configure the registry according to [the registry configuration](/en/manual/sharding-proxy/configuration/).

## Notices

1. The default port of Sharding-Proxy is 3307, user can change it by passing the port number on startup script, e.g. `bin/start.sh 3308`.
1. Sharding-Proxy use conf/server.yaml to configure registry-center, authentication and common properties.
1. Sharding-Proxy support multiple logic schema, for every configuration file which prefix as `config-`, and suffix as `.yaml`.
