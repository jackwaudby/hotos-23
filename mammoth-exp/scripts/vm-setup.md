
# Install Neo4j 
`
yes | sudo apt-get update &&
yes | sudo apt install build-essential
yes | sudo apt install openjdk-17-jdk openjdk-17-jre
yes | sudo apt install zip
wget http://www.neo4j.com/customer/download/neo4j-enterprise-5.4.0-unix.tar.gz
tar zxf neo4j-enterprise-5.4.0-unix.tar.gz
./bin/neo4j-admin server license --accept-commercial && ./bin/neo4j-admin server license --accept-evaluation
sed -i '/dbms.security.auth_enabled=true/ c\dbms.security.auth_enabled=false' conf/neo4j.conf 
`

# Generate LDBC Dataset 

`git clone https://github.com/ldbc/ldbc_snb_datagen_spark.git`

```
curl -s "https://get.sdkman.io" | bash
source "/home/azureuser/.sdkman/bin/sdkman-init.sh"
sdk install sbt
```

`yes | sudo apt-get update; sudo apt-get install make build-essential libssl-dev zlib1g-dev \
libbz2-dev libreadline-dev libsqlite3-dev wget curl llvm \
libncursesw5-dev xz-utils tk-dev libxml2-dev libxmlsec1-dev libffi-dev liblzma-dev`

`curl https://pyenv.run | bash`

```
export PATH="$HOME/.pyenv/bin:$PATH"
eval "$(pyenv init --path)"
eval "$(pyenv virtualenv-init -)"
exec $SHELL
```

```
pyenv install 3.7.13
pyenv virtualenv 3.7.13 ldbc_datagen_tools
pyenv local ldbc_datagen_tools
pip install -U pip
pip install ./tools
eval "$(pyenv init -)"
eval "$(pyenv virtualenv-init -)"
pyenv activate
```

Use java 11
```
scripts/build.sh
export PLATFORM_VERSION=$(sbt -batch -error 'print platformVersion')
export DATAGEN_VERSION=$(sbt -batch -error 'print version')
export LDBC_SNB_DATAGEN_JAR=$(sbt -batch -error 'print assembly / assemblyOutputPath')
./tools/run.py <runtime configuration arguments> -- <generator configuration arguments>
```

./tools/run.py --cores 2 -- --mode bi --format csv --scale-factor ${SF} --output-dir out-sf${SF}/ --explode-edges --epoch-millis  --format-options header=false,quoteAll=true,compression=gzip


mvn clean package && java -cp ./target/hotos-1.0-SNAPSHOT.jar Main -d
 60 -s postgres


## Env vars
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