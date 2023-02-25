package document.domain

import document.infrastructure.{DocumentGrpcRepository, DocumentInMemoryRepository}
import io.grpc.ManagedChannelBuilder
import io.grpc.document.document.ZioDocument.DocumentGrpcServiceClient
import scalapb.zio_grpc.ZManagedChannel
import zio.{Layer, Ref, Task, ULayer, ZIO, ZLayer}
import zio.stream.{Stream, UStream}

trait DocumentRepository {
  def createDocument(document: Document): Task[Document]

  def getDocuments(request: UStream[DocumentId]): Stream[Throwable, Document]
}