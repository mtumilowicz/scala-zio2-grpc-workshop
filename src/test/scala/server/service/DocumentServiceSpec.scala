package server.service

import client.DocumentClient
import io.grpc.document.document.{DocumentIdApiInput, NewDocumentApiInput}
import io.grpc.document.document.ZioDocument.DocumentGrpcServiceClient
import zio.{Scope, ZIO}
import zio.stream.ZStream
import zio.test.{Spec, TestEnvironment, ZIOSpecAbstract, ZIOSpecDefault, assertTrue}

object DocumentServiceSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] = suite("crud")(
    test("empty get") {
      for {
        service <- ZIO.service[DocumentService]
        _ <- service.createDocument(NewDocumentApiInput("1"))
        all <- service.getDocuments(ZStream.from(DocumentIdApiInput("1"))).runCollect
      } yield assertTrue(all.size == 1)
    }
  ).provideLayer(DocumentService.live)
}
