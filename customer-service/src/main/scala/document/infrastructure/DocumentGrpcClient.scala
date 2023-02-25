package document.infrastructure

import io.grpc.ManagedChannelBuilder
import io.grpc.document.document.ZioDocument.DocumentGrpcServiceClient
import scalapb.zio_grpc.ZManagedChannel
import zio.{Layer, RLayer, ZIO, ZLayer}

object DocumentGrpcClient {

  case class Config(port: Int)

  val live: ZLayer[Config, Throwable, DocumentGrpcServiceClient] = ZLayer.fromZIO {
    for {
      config <- ZIO.service[Config]
    } yield DocumentGrpcServiceClient.live(
      ZManagedChannel(
        ManagedChannelBuilder.forAddress("localhost", config.port).usePlaintext()
      )
    )
  }.flatten

}
