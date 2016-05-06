## Development of a Distributed Web Movie Recommendation System using Apache Spark, Apache Kafka, Node.JS and Complex Event Processing.

## Introduction
We have developed **MovieSpark**, a movie recommendation system that recommends movies to user that he/she might be interested in. Among the techniques used in building recommendation systems, we used [Collaborative Filtering](https://en.wikipedia.org/wiki/Collaborative_filtering) that is one of the most promising approaches. The aim of this project, is to build a Distributed Movie Recommendation System to analyze and process the real-time data streams and manage the [Complex Events](https://en.wikipedia.org/wiki/Complex_event_processing) with a Query Language. The system can be used with a web interface that allow users to vote and view the recommended movies.

## System Architecture
![alt tag](https://github.com/Seldonm/relazione-isa-lia/blob/master/images/cephw.png)

## Requirements
- Unix-like Operating System
- [IntelliJ IDEA with Scala and SBT Plugin] (https://www.jetbrains.com/idea/download/#section=linux)
- [Apache Spark (Pre-built for Hadoop 2.6 and later)] (http://spark.apache.org/downloads.html)
- [Apache Kafka 0.9.0.1 for Scala 2.10] (http://kafka.apache.org/downloads.html)
- [Node.js 5.10.1] (https://nodejs.org/en/download/package-manager/)
- [KafkaMovieWS] (../../../KafkaMovieWS)

## Installation and Configuration
- To get started, open your `home` directory and download the project with `git`:
```
git clone https://github.com/pietrotedeschi/MovieSpark
```
- Import **SBT Project** with IntelliJ IDEA, and install all dependencies.
- Create SBT Tasks on Intellij (Run-->Edit Configurations-->Add SBT Task)

**First Task**
```
Name: clean
Tasks: clean
```
**Second Task**
```
Name: assembly
Tasks: assembly
```
**Third Task**
```
Name: submit
Tasks: "sparkSubmit --class ml.Main"
```

- In your `home` directory, download the Web App KafkaMovieWS with `git`:
```
git clone https://github.com/pietrotedeschi/KafkaMovieWS
```
- Configure `config/server.properties` file of Apache Kafka:
```bash
zookeeper.connect = localhost:2181
advertised.host.name = your_hostname OR ip
```
- If you want to run MovieSpark in Cluster Mode, you must configure the `conf/spark-env.sh` file of Apache Spark:
```bash
export SPARK_WORKER_CORES = n
export SPARK_WORKER_MEMORY = mg
export SPARK_MASTER_IP = X . X . X . X
export SPARK_MASTER_PORT =7077
export MASTER = spark :// $ { SPARK_MASTER_IP }: $ { SPARK_MASTER_PORT }
```
Please read the [Spark Documetation](http://spark.apache.org/docs/latest/configuration.html) to understand how to configure this file.
- Open `start.sh` and configure it with your parameters.
```bash
#!/bin/bash
#Run Script with sudo !

SCALA_VERSION="2.10"
KAFKA_VERSION="0.9.0.1"

#Directory Path of NodeJS Script
NODE_SCRIPT_DIR="kafka_socket"

#I supposed that Kafka Installation Directory is located in /home/<user>
KAFKA_HOME=$HOME/kafka_$SCALA_VERSION-$KAFKA_VERSION

#Start Zookeeper and Kafka Server as daemon
echo "Starting Zookeeper and Kafka Server..."
$KAFKA_HOME/bin/zookeeper-server-start.sh -daemon $KAFKA_HOME/config/zookeeper.properties
sleep 5
$KAFKA_HOME/bin/kafka-server-start.sh -daemon $KAFKA_HOME/config/server.properties
sleep 5

#Create Topics
echo "Creating Topics..."
$KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic votes
$KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic result

#Start node.js App
echo "Starting App on localhost:3000"
node $HOME/$NODE_SCRIPT_DIR/app.js --port 3000
```
## Running (Local Mode)
1. Open terminal, and run the script `start.sh` with `sudo start.sh`.
2. Running project from IntelliJ (select Assembly Task).
3. Open the Web Dashboard on `localhost:3000`.

### Running (Cluster Mode)
1. **Master Node**: Open terminal, enter in the `root` directory of Spark and digit: `/sbin/start-master.sh`.
2. **Worker Node**: Open terminal, enter in the `root` directory of Spark and digit: `/sbin/start-slave.sh spark://SPARK_MASTER_IP:SPARK_MASTER_PORT`.
3. Run the script `start.sh` with `sudo start.sh`.
4. Open the Web Dashboard on `localhost:3000`.

## Authors
* [Pietro Tedeschi] (https://it.linkedin.com/in/pietrotedeschi)
* [Mauro Losciale] (https://it.linkedin.com/in/maurolosciale)
