To start:

```sbt assembly```

```spark-submit --class GetMostRatedMovies target/scala-2.10/recommendation-simple-assembly-1.0.jar --driver-memory 4g```

```spark-submit --class AdvancedRecommendation target/scala-2.10/recommendation-simple-assembly-1.0.jar --driver-memory 4g```


Requires a Spark Cluster with enough resources.

I.e: set driver memory and executor memory to at least 3g

Driver Memory: set in conf/spark-default.conf or as --driver-memory 3g on the spark-submit
Executor Memory: set in conf/spark-default.conf or as spark.executor.memory in your app (SparkConf())
