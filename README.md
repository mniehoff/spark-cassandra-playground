# spark-cassandra-playground

Playground for Spark anlytics on data stored in a cassandra database. Used for demo purposes.

## Data Used
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
- Tarball installation
- Datastax Enterprise installation
- Cassandra Cluster Manager (https://github.com/pcmanus/ccm)

Example with the CCM:
- Creates Cluster "moviedb" with 3 Nodes, using DSE v4.6.5, Opscenter in Version 5.1.1, starts the cluster

        ccm create --dse -v 4.6.5 --dse-username=<<dseUserName>> --dse-password=<<dsePassword>> -o 5.1.1 -n 3 moviedb -s

- Creates Cluster "moviedb" with 3 Nodes, using Cassandra 2.1.5, starts the Cluster

        ccm create moviedb -v binary:2.1.6 -n 3 -s

On a Mac you first have to alias the loopback adapter:

```
sudo ifconfig lo0 alias 127.0.0.2
sudo ifconfig lo0 alias 127.0.0.3
```

### Spark

Just grap the "Prebuild for Hadoop 2.6" tarball from https://spark.apache.org/downloads.html, untar it and you are done

### Spark Cassandra Connector

The Spark Cassandra Connector is written by Datastax and is found [here at github](https://github.com/datastax/spark-cassandra-connector). Please beware of the version compatibility. At the time of writing the master is at the 1.3-snapshot which is working fine with Spark 1.3.1 core, but not with Spark SQL 1.3.1. If you want to use all features you probably want to use Spark 1.2.x with the connector 1.2.x.

1. Clone Git Repo from https://github.com/datastax/spark-cassandra-connector, Change into dir
2. Checkout the Branch you want use (e.g. b1.2)
3. Build Assembly Jar with `./sbt/sbt assembly`

Copy the assembly jar found under spark-cassandra-connector-java/target/scala-2.10 to a place you will find it easily.

## Data & Datastructures

Start a CQL Sessions from the repository root, e.g with ```ccm node1 cqlsh```

### Cassandra Keyspaces & Tables

Create the Cassandra Keyspace and Tables using the provided schema.cql

```SOURCE './schema.cql'```
```USE movie```

### Load the data

- Load the latest dataset from the link above and copy the expanded file to <repository root>/data-original
- For the movies DON'T use the Data from the project, but the changed dataset provided in this repo: `data-changed/movies.zip` I added some years and fixed some formatting issues in this file. Just unzip the file.
- Import the data using the COPY Command. The ratings might need up to 2 hrs for import.

        COPY movies_raw (movieid, title, genres) FROM 'data-changed/movies.csv' with header= true;
        COPY tags_by_user FROM 'data-original/tags.csv' WITH header=true;
        COPY ratings_by_user FROM 'data-original/ratings.csv' with header=true 

Now all the data you need is inside your cassandra cluster and you are ready to go!

## Spark

To start the spark shell switch into the spark install dir and run 

```./bin/spark-shell --jars ~/path/to/jar/spark-cassandra-connector-assembly-1.3.0-SNAPSHOT.jar --conf spark.cassandra.connection.host=localhost --driver-memory 3g ```

We need 3g of memory for caching large ratings rdds. If your Cassandra cluster is not running in localhost change the parameter accordingly.

Then import the Cassandra Spark Classes:

```import com.datastax.spark.connector._, org.apache.spark.SparkConf, org.apache.spark.SparkContext, org.apache.spark.SparkContext._````

For some examples to use Spark on this data refer to the wiki, e.g.

* [Modify and Transform the data for further analytics](https://github.com/mniehoff/spark-cassandra-playground/wiki/Modify-and-Transform-the-data-for-further-analytics)
* [Basic analytics](https://github.com/mniehoff/spark-cassandra-playground/wiki/Basic-analytics)

## TODO

- More automation
