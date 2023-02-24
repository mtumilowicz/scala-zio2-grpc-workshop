import client.{Document, DocumentClientService, DocumentId, DocumentRepository}
import zio.Console.printLine
import zio.ZIO
import zio.stream.ZStream

object Main extends zio.ZIOAppDefault {

  def createDocument() = {
    val request = Document(DocumentId("1"), "blob1")
    println(s"[unary] create document request $request")

    for {
      reply <- ZIO.serviceWithZIO[DocumentClientService](_.createDocument(request))
      _ <- printLine(s"[unary] create document reply $reply")
    } yield reply
  }

  def streamDocuments() = {
    val documents = List(
      ("1", "blob1"),
    )

    val replyStream = for {
      reply <- ZStream.serviceWithStream[DocumentClientService](_.getDocuments(
        ZStream.fromIterable(
          documents.map(d => DocumentId(d._1))
        ).tap { r =>
          printLine(s"[bi-stream] document request $r").orDie
        }
      ))
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
      DocumentClientService.layer,
      DocumentRepository.grpc,
      DocumentRepository.clientLayer
    )
}