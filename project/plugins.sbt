addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.6")

libraryDependencies ++= Seq(
  "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-codegen" % "0.6.0-rc1",
  "com.thesamet.scalapb" %% "compilerplugin" % "0.11.13"
)
