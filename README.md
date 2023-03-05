# Evaluation of Graph Databases
Semester project for ETH Zürich. Implemented a benchmark for JanusGraph graph database to compare it with SPCL's GDI implementation for distributed memory RDMA architectures. Benchmark code contains scripts to automate start up of a Cassandra cluster, populating the database instances in the cluster and indexing recently loaded data. 

In this work, we measure and compare the performance of open source graph database management systems, JanusGraph and Neo4j, against Graph Database Interface (GDI) in high performance computing settings, scaling up to thousands of cores and dozens of nodes.

| Folder            | Functionality                                                       |
| ----------------- | ------------------------------------------------------------------- | 
| janusgraph-bench | Contains scripts to start Cassandra cluster, load database snapshots into database instances and start benchmarking code. |
| janusgraph-benchmark-tool | Contains the source code for the JanusGraph benchmarking tool. |
| neo4j-bench | Contains scripts to start Neo4j cluster, load database dumps into Neo4j instances and start benchmarking code. |
| neo4j-benchmark-tool | Contains the source code for the Neo4j benchmarking tool. |
| report | Contains the report which compares the performance of JanusGraph and Neo4j against GDA. |
