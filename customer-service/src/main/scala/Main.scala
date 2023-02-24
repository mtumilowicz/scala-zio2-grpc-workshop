import customer.{CustomerId, CustomerService}
import document.domain.{Document, DocumentId, DocumentRepository, DocumentService}
import zio.Console.printLine
import zio.ZIO
import zio.stream.ZStream

object Main extends zio.ZIOAppDefault {

  def createDocument() = {
    val request = Document(DocumentId("1"), "blob1")
    println(s"[unary] create document request $request")

    for {
      reply <- ZIO.serviceWithZIO[CustomerService](_.createDocument(CustomerId("1"), request))
      _ <- printLine(s"[unary] create document reply $reply")
    } yield reply
  }

  def streamDocuments() = {
    val documents = List(
      ("1", "blob1"),
    )

    val replyStream = for {
      reply <- ZStream.serviceWithStream[CustomerService](_.findAllDocuments(CustomerId("1")))
    } yield reply

    replyStream.foreach(r => printLine(s"[bi-stream] document reply $r"))
  }

  def myAppLogic =
    for {
      _ <- createDocument()
      _ <- streamDocuments()
    } yield ()

  final def run =
    myAppLogic.provide(
      CustomerService.layer,
      DocumentRepository.grpc,
      DocumentRepository.grpcClient,
      DocumentService.layer,
    )
}