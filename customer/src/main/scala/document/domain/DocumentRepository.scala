package document.domain

import zio.Task
import zio.stream.{Stream, UStream}

trait DocumentRepository {
  def createDocument(document: Document): Task[Document]

  def getDocuments(request: UStream[DocumentId]): Stream[Throwable, Document]
}