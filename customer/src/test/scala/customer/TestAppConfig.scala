package customer

import document.infrastructure.DocumentServiceContainer
import zio.{URLayer, ZIO, ZLayer}

object TestAppConfig {

  val live: URLayer[DocumentServiceContainer, CustomerAppConfig] = ZLayer.fromZIO {
    for {
      documentServiceContainer <- ZIO.service[DocumentServiceContainer]
    } yield CustomerAppConfig(
      documentServiceContainer.host,
      documentServiceContainer.externalPort,
    )
  }
}
