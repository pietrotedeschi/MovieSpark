#!/bin/bash

#Launch Script with sudo !

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