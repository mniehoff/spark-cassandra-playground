## Build & Publish newest Connector

```
cd /Users/matthiasniehoff/dev/repos/git/spark-cassandra-connector
sbt package
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file  -Dfile=spark-cassandra-connector/target/scala-2.10/spark-cassandra-connector_2.10-1.3.0-SNAPSHOT.jar \
                                                                              -DgroupId=com.datastax.spark \
                                                                              -DartifactId=spark-cassandra-connector_2.10 \
                                                                              -Dversion=1.3.0-SNAPSHOT \
                                                                              -Dpackaging=jar 
```

```
sbt assembly
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file  -Dfile=spark-cassandra-connector/target/scala-2.10/spark-cassandra-connector-assembly-1.3.0-SNAPSHOT.jar\
                                                                              -DgroupId=com.datastax.spark \
                                                                              -DartifactId=spark-cassandra-connector-assembly_2.10 \
                                                                              -Dversion=1.3.0-SNAPSHOT \
                                                                              -Dpackaging=jar 
                                                                              ```
																		 
