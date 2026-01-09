name := "exam-backend"

version := "1.0.0"

scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies ++= Seq(
      guice,
      "org.postgresql" % "postgresql" % "42.6.0",
      "com.typesafe.play" %% "play-slick" % "5.1.0",
      "com.typesafe.play" %% "play-slick-evolutions" % "5.1.0",
      "net.logstash.logback" % "logstash-logback-encoder" % "7.4" % Optional,
      "org.scalatestplus.play" %% "scalatestplus-play" % "6.0.0" % Test
    ),
    // Force logback version compatible with Play 2.8.19
    // Play 2.8.19 requires configureByResource(URL) which was removed in logback 1.4.9+
    // Use 1.4.8 which is the last version with this method
    dependencyOverrides ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.4.8",
      "ch.qos.logback" % "logback-core" % "1.4.8"
    )
  )
