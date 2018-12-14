+++
pre = "<b>3.4.1. </b>"
toc = true
title = "Local transaction"
weight = 1
+++

## Concept

* Support the none-cross-database transactions, e.g. table sharding without database sharding, or database sharding with the queries routed in the same database.

* Support the exception handling for the cross-database transactions due to logical exceptions. For example, in the same transaction, you want to update two databases, ShardingSphere will rollback all transactions for all two database when a null pointer is thrown after updating.

* Do not support the exception handling for the cross-database transactions due to network or hardware exceptions. For example, in the same transaction, you want to update two databases, ShardingSphere will only commit the transaction for second database when the first database is dead after updating.

## Supported

* Sharding-JDBC support by end user to configure without XA data sources

* Sharding-Proxy do not need support, just use XA or BASE transaction
