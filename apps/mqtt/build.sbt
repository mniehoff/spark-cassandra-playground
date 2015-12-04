name := "mqtt"

version := "1.0"

libraryDependencies += "com.datastax.spark" %% "spark-cassandra-connector" % "1.5.0-M2"
libraryDependencies += "org.apache.spark" %% "spark-core" % "1.5.2" % "provided"
libraryDependencies += "org.apache.spark" % "spark-streaming_2.10" % "1.5.2" % "provided"
libraryDependencies += "org.apache.spark" % "spark-streaming-mqtt_2.10" % "1.5.2" % "provided"
libraryDependencies += "com.google.guava" % "guava" % "18.0"

resolvers += Resolver.sonatypeRepo("public")
resolvers += "Paho Releases" at "https://repo.eclipse.org/content/repositories/paho-releases"
resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/dev/repos/maven"

//We do this so that Spark Dependencies will not be bundled with our fat jar but will still be included on the classpath
//When we do a sbt/run
run in Compile <<= Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run))
