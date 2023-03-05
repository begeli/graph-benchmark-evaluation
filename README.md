# graph_benchmark_evaluation
Semester project for ETH Zürich. Implemented a benchmark for JanusGraph graph database to compare it with SPCL's GDI implementation for distributed memory RDMA architectures. Benchmark code contains scripts to automate start up of a Cassandra cluster, populating the database instances in the cluster and indexing recently loaded data.

| Folder            | Functionality                                                       |
| ----------------- | ------------------------------------------------------------------- | 
| janusgraph-bench | Contains scripts to start Cassandra cluster, load database snapshots into database instances and start benchmarking code. |
| janusgraph-benchmark-tool | Contains the source code for the JanusGraph benchmarking tool. |
| neo4j-bench | Contains scripts to start Neo4j cluster, load database dumps into Neo4j instances and start benchmarking code. |
| neo4j-benchmark-tool | Contains the source code for the Neo4j benchmarking tool. |
