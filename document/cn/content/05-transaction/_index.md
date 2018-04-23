+++
pre = "<b>5. </b>"
title = "事务"
weight = 5
chapter = true
+++

## 背景

对于分布式的数据库来说，强一致性分布式事务在性能方面存在明显不足。追求最终一致性的柔性事务，在性能和一致性上则显得更加平衡。
Sharding-Sphere目前最大努力送达型柔性事务，未来也将支持TCC柔性事务。

若不使用柔性事务，Sharding-Sphere也会自动包含弱XA事务支持。
