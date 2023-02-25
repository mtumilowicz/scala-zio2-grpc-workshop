package document.infrastructure

import document.domain.{Document, DocumentId, DocumentRepository}
import io.grpc.document.document.ZioDocument.DocumentGrpcServiceClient
import zio.{Ref, ULayer, ZIO, ZLayer}

object DocumentRepositoryConfig {

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
