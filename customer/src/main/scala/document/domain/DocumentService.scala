package document.domain

import zio.stream.{Stream, UStream}
import zio.{Task, URLayer, ZIO, ZLayer}

case class DocumentService(repository: DocumentRepository) {
  def createDocument(document: Document): Task[Document] =
    repository.createDocument(document)

  def getDocuments(request: UStream[DocumentId]): Stream[Throwable, Document] =
    repository.getDocuments(request)
}

object DocumentService {
  val layer: URLayer[DocumentRepository, DocumentService] = ZLayer.fromZIO {
    for {
      repository <- ZIO.service[DocumentRepository]
    } yield DocumentService(repository)
  }

}