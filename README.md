# Mammoths Are Slow: The Overlooked Transactions of Graph Data

The repository contains the mammoth experiments code and community lock simulation code for the paper _"Mammoths Are Slow: The Overlooked Transactions of Graph Data"_.

This repository is structured as follows:

* `/mammoth-exp` - mammoth experiments code 
    * `/src` - contains Java source files for the test driver program
    * `/scripts` - contains utility scripts for running the experiment, i.e., starting/stopping the DBMS, loading data, and plotting results 
    * `b-params.csv` and `u-params.csv` files are used by the driver for parameterising transactions for the balanced and unbalanced mammoth experiments, respectively 
* `/sim-lock-escalation` - community lock simulation code
    * `/src` - contains Java source files for the simulation program
    * `/scripts` - contains utility scripts for running the experiment and plotting the results

Note, `/mammoth-exp` and `/sim-lock-escalation` are separate Java projects.

Prerequisites:
* Maven 3.8.5
* Java 17
* R 4.3.0 or higher 
* Neo4j 5.4.0

## Mammoth Experiments

### What is this set of experiments?

The goal of these experiments is evaluate the performance of an existing graph database (Neo4j) on balanced and unbalanced mammoth transactions. 
They involve executing a workload consisting of a configurable ratio of OLTP-style read-only and read-write transactions that access or update data on nodes in a social network graph. 
A mammoth transaction (either balanced and unbalanced) is run concurrently with the OLTP-style transactions while throughput is measured.
For more information, see Section 3 of the paper.

### How to run this experiment

Generate the LDBC SF 1 dataset. Detailed instructions can be found [here](https://github.com/ldbc/ldbc_snb_datagen_spark/).

Set the following environment variables:
```
export SF=1
export NEO4J_VERSION=5.4.0
export NEO4J_CSV_DIR=$HOME/ldbc_snb_datagen_spark/out-sf1/graphs/csv/bi/composite-projected-fk/
export NEO4J_HOME=$HOME/neo4j-enterprise-$NEO4J_VERSION
export NEO4J_DATA_DIR=$HOME/neo4j-enterprise-$NEO4J_VERSION/data
export NEO4J_PART_FIND_PATTERN=part-*.csv*
export NEO4J_HEADER_EXTENSION=.csv
export NEO4J_HEADER_DIR=$HOME/ldbc_snb_interactive_impls/cypher/headers
export FIND_COMMAND=find
export MAMMOTH_HOME=$HOME/mammoths-are-slow/mammoth-exp
```

Then set `dbms.security.auth_enabled=false` in `neo4j.conf`.

Navigating to `scripts/` and start Neo4j:
```
./start-dbms.sh
```

Import LDBC data into Neo4j:
```
./import.sh
```

Package the test driver jar:
```
cd $HOME/mammoths-are-slow/mammoth-exp
mvn clean package 
```

Run experiment: 
```
java -cp ./target/mammothsareslow-1.0-SNAPSHOT.jar Main --balanced <true/false> --duration <experiment_duration> --mammothDelay <mammothStartDelay> --readClients <readTxnClients> -writeClients <writeTxnClients> --uri <neo4jUri>
```

Parameters:
```
-b, --balanced=<BALANCED>              Balanced or unbalanced workload
-d, --duration=<EXPERIMENT_DURATION>   Experiment duration (secs)
-m, --mammothDelay=<MAMMOTH_DELAY>     Delay before starting mammoth transaction (secs)
-r, --readClients=<READERS>            Number of read clients
-u, --uri=<URI>                        Neo4j URI
 w, --writeClients=<WRITERS>           Number of write clients
```

The fixed values used for our experiments are `--duration=75` and `--mammothDelay=30`. The `--uri` is dependent on your setup; if run locally, it would be `bolt://localhost:7687`. 
`--balanced` is either `true` or `false`, depending on which experiment you wish to run. 
`--writeClients` and `--readClients` were varied in our experiments to evaluate performance under different ratios.

Stop Neo4j:
```
./stop-dbms.sh
```

Delete database:
```
./delete-database.sh
```

Results are outputted to `results.csv`. The file format is `throughput/sec,aborts` and can be plotted using:
```
./make-plot.sh
```
This plots the throughput over time, denoting the start and end time of the mammoth that was executed (see Figures 1a and 1b in the paper).


## Community Lock Simulation 

### What is this experiment?

The goal of this experiment is to measure the impact of different lock escalation strategies on graph data.
In the experiment, a graph with 100K nodes is generated, and each node assigned to a range and a community based on its unique id.
Ranges are contiguous groups of ids (e.g., the first range includes node 1 to node 99). 
For communities, nodes are randomly assigned to one (i.e., each community will contain a set of node ids that may not align with the sequential order of ids). 
We simulate traversal operations on the graph, with an 85% probability that each subsequent operation within a traversal  will access a node in the same community as the previous one. 
We then compare the number of nodes locked under range locks versus community locks. 
For more information, see Section 5.1 of the paper.

### How to run this experiment

Navigate to the directory:
```
cd sim-lock-escalation 
```

Package the jar:
```
mvn clean package
```
 
Run the simulation:
```
java -cp ./target/sim-lock-escalation-1.0-SNAPSHOT.jar Main --keys <keys> --transactionSize <transactionSize> --communities <communities> --ranges <ranges>
```

Parameters:
```
 -c, --communities=<communities>         Number of communities
 -k, --keys=<keys>                       Database size
 -r, --ranges=<ranges>                   Number of ranges
 -t, --transactionSize=<transactionSize> Transaction size
```

The experiment in the paper can be reproduced by navigating to `scripts/` and executing `run.sh`. 
The values used are `--keys=100000`, `--transactionSize=10`, `--ranges` and `--communities` are varied from 10 to 30.

Results are outputted to `results.csv`. The file format is `keys,transactionSize,rangeSize,communities,rangeLocked,communityLocked`. 
The `rangeLocked` and `communityLocked` columns report the nodes locked under each lock escalation strategy.

Results can be plotted using `Rscript plot.R`, displaying the locks taken by each strategy as the range size and community count is varied (see Figure 2 in the paper).


