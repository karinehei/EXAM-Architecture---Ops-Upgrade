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
    )
  )
