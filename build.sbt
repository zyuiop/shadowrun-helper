ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.6"

lazy val root = (project in file("."))
  .settings(
    name := "shadowrun-helper",
    resolvers += Resolver.mavenLocal,
    libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.2",
    libraryDependencies += "com.googlecode.lanterna" % "lanterna" % "3.2.0-SNAPSHOT",
  )
