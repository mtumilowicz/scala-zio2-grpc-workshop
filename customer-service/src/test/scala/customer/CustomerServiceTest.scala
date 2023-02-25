package customer

import document.domain.{Document, DocumentId, DocumentRepository, DocumentService}
import document.infrastructure.DocumentServiceContainer
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
  ).provide(
    CustomerService.layer,
    DocumentRepository.grpc,
    DocumentRepository.grpcClient,
    DocumentService.layer)
    .provideShared(DocumentServiceContainer.live)
}
