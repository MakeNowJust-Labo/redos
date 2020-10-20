Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / githubOwner := "MakeNowJust-Labo"
ThisBuild / githubRepository := "redos"

ThisBuild / scalaVersion := "2.13.3"
ThisBuild / scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-feature",
  "-deprecation",
  "-Wunused"
)

// Scalafix config:
ThisBuild / scalafixScalaBinaryVersion := "2.13"
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.4.2"
ThisBuild / scalafixDependencies += "com.github.vovapolu" %% "scaluzzi" % "0.1.14"

addCommandAlias("amm", "launchAmmoniteRepl")

lazy val root = project
  .in(file("."))
  .settings(
    organization := "codes.quine.labo",
    name := "redos",
    version := "0.1.0-SNAPSHOT",
    console / initialCommands := """
      |import codes.quine.labo.redos._
      |import codes.quine.labo.redos.automaton._
      |import codes.quine.labo.redos.data._
      |import codes.quine.labo.redos.regexp._
      |import codes.quine.labo.redos.unicode._
      |import codes.quine.labo.redos.util._
      |""".stripMargin,
    Compile / console / scalacOptions -= "-Wunused",
    Test / console / scalacOptions -= "-Wunused",
    // Settings for scaladoc:
    Compile / doc / scalacOptions += "-diagrams",
    // Set URL mapping of scala standard API for Scaladoc.
    apiMappings ++= scalaInstance.value.libraryJars
      .filter(file => file.getName.startsWith("scala-library") && file.getName.endsWith(".jar"))
      .map(_ -> url(s"http://www.scala-lang.org/api/${scalaVersion.value}/"))
      .toMap,
    // Dependencies:
    libraryDependencies += "com.ibm.icu" % "icu4j" % "67.1",
    libraryDependencies += "com.lihaoyi" %% "fastparse" % "2.3.0",
    // Settings for test:
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.14" % Test,
    testFrameworks += new TestFramework("munit.Framework")
  )
