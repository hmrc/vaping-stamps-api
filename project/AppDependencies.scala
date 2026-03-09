import sbt._

object AppDependencies {

  private val bootstrapVersion = "10.6.0"

  // Added: explicit test library versions (Scala 3 compatible)
  private val scalaTestVersion = "3.2.18"
  private val scalaTestPlusPlay = "7.0.1"
  private val scalaTestPlusMockito = "3.2.18.0"
  private val mockitoCoreVersion = "5.11.0"

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-30" % bootstrapVersion
  )

  val test = Seq(
    // HMRC bootstrap test bundle (Scala 3 safe)
    // Includes ScalaTest, Play test helpers, Mockito core
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test,
    // ScalaTest support for integration tests
    "org.scalatest" %% "scalatest" % "3.2.18" % Test,
    // Play test helpers for integration tests
    "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test
  )

  val it = Seq.empty
}
