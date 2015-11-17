organization := "edu.cmu.ml.rtw"

name := "one-off"

version := "1.0"

scalaVersion := "2.11.2"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

javacOptions ++= Seq("-Xlint:unchecked", "-source", "1.7", "-target", "1.7")

crossScalaVersions := Seq("2.11.2", "2.10.3")

libraryDependencies ++= Seq(
  "edu.cmu.ml.rtw" %% "pra" % "3.1-SNAPSHOT",
  "edu.cmu.ml.rtw" %% "matt-util" % "1.1"
)

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD")

instrumentSettings

jacoco.settings

jacoco.reportFormats in jacoco.Config := Seq(
  de.johoop.jacoco4sbt.ScalaHTMLReport(encoding = "utf-8", withBranchCoverage = true))
