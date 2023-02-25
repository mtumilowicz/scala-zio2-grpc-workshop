ThisBuild / scalaVersion     := "2.13.10"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"
ThisBuild / Test / fork := true

Compile / PB.targets := Seq(
  scalapb.gen(grpc = true) -> (Compile / sourceManaged).value,
  scalapb.zio_grpc.ZioCodeGenerator -> (Compile / sourceManaged).value
)

lazy val root = (project in file("."))
  .settings(
    name := "scala-zio2-grpc-workshop",
    libraryDependencies ++= Seq(
      "io.grpc" % "grpc-netty" % "1.53.0",
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
      "dev.zio" %% "zio" % "2.0.9",
      "dev.zio" %% "zio-test" % "2.0.9" % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )

lazy val `document-api-grpc` = (project in file("document-service/document-api-grpc"))
  .settings(
    libraryDependencies ++= Seq(
      "io.grpc" % "grpc-netty" % "1.53.0",
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
      "dev.zio" %% "zio" % "2.0.9",
      "dev.zio" %% "zio-test" % "2.0.9" % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
  .settings(Compile / PB.targets := Seq(
    scalapb.gen(grpc = true) -> (Compile / sourceManaged).value,
    scalapb.zio_grpc.ZioCodeGenerator -> (Compile / sourceManaged).value
  ))

lazy val `document-service` = (project in file("document-service"))
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "io.grpc" % "grpc-netty" % "1.53.0",
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
      "dev.zio" %% "zio" % "2.0.9",
      "dev.zio" %% "zio-test" % "2.0.9" % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
  .settings(Compile / PB.targets := Seq(
    scalapb.gen(grpc = true) -> (Compile / sourceManaged).value,
    scalapb.zio_grpc.ZioCodeGenerator -> (Compile / sourceManaged).value
  ))
  .settings(
    Docker / packageName := "document-service",
    dockerBaseImage := "openjdk:11-jre-slim-buster",
    dockerExposedPorts ++= Seq(8080),
    dockerUpdateLatest := true,
  )
  .dependsOn(`document-api-grpc`)

lazy val `customer-service` = (project in file("customer-service"))
  .settings(
    libraryDependencies ++= Seq(
      "io.grpc" % "grpc-netty" % "1.53.0",
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
      "dev.zio" %% "zio" % "2.0.9",
      "dev.zio" %% "zio-config" % "3.0.7",
      "dev.zio" %% "zio-config-magnolia" % "3.0.7",
      "dev.zio" %% "zio-config-typesafe" % "3.0.7",
      "com.dimafeng" %% "testcontainers-scala-scalatest" % "0.40.12",
      "dev.zio" %% "zio-test" % "2.0.9" % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
  .settings(Compile / PB.targets := Seq(
    scalapb.gen(grpc = true) -> (Compile / sourceManaged).value,
    scalapb.zio_grpc.ZioCodeGenerator -> (Compile / sourceManaged).value
  ))
  .dependsOn(`document-api-grpc`)