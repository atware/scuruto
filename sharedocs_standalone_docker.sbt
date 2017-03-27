mainClass in assembly := Some("skinny.standalone.JettyLauncher")
_root_.sbt.Keys.test in assembly := {}
resourceGenerators in Compile <+= (resourceManaged, baseDirectory) map { (managedBase, base) =>
  val webappBase = base / "src" / "main" / "webapp"
  for ( (from, to) <- webappBase ** "*" `pair` rebase(webappBase, managedBase / "main/") )
  yield {
    Sync.copy(from, to)
    to
  }
}

dockerfile in docker := {
  val artifact: File = assembly.value
  val artifactTargetPath = s"/app/${artifact.name}"

  new Dockerfile {
    from("openjdk:8-jre-alpine")
    add(artifact, artifactTargetPath)
    entryPoint("java", "-jar", "-Dskinny.env=production", artifactTargetPath)
  }
}

imageNames in docker := Seq(
  ImageName(s"sharedocs/${name.value}:latest"),
  ImageName(
    namespace = Some("sharedocs"),
    repository = name.value,
    tag = Some("v" + version.value)
  )
)
