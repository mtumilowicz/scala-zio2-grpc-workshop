package customer

import document.domain.{Document, DocumentId, DocumentRepository, DocumentService}
import document.infrastructure.{DocumentGrpcClient, DocumentRepositoryConfig, DocumentServiceContainer}
import zio.stream.ZStream
import zio.{Scope, ZIO, ZLayer}
import zio.test.{Spec, TestEnvironment, ZIOSpecAbstract, ZIOSpecDefault, assertTrue}

object CustomerServiceTest extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] = suite("crud")(
    test("create and get") {
      for {
        customerService <- ZIO.service[CustomerService]
        _ <- customerService.createDocument(CustomerId("1"), Document(DocumentId("1"), "content"))
        result <- customerService.findAllDocuments(CustomerId("1")).runCollect
      } yield assertTrue(result.size == 1)
    },
    test("create and get") {
      for {
        customerService <- ZIO.service[CustomerService]
        _ <- customerService.createDocument(CustomerId("1"), Document(DocumentId("2"), "content"))
        result <- customerService.findAllDocuments(CustomerId("1")).runCollect
      } yield assertTrue(result.size == 1)
    }
  ).provideSome[Scope with CustomerAppConfig](
    CustomerService.layer,
    DocumentRepositoryConfig.grpc,
    DocumentGrpcClient.live,
    DocumentService.layer
  )
    .provideSomeShared(DocumentServiceContainer.live, testGrpcConfig)

  val testGrpcConfig = ZLayer.fromZIO {
    for {
      orderTestContainer <- ZIO.service[DocumentServiceContainer]
    } yield CustomerAppConfig.from(orderTestContainer)
  }
}
