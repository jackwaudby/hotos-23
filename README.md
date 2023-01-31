# HotOS 2023


Env vars
```
export SF=1
export LDBC_SNB_DATAGEN_DIR=$HOME/ldbc_snb_datagen_spark
export NEO4J_CSV_DIR=$HOME/ldbc_snb_datagen_spark/out-sf1/graphs/csv/bi/composite-projected-fk/
export NEO4J_HOME=$HOME/neo4j-enterprise-5.4.0
export NEO4J_DATA_DIR=$HOME/neo4j-enterprise-5.4.0/data
export NEO4J_PART_FIND_PATTERN=part-*.csv*
export NEO4J_HEADER_EXTENSION=.csv
export NEO4J_HEADER_DIR=$HOME/ldbc_snb_interactive_impls/cypher/headers
export FIND_COMMAND=find
export HOTOS_HOME=$HOME/hotos-23
```

Set `dbms.security.auth_enabled=false` in `neo4j.conf`

PAT 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

scp -i ./ssh_keys/neo4j-server_key.pem azureuser@4.234.216.240:~/hotos-23/test.csv ./data.csv