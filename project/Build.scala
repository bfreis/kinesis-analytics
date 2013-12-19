import sbt._
import Keys._

object Build extends sbt.Build {
  val df = new java.text.SimpleDateFormat("yyyyMMdd-HHmmss")
  val buildTime = df.format(new java.util.Date())
  val appVersion = "0.1-%s".format(buildTime)

  val commonDependencies = Seq(
    "com.google.code.findbugs" % "jsr305" % "2.0.2",
    "com.google.guava" % "guava" % "15.0",
    "com.google.code.gson" % "gson" % "2.2.4",
    "com.amazonaws" % "aws-java-sdk" % "1.6.10",
    "com.amazonaws" % "amazon-kinesis-client" % "1.0.0"
  )
  val collectorDependencies = Seq()
  val dashboardDependencies = Seq()

  val scalaBuildOptions = Seq("-unchecked", "-deprecation", "-feature", "-language:reflectiveCalls")

  val common = play.Project("common", appVersion, commonDependencies, path = file("modules/common")).settings(
    scalacOptions ++= scalaBuildOptions,
    sources in doc in Compile := List(),
    javaOptions in Test += "-Dconfig.resource=common-application.conf"
  )

  val dashboard = play.Project("dashboard", appVersion, dashboardDependencies, path = file("modules/dashboard"))
    .settings(com.github.play2war.plugin.Play2WarPlugin.play2WarSettings : _*)
    .settings(com.github.play2war.plugin.Play2WarKeys.servletVersion := "3.0")
    .settings(
    scalacOptions ++= scalaBuildOptions,
    sources in doc in Compile := List(),
    javaOptions in Test += "-Dconfig.resource=dashboard-application.conf"
  ).dependsOn(common % "test->test;compile->compile").aggregate(common)

  val collector = play.Project("collector", appVersion, collectorDependencies, path = file("modules/collector"))
    .settings(com.github.play2war.plugin.Play2WarPlugin.play2WarSettings : _*)
    .settings(com.github.play2war.plugin.Play2WarKeys.servletVersion := "3.0")
    .settings(
    scalacOptions ++= scalaBuildOptions,
    sources in doc in Compile := List(),
    javaOptions in Test += "-Dconfig.resource=collector-application.conf"
  ).dependsOn(common % "test->test;compile->compile").aggregate(common)


  val aaaRoot = play.Project("root", appVersion, commonDependencies ++ collectorDependencies ++ dashboardDependencies).settings(
    scalacOptions ++= scalaBuildOptions,
    sources in doc in Compile := List()
  ).dependsOn(common % "test->test;compile->compile", collector % "test->test;compile->compile", dashboard % "test->test;compile->compile").aggregate(common, collector, dashboard)
}
