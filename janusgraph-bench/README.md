# janusgraph-bench

Scripts needed to benchmark JanusGraph on the Piz Daint supercomputer

## Benchmark set up and Folder structure 

You should copy this folder into Piz Daint scratch folder (/scratch/snx3000/\<YOUR USER NAME\>/janusgraph-bench)
The benchmark executable should be located in your $HOME directory. (i.e. /users/\<YOUR USER NAME\>/janusgraph-benchmark-tool.jar)

## Loading Data into JanusGraph 

Loading the graph into JanusGraph is the first step before using the benchmark. JanusGraph does not natively support loading csv files, therefore, we use an open source tool to load the graph.
You can find the tool here: https://github.com/jespersm/janusgraph-csv-import

You first need to generate a jar file of the above tool. You can do that through gradle. Alternatively, you can use an IDE like IntelliJ (which is what I have done, so it is what I will explain.)
IntelliJ allows you to generate the Shadow Jar of the tool just by clicking the green arrow next to https://github.com/jespersm/janusgraph-csv-import/blob/master/build.gradle#L47.
You can find a copy of the import jar file in the /import folder in the current directory.

You should then create the csv files for the graph. Import folder has the file generator (lpg\_graph500\_csv). Get a slurm allocation through salloc. A single node should suffice. Then create the files through the following command: srun --nodes=<NODE\_COUNT> --nodelist=<HOSTNAME\_OF\_NODES> lpg\_graph500\_csv -s <GRAPH\_SCALE> -e 16

After you create the csv files you need to merge the files into a single edge file called edges.csv. You can do that through the merge\_edge\_files script. You can now deallocate the slurm allocation. (i.e. call ./merge\_edge\_files in the folder which has the graph csv files.)

You should make another slurm allocation. This time the allocation should have two nodes. (i.e. salloc --nodes=2 ...)
Start a Cassandra node in the first node. (./start\_cassandra --nodes \<HOSTNAME1>)

This command should create a runtime folder in the janusgraph-bench folder. Create two additional folders in the runtime folder. 
- mkdir ./runtime/janus-data 
- mkdir ./runtime/janus-conf

SSH into the second node.
- ssh \<HOSTNAME2>
- module load singularity 

Start the JanusGraph instance
```
singularity instance start 
--writable-tmpfs 
--no-home 
--env JANUS_PROPS_TEMPLATE=cql 
--bind $SCRATCH/janusgraph-bench/runtime/janus-data:/var/lib/janusgraph 
--bind $SCRATCH/janusgraph-bench/runtime/janus-conf:/etc/opt/janusgraph 
--bind $SCRATCH/janusgraph-bench/janusgraph/janusgraph-server.yaml:/conf/janusgraph-server.yaml 
--bind $SCRATCH/janusgraph-bench/janusgraph/janusgraph-cql-server.properties:/conf/janusgraph-cql-server.properties 
--env JANUS_HOME=/opt/janusgraph 
--env JANUS_CONFIG_DIR=/etc/opt/janusgraph 
--env JANUS\_DATA\_DIR=/var/lib/janusgraph 
--env JAVA\_HOME=/usr/local/openjdk-8 
$SCRATCH/janusgraph-bench/janusgraph/janusgraph.sif 
janusgraph
```

Use the import tool mentioned above to start loading the csv files into the JanusGraph. (Read all of the commands before you start executing them.)

- singularity shell --bind /scratch/snx3000/\<YOUR USER NAME\>/janusgraph-bench/import/:/users/\<YOUR USER NAME\>/import/ docker://openjdk:8-jdk
- cd import 

**IMPORTANT NODE:** Make sure before you execute the next command, the storage.backend parameter is set to \<HOSTNAME1\> in janusgraph\_import.properties file

```
java -jar janusgraph-csv-import-all.jar 
-c=janusgraph\_import.properties 
-D 
--threads=16 
--nodes=Company=nodes\_Company.csv 
--nodes=Person=nodes\_Person.csv 
--nodes=Place=nodes\_Place.csv 
--nodes=Project=nodes\_Project.csv 
--nodes=Resource=nodes\_Resource.csv 
--relationships=edges.csv 
--edgeLabels=CAN\_BE\_USED\_WITH,CAN\_USE,FOUND\_AT,HAS\_BRANCHES\_AT,IMPACTS,IN\_BUSINESS\_WITH,INFLUENCES,IN\_VICINITY\_OF,IS\_PART\_OF,KNOWS,NEEDS,SUPPORTS,USES,WAS\_IN,WORKS\_AT
```

