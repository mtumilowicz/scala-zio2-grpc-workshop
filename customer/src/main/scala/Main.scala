import customer.{CustomerAppConfig, CustomerId, CustomerService}
import document.domain.{Document, DocumentId, DocumentService}
import document.infrastructure.{DocumentGrpcClient, DocumentRepositoryConfig}
import zio.ZIO
import zio.stream.ZSink

object Main extends zio.ZIOAppDefault {
  def createAndGet =
    for {
      customerService <- ZIO.service[CustomerService]
      _ <- customerService.createDocument(CustomerId("1"), Document(DocumentId("1"), "content"))
      result <- customerService.findAllDocuments(CustomerId("1")).run(ZSink.foreach(d => zio.Console.printLine(d)))
    } yield result

  final def run =
    createAndGet.provide(
      CustomerService.layer,
      DocumentRepositoryConfig.grpc,
      DocumentGrpcClient.live,
      DocumentService.layer,
      CustomerAppConfig.layer
    )
}