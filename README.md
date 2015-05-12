# spark-cassandra-playground

Playground for Spark anlytics on data stored in a cassandra database. Used for demo purposes.

## Used Data
The project uses the data of the [movielens project](http://www.movielens.org), a non commercial movie recommendations site 

You can find different datasets of the project here: http://grouplens.org/datasets/movielens/

For this project I used the latest dataset (as of April 2015): http://files.grouplens.org/datasets/movielens/ml-latest.zip

## Set Up 

### Cassandra
Install Cassandra as you like 
- with as much nodes as you want
- optional with the opscenter
- maybe you don't want to use vnodes, as these slow down performance together with spark

Possiblities to install:
- tarball installation
- Datastax Enterprise installation
- Cassandra Cluster Manager (https://github.com/pcmanus/ccm)

Example with the CCM:
- Creates Cluster "moviedb" with 3 Nodes, using DSE v4.6.5, Opscenter in Version 5.1.1, starts the cluster

        ccm create --dse -v 4.6.5 --dse-username=<<dseUserName>> --dse-password=<<dsePassword>> -o 5.1.1 -n 3 moviedb -s

- Creates Cluster "moviedb" with 3 Nodes, using Cassandra 2.1.6, starts the Cluster

        ccm create moviedb -v binary:2.1.6 -n 3 -s

On Mac you first have to alias the loopback adapter:

```
sudo ifconfig lo0 alias 127.0.0.2
sudo ifconfig lo0 alias 127.0.0.3
```

### Spark

Just grap the "Prebuild for Hadoop 2.6" tarball from https://spark.apache.org/downloads.html, untar it and you are done

### Spark Cassandra Connector

The Spark Cassandra Connector is written by Datastax and found [here at github](https://github.com/datastax/spark-cassandra-connector). Please beware of the version compatibility. At time of writing the master is the 1.3 Snapshot which is working fine with Spark 1.3.1 core, but not with Spark SQL 1.3.1. If you want to use all features you probably want to use spark 1.2.x with the connector 1.2.x.

1. Clone Git Repo from https://github.com/datastax/spark-cassandra-connector, Change into dir
2. Checkout the Branch you want use (e.g. b1.2)
3. Build Assembly Jar with `./sbt/sbt assembly`

Copy the assembly jar found under spark-cassandra-connector-java/target/scala-2.10 to a place you fill find it fast,

## Data & Datastructures

Get up the Datastructures and import the data from the movielens project




## TODO

- More automation
