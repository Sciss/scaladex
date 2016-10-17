import ScalaJSHelper._
import org.scalajs.sbtplugin.cross.CrossProject

scalafmtConfig in ThisBuild := Some(file(".scalafmt"))

lazy val akkaVersion = "2.4.11"
lazy val upickleVersion = "0.4.1"
lazy val scalatagsVersion = "0.6.1"
lazy val autowireVersion = "0.2.5"
lazy val h2Version = "1.4.191"

val nscalaTime = "com.github.nscala-time" %% "nscala-time" % "2.14.0"
val logback = "ch.qos.logback" % "logback-classic" % "1.1.7"

lazy val baseSettings = Seq(
  organization := "ch.epfl.scala.index",
  version := "0.2.0"
)

lazy val commonSettings = Seq(
    resolvers += Resolver.typesafeIvyRepo("releases"),
    scalaVersion := "2.11.8",
    // 2.12.0
    // missing
    //   play-ws
    scalacOptions := Seq(
      "-deprecation",
      "-encoding",
      "UTF-8",
      "-feature",
      "-unchecked",
      "-Xfatal-warnings",
      "-Xlint",
      "-Yinline-warnings",
      "-Yno-adapted-args",
      "-Yrangepos",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-unused-import",
      "-Ywarn-value-discard",
      "-Xmax-classfile-name",
      "78"
    ),
    scalacOptions in (Test, console) -= "-Ywarn-unused-import",
    scalacOptions in (Compile, consoleQuick) -= "-Ywarn-unused-import",
    libraryDependencies += "org.specs2" %% "specs2-core" % "3.8.6" % "test",
    scalacOptions in Test ++= Seq("-Yrangepos")
  ) ++ baseSettings ++
    addCommandAlias("start", "reStart")

lazy val template = project
  .settings(commonSettings)
  .settings(
    scalacOptions -= "-Ywarn-unused-import",
    libraryDependencies ++= Seq(
      nscalaTime,
      "com.typesafe" % "config" % "1.3.1",
      "com.typesafe.akka" %% "akka-http-core" % akkaVersion
    )
  )
  .dependsOn(model)
  .enablePlugins(SbtTwirl)

lazy val shared = crossProject
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "scalatags" % scalatagsVersion,
      "com.lihaoyi" %%% "upickle" % upickleVersion,
      "com.lihaoyi" %%% "autowire" % autowireVersion
    )
  )
lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

lazy val client = project.settings(commonSettings).enablePlugins(ScalaJSPlugin).dependsOn(sharedJs)

lazy val server = project
  .settings(commonSettings)
  .settings(packageScalaJS(client))
  .settings(
    libraryDependencies ++= Seq(
      logback,
      "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "ch.megard" %% "akka-http-cors" % "0.1.8",
      "com.softwaremill.akka-http-session" %% "core" % "0.2.7",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
      "org.webjars.bower" % "bootstrap-sass" % "3.3.6",
      "org.webjars.bower" % "bootstrap-switch" % "3.3.2",
      "org.webjars.bower" % "bootstrap-select" % "1.10.0",
      "org.webjars.bower" % "font-awesome" % "4.6.3",
      "org.webjars.bower" % "jQuery" % "2.2.4",
      "org.webjars.bower" % "select2" % "4.0.3",
      "com.lihaoyi" %%% "scalatags" % scalatagsVersion
    ),
    packageBin in Universal := (packageBin in Universal).dependsOn(WebKeys.assets in Assets).value,
    reStart := reStart.dependsOn(WebKeys.assets in Assets).evaluated,
    unmanagedResourceDirectories in Compile += (WebKeys.public in Assets).value,
    javaOptions in reStart += "-Xmx3g",
    packageName in Universal := "scaladex"
  )
  .dependsOn(template, data, sharedJvm)
  .enablePlugins(SbtSass, JavaServerAppPackaging)

lazy val model = project
  .settings(commonSettings)
  .settings(
    libraryDependencies += "com.lihaoyi" %% "fastparse" % "0.4.2"
  )

/*
sudo su -
psql postgres
CREATE DATABASE scaladex
GRANT ALL PRIVILEGES ON DATABASE "scaladex" to scaladex;
\c scaladex
\dt scaladex.*
*/

lazy val schemaName = "SCALADEX"
lazy val database = project
  .settings(commonSettings)
  .settings(slickCodegenSettings)
  .settings(
    scalacOptions -= "-Ywarn-unused-import",
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick"      % "3.1.0",
      "com.zaxxer"          % "HikariCP"   % "2.5.1",
      "org.postgresql"      % "postgresql" % "9.4-1201-jdbc41"
    ),
    slickCodegenDatabaseUrl      := (flywayUrl in flyway).value,
    slickCodegenDatabaseUser     := (flywayUser in flyway).value,
    slickCodegenDatabasePassword := (flywayPassword in flyway).value,
    slickCodegenDriver           := slick.driver.PostgresDriver,
    slickCodegenJdbcDriver       := "slick.jdbc.DatabaseUrlDataSource",
    slickCodegenOutputPackage    := "ch.epfl.scala.index.database.generated",
    slickCodegenExcludedTables   := Seq("schema_version"),
    sourceGenerators in Compile  += slickCodegen.taskValue
  ).dependsOn(flyway)

lazy val flyway = project
  .settings(commonSettings)
  .settings(
    flywaySchemas := Seq(schemaName),
    flywayLocations := {
      val dir = (resourceDirectory in Compile).value
      Seq(s"filesystem:$dir/db/migration")
    },
    flywayUrl := s"jdbc:h2:${baseDirectory.value.absolutePath}/.h2data;MODE=PostgreSQL",
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick" % "3.1.1",
      "com.h2database"      % "h2"    % h2Version
    )
  )

lazy val data = project
  .settings(commonSettings)
  .settings(
    resolvers += Resolver.bintrayRepo("hseeberger", "maven"),
    libraryDependencies ++= Seq(
      nscalaTime,
      logback,
      "com.sksamuel.elastic4s" %% "elastic4s-core" % "2.4.0",
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "org.json4s" %% "json4s-native" % "3.4.2",
      "me.tongfei" % "progressbar" % "0.4.0",
      "org.apache.maven" % "maven-model-builder" % "3.3.9",
      "org.jsoup" % "jsoup" % "1.10.1",
      "com.typesafe.play" %% "play-ws" % "2.5.9"
    ),
    buildInfoPackage := "build.info",
    buildInfoKeys := Seq[BuildInfoKey](baseDirectory in ThisBuild),
    javaOptions in reStart += "-Xmx3g"
  )
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(model, database)

// to publish plugin
// follow: http://www.scala-sbt.org/0.13/docs/Bintray-For-Plugins.html
// then add a new package ()
// name: sbt-scaladex, license: MIT, version control: git@github.com:scalacenter/scaladex.git
// to be available without a resolver
// follow: http://www.scala-sbt.org/0.13/docs/Bintray-For-Plugins.html#Linking+your+package+to+the+sbt+organization
lazy val sbtScaladex = project
  .settings(baseSettings)
  .settings(ScriptedPlugin.scriptedSettings)
  .settings(
    name := "sbt-scaladex",
    sbtPlugin := true,
    scalaVersion := "2.10.6",
    scriptedLaunchOpts := scriptedLaunchOpts.value ++ Seq("-Xmx1024M",
                                                          "-Dplugin.version=" + version.value),
    scriptedBufferLog := false,
    bintrayRepository := "sbt-plugins",
    bintrayOrganization := None,
    scalafmt := {}
  )
  .enablePlugins(BintrayPlugin)
