# HotOS 2023


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

PAT 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

scp -i ./ssh_keys/neo4j-server_key.pem azureuser@4.234.216.240:~/hotos-23/test.csv ./data.csv

## Evaluation 

### Balanced 

1. Cascading delete 
2. Community detection 
3. Post visibility / going private
4. Book/stock visibiltiy 

### Unbalanced 

1. Fake accounts 
2. Path finding 


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
* Challenge: finding deeper path would improve the accuracy of the centrality algorithm, but even on relatively small graph the explodes the number of paths found. Thus, the transaction takes disproportionally longer. 3 deep takes 20 secs (22M paths), 4 deep takes ()

2.1 
* Gremlin is supported by Amazon Neptune and JanusGraph
* MERGE either (i) matches existing nodes, or (ii) it creates new data if the requested pattern is not found. It either matches the whole pattern, or the whole pattern is created. MERGE will not work if partial matches are found.

2.2.1
* Can we find a way to make community detection more broad. The example we use for balanced in the evaluation is a centrality algorithm. These are balanced graph algorithms. 
* I would argue they don't need to be written back, but having them avoids recomputation and allows the application to use this information as part of other (OLTP) transactions.
* Politicans answer: its hard to say because each customer's workload is very distinct. Also, generally speaking we are conditioned at Neo4j to split things up to avoid hitting any boundaries, and/or running the heavy stuff on dedicated read replica in clusters, or dedicated instances running the GDS library 

2.3
The LDBC Interative workload is a popular graph database benchmark containing complex,
processing-intensive reads queries, interspersed with short read queries, and insert operations. Whilst work is ongoing to include deletions operations similarly to what is described in Sec X, it remains lacking updates to node and edge labels and properties and  complex read-write transactions with arbitrary large and read- and write sets.

3
Neo4j's default isolation level is Read Committed, though it was demonstrated in X that it provides Monotonic Atomic View. Via Cypher explicit write locks can be taken on nodes to simulate improved isolation in some cases to prevent Lost Updates for example.

CREATE 

CREATE p = (n:Person {name: 'Jack', id: 1})-[:CREATED]->(r:Post {content: "rant", up: 0})
RETURN p

UNWIND range(1, 50) AS i
MERGE (p:Person { id: i })
ON CREATE
MATCH (j:Person {name: 'Jack', id: 1})
CREATE (j)-[r:KNOWS {close: false}]->(p)

UNWIND range(1, 50) AS i
MATCH (j:Person {name: 'Jack', id: 1}), MATCH (f:Person {name: 'Jack', id: i}), 
CREATE (j)-[r:KNOWS {close: false}]->(f)
