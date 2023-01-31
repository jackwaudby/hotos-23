# HotOS 2023


Env vars
```
SF=0.003
LDBC_SNB_DATAGEN_DIR=/Users/replacementloaner/ldbc_snb_datagen_spark
NEO4J_CSV_DIR=/Users/replacementloaner/ldbc_snb_datagen_spark/out-sf1/graphs/csv/bi/composite-projected-fk/
NEO4J_HOME=/Users/replacementloaner/neo4j-enterprise-5.0.0
NEO4J_DATA_DIR=/Users/replacementloaner/neo4j-enterprise-5.0.0/data
NEO4J_PART_FIND_PATTERN=part-*.csv*
NEO4J_HEADER_EXTENSION=.csv
NEO4J_HEADER_DIR=/Users/replacementloaner/ldbc_snb_interactive_impls/cypher/headers
FIND_COMMAND=gfind
HOTOS_HOME=/Users/replacementloaner/hotos
```

Set `dbms.security.auth_enabled=false` in `neo4j.conf`