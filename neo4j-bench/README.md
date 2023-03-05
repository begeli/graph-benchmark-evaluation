# neo4j-bench
Scripts needed to benchmark neo4j on the Piz Daint super computer

## Folder structure 
Neo4j benchmark tool executable (the jar file should be located in the $HOME folder.)

```
/scratch/snx3000/\<USER\_NAME\>
  /graph500/
    /sxxe16 (the folder which contains the csv files for the graph)
      nodes_*.csv
      edges_*.csv
    /sxxe16.dump (xx is the scale of the graph)
  /neo4j-bench
```

## Dump file generation

- cd /scratch/snx3000/\<USER\_NAME\>/neo4j-bench 

1) salloc -p normal -C mc --account=g34 --nodes=5 --time=00:30:00
Inside the $SCRATCH/neo4j-bench folder
2) ./start\_neo4j -c 3 -r 2 // (create the runtime folders)
3) ./stop\_neo4j
4) ssh ${core host 00}
5) singularity exec \
    --writable-tmpfs \
    --bind $SCRATCH/neo4j-bench/neo4j.conf:/conf/neo4j.conf \
    --bind $SCRATCH/neo4j-bench/runtime/core_server_list:/core_server_list \
    --bind $SCRATCH/neo4j-bench/runtime/number_core_servers:/number_core_servers \
    --bind $SCRATCH/neo4j-bench/runtime/data/core00:/data \
    --bind $SCRATCH/neo4j-bench/runtime/logs/core00:/logs \
    --bind $SCRATCH/neo4j-bench/runtime/conf/core00:/var/lib/neo4j/conf \
    --bind $SCRATCH/graph500/s23e16/:/var/lib/neo4j/import \
    --env NEO4J_AUTH=neo4j/test \  // I assume we set the database password here
    --env TINI_SUBREAPER=true \
    --env NEO4J_dbms_mode=core \
    --env EXTENDED_CONF=yes \
    --env NEO4J_ACCEPT_LICENSE_AGREEMENT=yes \
    $SCRATCH/neo4j-bench/neo4j_enterprise.sif /bin/bash
6) neo4j-admin dbms set-initial-password test
7) neo4j-admin import --database lpg1 \
    --nodes=Company=/var/lib/neo4j/import/nodes_Company.csv \
    --nodes=Person=/var/lib/neo4j/import/nodes_Person.csv \
    --nodes=Place=/var/lib/neo4j/import/nodes_Place.csv \
    --nodes=Project=/var/lib/neo4j/import/nodes_Project.csv \
    --nodes=Resource=/var/lib/neo4j/import/nodes_Resource.csv \
    --relationships=CAN_BE_USED_WITH=/var/lib/neo4j/import/edges_canBeUsedWith.csv \
    --relationships=CAN_USE=/var/lib/neo4j/import/edges_canUse.csv \
    --relationships=FOUND_AT=/var/lib/neo4j/import/edges_foundAt.csv \
    --relationships=HAS_BRANCHES_AT=/var/lib/neo4j/import/edges_hasBranchesAt.csv \
    --relationships=IMPACTS=/var/lib/neo4j/import/edges_impacts.csv \
    --relationships=IN_BUSINESS_WITH=/var/lib/neo4j/import/edges_inBusinessWith.csv \
    --relationships=INFLUENCES=/var/lib/neo4j/import/edges_influences.csv \
    --relationships=IN_VICINITY_OF=/var/lib/neo4j/import/edges_inVicinityOf.csv \
    --relationships=IS_PART_OF=/var/lib/neo4j/import/edges_isPartOf.csv \
    --relationships=KNOWS=/var/lib/neo4j/import/edges_knows.csv \
    --relationships=NEEDS=/var/lib/neo4j/import/edges_needs.csv \
    --relationships=SUPPORTS=/var/lib/neo4j/import/edges_supports.csv \
    --relationships=USES=/var/lib/neo4j/import/edges_uses.csv \
    --relationships=WAS_IN=/var/lib/neo4j/import/edges_wasIn.csv \
    --relationships=WORKS_AT=/var/lib/neo4j/import/edges_worksAt.csv \
    --trim-strings=true \
    --expand-commands
8) neo4j-admin dump --database=lpg1 --to=/var/lib/neo4j/import/s23e16.dump --expand-commands

Exit the singularity container and

- cd /scratch/snx3000/\<USER\_NAME\>/graph500/sxxe16/
- mv sxxe16.dump ..

Then the benchmark should become usable. Just change the dump file name in the run\_bench script to fit sxxe16 (Again, xx being the scale) 

## Usage

Clone this repo to you scratch space (`/scratch/snx3000/${USER}/`, if you use another location inside scratch, you need to use the `--base-dir` flag). Two ways to run exist:

- Allocate the requested number of rows (e.g. `salloc -p normal -C mc --account=g34 --nodes=5 --time=1:00:00`). Run the script manually (e.g. `./start_neo4j --cores 3 --replicas 2`).
- Modify the `start_neo4j` script to run the required benchmark after launch, and use sbatch (e.g. `sbatch --nodes=5 start_neo4j -c 3 -r 2`).

Starting the cluster will create a `runtime/` directory with persitent files for each instance. `runtime/logs/` can be usefull for debugging. If you want to start from a clean state, simply delete the entire `runtime/` directory.


## Scripts

A short description of the scripts in this repo.

| Script                 | Functionality                                                                             |
| ---------------------- | ----------------------------------------------------------------------------------------- |
| `start_neo4j`          | Start a neo4j cluster (from inside slurm allocation). See `start_neo4j --help` for usage. |
| `start_neo4j_instance` | Start one instance (core or replica). This is mainly used by `start_neo4j`.               |
| `stop_neo4j`           | Stops the neo4j instances on all servers in the allocation.                               |
| `connect_cypher_shell` | Connect to the first core server in the cluster using cypher shell.                       |
| `load_dump`            | Load a database dump to multiple core instances at the same time.                         |


## Neo4j container image

`neo4j_enterprise.sif` is the container image that should be used with these script. It contains a slight modification, because singularities conversion from docker images to `.sif` images does not set the start script correctly. `neo4j_enterprise.sif` can be recreated by following the below steps.  
**Note:** This requires root privileges and can thus not be done on Piz Daint. Do it localy and copy the resulting image to daint.

 1. Pull the neo4j enterprise docker image ([neo4j:enterprise](https://hub.docker.com/_/neo4j)) using singularity (`singularity pull docker://neo4j:enterprise`)
 2. Convert into a sandbox (`sudo singularity build --sandbox <SANDBOX> <CONTAINER>.sif`(
 3. Edit `<SANDBOX>/.singularity.d/startscript` to mimic `<SANDBOX>/.singularity.d/runscript`
 4. Rebuild the container (`sudo singularity build <NEW_CONTAINER>.sif <SANDBOX>`)
