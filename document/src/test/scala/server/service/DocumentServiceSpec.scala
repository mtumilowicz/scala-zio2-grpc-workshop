package server.service

import io.grpc.document.document.{DocumentIdApiInput, NewDocumentApiInput}
import zio.stream.ZStream
import zio.{Scope, ZIO}
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

object DocumentServiceSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] = suite("crud")(
    test("create and get") {
      for {
        customerService <- ZIO.service[DocumentService]
        _ <- customerService.createDocument(NewDocumentApiInput("1", "content"))
        result <- customerService.getDocuments(ZStream.from(DocumentIdApiInput("1"))).runCollect
      } yield assertTrue(result.size == 1)
    }
  ).provide(DocumentService.live)
}
