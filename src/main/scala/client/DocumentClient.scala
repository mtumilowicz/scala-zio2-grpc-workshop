package client

import zio.Layer
import io.grpc.ManagedChannelBuilder
import io.grpc.document.document.{DocumentIdApiInput, NewDocumentApiInput}
import io.grpc.document.document.ZioDocument.DocumentGrpcServiceClient
import zio.Console._
import scalapb.zio_grpc.ZManagedChannel
import zio._
import zio.stream.ZStream

object DocumentClient extends zio.ZIOAppDefault {
  val clientLayer: Layer[Throwable, DocumentGrpcServiceClient] =
    DocumentGrpcServiceClient.live(
      ZManagedChannel(
        ManagedChannelBuilder.forAddress("localhost", 9000).usePlaintext()
      )
    )

  def createDocument() = {
    val request = NewDocumentApiInput("1", "blob1")
    println(s"[unary] create document request $request")

    for {
      reply <- DocumentGrpcServiceClient.createDocument(request)
      _ <- printLine(s"[unary] create document reply $reply")
    } yield reply
  }

  def streamDocuments() = {
    val documents = List(
      ("1", "blob1"),
    )

    val replyStream = for {
      reply <- DocumentGrpcServiceClient.getDocuments(
        ZStream.fromIterable(
          documents.map(d => DocumentIdApiInput(d._1))
        ).tap { r =>
          printLine(s"[bi-stream] document request $r").orDie
        }
      )
    } yield reply

    replyStream.foreach(r => printLine(s"[bi-stream] document reply $r"))
  }


  def myAppLogic =
    for {
      _ <- createDocument()
      _ <- streamDocuments()
    } yield ()

  final def run =
    myAppLogic.provideLayer(clientLayer)
}