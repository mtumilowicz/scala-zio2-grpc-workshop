package document.infrastructure

import customer.CustomerAppConfig
import io.grpc.ManagedChannelBuilder
import io.grpc.document.document.ZioDocument.DocumentGrpcServiceClient
import scalapb.zio_grpc.ZManagedChannel
import zio.{Layer, RLayer, ZIO, ZLayer}

object DocumentGrpcClient {
  val live: ZLayer[CustomerAppConfig, Throwable, DocumentGrpcServiceClient] = ZLayer.fromZIO {
    for {
      config <- ZIO.service[CustomerAppConfig]
    } yield DocumentGrpcServiceClient.live(
      ZManagedChannel(
        ManagedChannelBuilder.forAddress(config.documentGrpcHost, config.documentGrpcPort).usePlaintext()
      )
    )
  }.flatten

}
