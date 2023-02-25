package server.service

import io.grpc.Status
import io.grpc.document.document.ZioDocument.DocumentGrpcService
import io.grpc.document.document.{DocumentApiOutput, DocumentIdApiInput, NewDocumentApiInput}
import zio.{IO, Ref, ULayer, ZIO, ZLayer, stream}

case class DocumentId(raw: String)

case class Document(id: DocumentId, content: String)


case class DocumentService(storage: Ref[Map[DocumentId, Document]]) extends DocumentGrpcService {
  override def createDocument(request: NewDocumentApiInput): IO[Status, NewDocumentApiInput] = {
    val newDocumentId = DocumentId(request.id)
    storage.update(_.updatedWith(newDocumentId) {
      case Some(_) => return ZIO.fail(Status.ALREADY_EXISTS)
      case None => Some(Document(newDocumentId, request.payload))
    }) *> ZIO.succeed(NewDocumentApiInput(newDocumentId.raw))
  }

  override def getDocuments(request: stream.Stream[Status, DocumentIdApiInput]): stream.Stream[Status, DocumentApiOutput] =
    request.map(_.id)
      .map(DocumentId)
      .mapZIO(id => storage.get.map(_.get(id)))
      .mapZIO {
        case Some(value) => ZIO.succeed(value)
        case None => ZIO.fail(Status.NOT_FOUND)
      }
      .map(d => DocumentApiOutput(d.id.raw, d.content))
}

object DocumentService {
  def live: ULayer[DocumentService] = ZLayer.fromZIO {
    for {
      map <- Ref.make(Map.empty[DocumentId, Document])
    } yield DocumentService(map)
  }
}