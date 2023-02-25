package customer

import document.domain.{Document, DocumentId, DocumentService}
import zio.stream.ZStream
import zio.{Task, URLayer, ZIO, ZLayer, stream}

case class CustomerService(documentService: DocumentService) {

  def createDocument(customerId: CustomerId, document: Document): Task[Document] =
    documentService.createDocument(document)

  def findAllDocuments(customerId: CustomerId): stream.Stream[Throwable, Document] =
    documentService.getDocuments(ZStream.from(DocumentId("1")))
}

object CustomerService {
  val layer: URLayer[DocumentService, CustomerService] = ZLayer.fromZIO {
    for {
      repository <- ZIO.service[DocumentService]
    } yield CustomerService(repository)
  }

}
