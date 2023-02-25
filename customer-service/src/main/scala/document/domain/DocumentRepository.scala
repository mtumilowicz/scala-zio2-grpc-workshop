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

object DocumentRepository {
  val grpcClient: Layer[Throwable, DocumentGrpcServiceClient] =
    DocumentGrpcServiceClient.live(
      ZManagedChannel(
        ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext()
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