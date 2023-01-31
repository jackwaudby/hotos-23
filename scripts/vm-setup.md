yes | sudo apt-get update &&
yes | sudo apt install build-essential
yes | sudo apt install openjdk-17-jdk openjdk-17-jre
yes | sudo apt install zip
wget http://www.neo4j.com/customer/download/neo4j-enterprise-5.4.0-unix.tar.gz
tar zxf neo4j-enterprise-5.4.0-unix.tar.gz
./bin/neo4j-admin server license --accept-commercial && ./bin/neo4j-admin server license --accept-evaluation
sed -i '/dbms.security.auth_enabled=true/ c\dbms.security.auth_enabled=false' conf/neo4j.conf 

git clone https://github.com/ldbc/ldbc_snb_datagen_spark.git

curl -s "https://get.sdkman.io" | bash
source "/home/azureuser/.sdkman/bin/sdkman-init.sh"
sdk install sbt

yes | sudo apt-get update; sudo apt-get install make build-essential libssl-dev zlib1g-dev \
libbz2-dev libreadline-dev libsqlite3-dev wget curl llvm \
libncursesw5-dev xz-utils tk-dev libxml2-dev libxmlsec1-dev libffi-dev liblzma-dev
curl https://pyenv.run | bash
export PATH="$HOME/.pyenv/bin:$PATH"
eval "$(pyenv init --path)"
eval "$(pyenv virtualenv-init -)"
exec $SHELL

pyenv install 3.7.13

./tools/run.py --cores 2 -- --mode bi --format csv --scale-factor ${SF} --output-dir out-sf${SF}/ --explode-edges --epoch-millis  --format-options header=false,quoteAll=true,compression=gzip