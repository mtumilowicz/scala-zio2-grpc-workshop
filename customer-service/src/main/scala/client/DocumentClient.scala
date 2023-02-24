package client

import zio.Layer
import io.grpc.ManagedChannelBuilder
import io.grpc.document.document.{DocumentApiOutput, DocumentIdApiInput, NewDocumentApiInput}
import io.grpc.document.document.ZioDocument.DocumentGrpcServiceClient
import zio.Console._
import scalapb.zio_grpc.ZManagedChannel
import zio._
import zio.stream.{Stream, UStream, ZStream}

case class DocumentId(raw: String)

case class Document(id: DocumentId, content: String)

trait DocumentRepository {
  def createDocument(document: Document): Task[Document]
  def getDocuments(request: UStream[DocumentId]): Stream[Throwable, Document]
}

object DocumentRepository {
  val clientLayer: Layer[Throwable, DocumentGrpcServiceClient] =
    DocumentGrpcServiceClient.live(
      ZManagedChannel(
        ManagedChannelBuilder.forAddress("localhost", 9000).usePlaintext()
      )
    )
  val grpc: ZLayer[DocumentGrpcServiceClient, Throwable, DocumentRepository] = ZLayer.fromZIO {
    for {
      client <- ZIO.service[DocumentGrpcServiceClient]
    } yield DocumentGrpcRepository(client)
  }

  val inMemory: ULayer[DocumentRepository] = ZLayer.fromZIO {
    for {
      map <- Ref.make(Map.empty[DocumentId, Document])
    } yield DocumentInMemoryRepository(map)
  }
}

case class DocumentClientService(repository: DocumentRepository) {
  def createDocument(document: Document): Task[Document] =
    repository.createDocument(document)

  def getDocuments(request: UStream[DocumentId]): Stream[Throwable, Document] =
    repository.getDocuments(request)
}

object DocumentClientService {
  val layer: URLayer[DocumentRepository, DocumentClientService] = ZLayer.fromZIO {
    for {
      repository <- ZIO.service[DocumentRepository]
    } yield DocumentClientService(repository)
  }

}

case class DocumentInMemoryRepository(storage: Ref[Map[DocumentId, Document]]) extends DocumentRepository {

  override def createDocument(document: Document): Task[Document] = {
    storage.update(_.updatedWith(document.id) {
      case Some(_) => return ZIO.fail(new RuntimeException("Document already exist"))
      case None => Some(document)
    }) *> ZIO.succeed(document)
  }

  override def getDocuments(request: UStream[DocumentId]): Stream[Throwable, Document] =
    request
    .mapZIO(id => storage.get.map(_.get(id)))
    .mapZIO {
      case Some(value) => ZIO.succeed(value)
      case None => ZIO.fail(new RuntimeException("Document not found"))
    }
}

case class DocumentGrpcRepository(client: DocumentGrpcServiceClient) extends DocumentRepository {
  override def createDocument(document: Document): Task[Document] =
    client.createDocument(NewDocumentApiInput(document.id.raw, document.content))
      .mapBoth(_.getCause, _ => document)

  override def getDocuments(request: UStream[DocumentId]): Stream[Throwable, Document] =
    client.getDocuments(request.map(di => DocumentIdApiInput(di.raw)))
      .mapBoth(_.getCause, d => Document(DocumentId(d.id), d.payload))
}