# VLDB 2023


Env vars
```
export SF=1
export NEO4J_VERSION=5.7.0
export LDBC_SNB_DATAGEN_DIR=$HOME/ldbc_snb_datagen_spark
export NEO4J_CSV_DIR=$HOME/ldbc_snb_datagen_spark/out-sf1/graphs/csv/bi/composite-projected-fk/
export NEO4J_HOME=$HOME/neo4j-enterprise-$NEO4J_VERSION
export NEO4J_DATA_DIR=$HOME/neo4j-enterprise-$NEO4J_VERSION/data
export NEO4J_PART_FIND_PATTERN=part-*.csv*
export NEO4J_HEADER_EXTENSION=.csv
export NEO4J_HEADER_DIR=$HOME/ldbc_snb_interactive_impls/cypher/headers
export FIND_COMMAND=find
export HOTOS_HOME=$HOME/hotos-23
```

Set `dbms.security.auth_enabled=false` in `neo4j.conf`


## Evaluation 

* Neo4j enterprise 5.4.0
* Azure Standard D48ds v5 (48 vcpus, 192 GiB memory)
* LDBC dataset with 10K Person nodes, 346k Knows edges
* Ubuntu 20.04.5 LTS distribution
* 90/10 split between OLTP read-only and read-write transactions
* Read-only: look up Person node
* Read-write: look up Person node and set lastSeen property
* Person nodes are selected randomly with equal probabilty 
* After 60 seconds a mammoth transaction is executed 
* Mammoth: approximate/synthetic/naive centrality algorithm;
    * For each Person find all paths that are 3-levels deep via Knows relationship
    * For each Person in each path found increment visited property
    * Return the top 10 Persons with the highest visited property
* Rationale: detects central nodes in the network, i.e., the ones that are on most paths
* Report throughput over time
