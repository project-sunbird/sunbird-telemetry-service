import sbt._
import Keys._
import play.Play.autoImport._
import PlayKeys._
import play.sbt._

object ApplicationBuild extends Build {

    val core = Project("telemetry-api-core", file("telemetry-api-core"))
        .settings(
            version := Pom.version(baseDirectory.value),
            libraryDependencies ++= Pom.dependencies(baseDirectory.value))

    val root = Project("telemetry-api", file("telemetry-api"))
        .dependsOn(core)
        .settings(
            version := Pom.version(baseDirectory.value),
            libraryDependencies ++= Pom.dependencies(baseDirectory.value).filterNot(d => d.name == core.id))

    override def rootProject = Some(root)
}