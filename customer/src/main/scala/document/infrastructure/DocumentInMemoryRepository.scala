package document.infrastructure

import document.domain.{Document, DocumentId, DocumentRepository}
import zio.stream.{Stream, UStream}
import zio.{Ref, Task, ZIO}

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