- SSH into the first node (i.e. ssh \<HOSTNAME1\>)
- Take a snapshot of the graph you loaded. (i.e. module load singularity && singularity exec instance://cassandra nodetool snapshot janusgraph)

Now in your primary Daint node there should be a snapshot of the loaded data in the /runtime/cassandra folder. (I don't remember the exact path in the runtime folder but the path of the snapshots should look something like this: /runtime/cassandra/00/data/janusgraph/\<Table Name\>/snapshot/\<Snapshot id\>/ which contains the sstables for the table.) 

Copy all of the snapshotted sstables into /janusgraph-bench/cassandra/snapshot/\<Table Name\>/ folder. (You have to create this folder yourself.)

In the end the snapshot folder should look like the following:

```
/janusgraph-bench
  /cassandra
    /snapshot
      /edgestore
         <SSTables>
      /edgestore_lock_
         <SSTables>
      /graphindex
         <SSTables>
      /graphindex_lock_
         <SSTables>
      /janusgraph_ids
         <SSTables>
      /system_properties
         <SSTables>
      /system_properties_lock_
         <SSTables>
      /systemlog
         <SSTables>
      /txlog
         <SSTables>
``` 

## Indexing the Graph

Unfortunately, the graph is still not ready for use. To be able use the graph for benchmarking, we need to index the vertices in the nodes.

- Get a slurm allocation for a single node. 
- cd /scratch/snx3000/\<User Name\>/janusgraph-bench
- ./create\_janusgraph\_cql\_config --hostname \<HOSTNAME\> --target-dir /users/\<User Name\>/
- ./start\_cassandra --nodes \<HOSTNAME\>
- ./create\_schema --seed \<HOSTNAME\>
- ./load\_snapshot --nodes \<HOSTNAME\>

- cd /users/\<User Name\>/
- module load singularity
- singularity exec docker://openjdk:11-jdk java -jar janusgraph-benchmark-tool.jar --config janusgraph-cql-server.properties --threads 0 --tasks 0

When the benchmark is started, it will first check for the existence of an index called byIdComposite. If the index does not exist, the benchmark will index the graph.
We set threads and tasks to 0 to prevent the benchmark from executing queries on the indexed graph.
You should then take a snapshot of the indexed graph in the Cassandra node, and create a new snapshot folder as described in the previous section.

After this stage, the graph snapshot is ready for use. 

## Usage

Clone this repo to you scratch space (`/scratch/snx3000/${USER}/`, if you use another location inside scratch, you need to use the `--base-dir` flag). There are two options to run the benchmark:

- Allocate the requested number of rows (e.g. `salloc -p normal -C mc --account=g34 --nodes=5 --time=1:00:00`). Run the script manually 
(i.e. ./run\_bench -c \<BACKEND NODE COUNT\> -b \<BENCHMARK NODE COUNT\>, ideally backend node count + benchmark node count = total node count, so the nodes do not overlap).
- run\_bench script contains hardcoded output folder/file names, you can change them manually.
- The benchmark executable has to execution modes, throughput mode (which measures the total execution time of queries on a thread) and latency mode (which measures the execution time of individual queries.) You can switch between throughput mode and latency mode by (un)commenting the respective sections in the run\_bench and run\_bench\_instance scripts.

**Important Note:** The benchmark executable is harcoded to work with scale 23 graphs by default. If you want to use the benchmark with different graph scales you need to change node ID boundaries in the benchmark code. (i.e. change the boundaries here https://spclgitlab.ethz.ch/gerro/gdi-working/-/blob/begeli/benchmark_evaluation/janusgraph-benchmark-tool/src/main/java/ch/ethz/spcl/node/PersonNode.java#L14-15) 

The Node files are located in the following folder: https://spclgitlab.ethz.ch/gerro/gdi-working/-/tree/begeli/benchmark_evaluation/janusgraph-benchmark-tool/src/main/java/ch/ethz/spcl/node

After this change, you need to rebuild the jar file.

## Scripts

A short description of the scripts in this repo.

| Script            | Functionality                                                       |
| ----------------- | ------------------------------------------------------------------- |
| `start_cassandra` | Start the Cassandra storage backend. See `./start_cassandra --help` |
| `stop_cassandra`  | Stop the Cassandra storage backend on all nodes in the allocation   |
| `create_cassandra_config` | Create the yaml file needed to connect to cassandra nodes during benchmark execution |
| `create_janusgraph_cql_config` | Create the config file which will be used by the benchmark executable to connect to JanusGraph backend |
| `run_bench_instance`| Run the benchmark executable in a single specified compute node |
| `load_snapshot` | Load the snapshot in the /janusgraph-bench/cassandra/snapshot folder into the JanusGraph backend |
| `run_bench` | Use all of these scripts to set up the JanusGraph cluster, load the data and also start the benchmark executables in the benchmark compute nodes and merge all of the output files in the hardcoded output files (they are hardcoded in the run\_bench script) |
