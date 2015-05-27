## Overview
The are three tables with data after doing the first steps:
- movies_raw
- ratings_by_user
- tags_by_user

## movies_raw
```
CREATE TABLE movies_raw (
  movieid bigint,
  genres text,
  title text,
  PRIMARY KEY ((movieid))
)
```

The title consists of the title and the year in brackets. The genres are a list, separated by pipes. We want the year in a separate field and the genres as a set.

### 1. CQLSH 

Create table if not exists

```
CREATE TABLE movies (
  movieid bigint,
  genres set<text>,
  title text,
  year int,
  PRIMARY KEY ((movieid))
)
```

### 2. Spark Shell

Start Shell

    ./bin/spark-shell --jars ~/path/to/jar/spark-cassandra-connector-assembly-1.3.0-SNAPSHOT.jar --conf spark.cassandra.connection.host=localhost --driver-memory 3g

Import Cassandra Classes

    import com.datastax.spark.connector._, org.apache.spark.SparkConf, org.apache.spark.SparkContext, org.apache.spark.SparkContext._

Create Case Classes

    case class MovieRaw(movieid:Long,genres:String,title:String)
    case class Movie(movieid:Long,genres:Set[String],title:String,year:Int)

Load Movies From Cassandra

    val moviesRaw = sc.cassandraTable[MovieRaw]("movie","movies_raw")

Regex for getting the year

    val regexYear = ".*\\((\\d*)\\)".r

Transform the Raw Movies to the desired format

    val movies = moviesRaw.map{case MovieRaw(i,g,t) => Movie(i,g.split("\\|").toSet,t.substring(0,t.lastIndexOf('(')).trim,t.trim match { case regexYear(y) => Integer.parseInt(y)})}

Save the transformed movies to Cassandra

    movies.saveToCassandra("movie","movies")

## ratings_by_user

We want a ratings_by_movie table

### 1. CQLSH

    CREATE TABLE ratings_by_movie(movieid bigint,userid bigint,rating double,time timestamp,   PRIMARY KEY((movieid),userid) ) ;

### 2. Spark Shell

Start Spark Shell and import classes (see above)

Create Case Class, Read and Write

    case class Rating (userid:Long, movieid:Long, rating:Double, time: java.util.Date)
    val ratingsByUser = sc.cassandraTable[Rating]("movie","ratings_by_user")
    ratingsByUser.saveToCassandra("movie","ratings_by_movie")

## tags_by_user

We want a tags_by_movie table

### 1. CQLSH

    CREATE TABLE tags_by_movie(movieid bigint, userid bigint, tag text, time timestamp, PRIMARY KEY ((movieid), userid, tag, time));

### 2. Spark Shell

Start Spark Shell and import classes (see above)

Create Case Class, Read and Write

    case class Tag(userid:Long,movieid:Long,tag:String,time:java.util.Date)
    val tagsByUser = sc.cassandraTable[Tag]("movie","tags_by_user")
    tagsByUser.saveToCassandra("movie","tags_by_movie")