mergeStrategy in assembly := {
  case PathList("META-INF", "ECLIPSEF.RSA") => MergeStrategy.discard
  case x => {
    val oldStrategy = (mergeStrategy in assembly).value
    oldStrategy(x)
  }
}
