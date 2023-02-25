
* references
    * https://github.com/scalapb/zio-grpc
    * https://zio.dev/ecosystem/community/zio-grpc/
    * https://scalapb.github.io/zio-grpc/
    * https://medium.com/rahasak/asynchronous-microservices-with-zio-grpc-and-scala-2e6519cb4e9a
    * [Functional, Type-safe, Testable Microservices with ZIO gRPC](https://www.youtube.com/watch?v=XTkhxRTH1nE)
    * https://gitlab.com/rahasak-labs/zrpc
    * https://github.com/grpc/grpc-java/issues/515

* https://github.com/grpc/grpc-java/issues/515#issuecomment-110023227
    * Status is for a canonical status used by all gRPC servers and will only rarely be added to. I think you saw the comment "If new codes are added over time they must choose a numerical value that does not collide with any previously used value." This comment is not referring to applications adding codes, but gRPC as a whole adding new codes. The current set of codes should be able to appropriately describe almost any application status in a generic way.

      For custom status information, use Metadata.

* show that streaming is possible
    * .run(ZSink.foreach(d => zio.Console.printLine(d)))
    *   override def getDocuments(request: stream.Stream[Status, DocumentIdApiInput]): stream.Stream[Status, DocumentApiOutput] = {
          ZStream.from(DocumentApiOutput("2", "content2"))
            .repeat(Schedule.spaced(2.seconds))
        }