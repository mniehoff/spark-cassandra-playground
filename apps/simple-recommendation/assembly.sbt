import AssemblyKeys._ 

assemblySettings

mergeStrategy in assembly := {
  case PathList("META-INF", "ECLIPSEF.RSA") => MergeStrategy.discard
  case x => {
    val oldStrategy = (mergeStrategy in assembly).value
    oldStrategy(x)
  }
}

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.9.2")