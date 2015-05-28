name := "recommendation-simple"

version := "1.0"

libraryDependencies += "com.datastax.spark" %% "spark-cassandra-connector-assembly" % "1.3.0-SNAPSHOT"
libraryDependencies += "org.apache.spark" %% "spark-core" % "1.3.1" % "provided"
libraryDependencies += "org.apache.spark" % "spark-mllib_2.10" % "1.3.1" % "provided"
libraryDependencies += "com.google.guava" % "guava" % "18.0"

resolvers += Resolver.sonatypeRepo("public")
resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/dev/repos/maven"

//We do this so that Spark Dependencies will not be bundled with our fat jar but will still be included on the classpath
//When we do a sbt/run
run in Compile <<= Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run))
