# Mammoths Are Slow: The Overlooked Transactions of Graph Data

The repository currently contains mammoth experiment and community lock simulation code for _"Mammoths Are Slow: The Overlooked Transactions of Graph Data"_.

This repository is structured as follows:

/sim-lock-escalation - community lock simulation code
/mammoth-exp - mammoth experiment code 

Prerequisites:
* Maven 3.8.5
* Java 17
* R 4.3.0 or higher 

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

