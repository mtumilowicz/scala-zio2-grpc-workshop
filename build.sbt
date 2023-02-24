ThisBuild / scalaVersion     := "2.13.10"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

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
  ).dependsOn(`document-api-grpc`, `document-service`)

lazy val `document-api-grpc` = (project in file("document-api-grpc"))
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
  .dependsOn(`document-api-grpc`)