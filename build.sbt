import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.3.6"

/** --------------------------------
 * Compiler options
 * --------------------------------
 */
ThisBuild / scalacOptions ++= Seq(
  "-Wconf:msg=Flag.*repeatedly:s",
  "-Wconf:src=routes/.*:s" // suppress warnings in generated routes files
)

/** --------------------------------
 * Scalafmt
 * --------------------------------
 */
ThisBuild / scalafmtOnCompile := true
ThisBuild / scalafmtSbt := true

/** --------------------------------
 * Main service
 * --------------------------------
 */
lazy val microservice = Project("vaping-stamps-api", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources"
  )
  .settings(CodeCoverageSettings.settings: _*)

/** --------------------------------
 * Integration tests
 * --------------------------------
 */
lazy val it = project
  .enablePlugins(play.sbt.PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(
    libraryDependencies ++= AppDependencies.it
  )
