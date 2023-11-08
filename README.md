# Mammoths Are Slow: The Overlooked Transactions of Graph Data

The repository currently contains mammoth experiment and community lock simulation code for _"Mammoths Are Slow: The Overlooked Transactions of Graph Data"_.

This repository is structured as follows:

* /sim-lock-escalation - community lock simulation code
* /mammoth-exp - mammoth experiment code 

Prerequisites:
* Maven 3.8.5
* Java 17
* R 4.3.0 or higher 
* Neo4j 5.4.0

## Community Lock Simulation 

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

The experiment in the paper can be reproduced by navigating to `scripts/` and executing `run.sh`. 
Results are outputted to `results.csv` and can be plotted using `Rscript plot.R`


## Mammoth Experiment

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

Stop Neo4j:
```
./stop-dbms.sh
```

Delete database:
```
./delete-database.sh
```

Results are outputted to `results.csv` and can be plotted using:
```
./make-plot.sh
```



