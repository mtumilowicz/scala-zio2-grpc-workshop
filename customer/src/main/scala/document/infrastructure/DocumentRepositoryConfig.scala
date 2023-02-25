package document.infrastructure

import document.domain.{Document, DocumentId, DocumentRepository}
import document.infrastructure.{DocumentGrpcRepository, DocumentInMemoryRepository}
import io.grpc.ManagedChannelBuilder
import io.grpc.document.document.ZioDocument.DocumentGrpcServiceClient
import scalapb.zio_grpc.ZManagedChannel
import zio.{Layer, Ref, Task, ULayer, ZIO, ZLayer}
import zio.stream.{Stream, UStream}

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
