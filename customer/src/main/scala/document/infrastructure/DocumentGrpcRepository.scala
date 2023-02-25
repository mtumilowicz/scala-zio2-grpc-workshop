package document.infrastructure

import document.domain
import document.domain.{Document, DocumentId, DocumentRepository}
import io.grpc.document.document.ZioDocument.DocumentGrpcServiceClient
import io.grpc.document.document.{DocumentIdApiInput, NewDocumentApiInput}
import zio.Task
import zio.stream.{Stream, UStream}

case class DocumentGrpcRepository(client: DocumentGrpcServiceClient) extends DocumentRepository {
  override def createDocument(document: Document): Task[Document] =
    client.createDocument(NewDocumentApiInput(document.id.raw, document.content))
      .mapBoth(_.getCause, _ => document)

  override def getDocuments(request: UStream[DocumentId]): Stream[Throwable, Document] =
    client.getDocuments(request.map(di => DocumentIdApiInput(di.raw)))
      .mapBoth(_.getCause, d => domain.Document(DocumentId(d.id), d.payload))
}