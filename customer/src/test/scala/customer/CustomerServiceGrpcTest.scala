package customer

import document.domain.{Document, DocumentId, DocumentService}
import document.infrastructure.{DocumentGrpcClient, DocumentRepositoryConfig, DocumentServiceContainer}
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}
import zio.{Scope, ZIO}

object CustomerServiceGrpcTest extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] = suite("crud")(
    test("create and get") {
      for {
        customerService <- ZIO.service[CustomerService]
        _ <- customerService.createDocument(CustomerId("1"), Document(DocumentId("1"), "content"))
        result <- customerService.findAllDocuments(CustomerId("1")).runCollect
      } yield assertTrue(result.size == 1)
    }
  ).provideSome[CustomerAppConfig](
    CustomerService.layer,
    DocumentRepositoryConfig.grpc,
    DocumentGrpcClient.live,
    DocumentService.layer
  ).provideSomeShared(DocumentServiceContainer.live, TestAppConfig.live)

}
