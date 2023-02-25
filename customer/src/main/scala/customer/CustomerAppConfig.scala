package customer

import document.infrastructure.DocumentServiceContainer
import zio.{Layer, ZLayer}
import zio.config.{PropertyTreePath, ReadError, toKebabCase}
import zio.config.magnolia.descriptor
import zio.config.typesafe.TypesafeConfigSource

case class CustomerAppConfig(
                              documentGrpcHost: String,
                              documentGrpcPort: Int,
)

object CustomerAppConfig {

  implicit val customerAppConfig  = descriptor[CustomerAppConfig]

  val layer: Layer[ReadError[String], CustomerAppConfig] =
    ZLayer {
      zio.config.read {
        descriptor[CustomerAppConfig].from(
          TypesafeConfigSource.fromResourcePath
            .at(PropertyTreePath.$(keyMapper("CustomerAppConfig")))
        )
          .mapKey(keyMapper)
      }
    }

  def from(documentServiceContainer: DocumentServiceContainer): CustomerAppConfig =
    CustomerAppConfig(
      documentServiceContainer.host,
      documentServiceContainer.externalPort,
    )

  private lazy val keyMapper = toKebabCase
}
