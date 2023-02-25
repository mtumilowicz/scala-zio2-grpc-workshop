package document.infrastructure;

import com.dimafeng.testcontainers.GenericContainer
import zio._

case class DocumentServiceContainer(port: Int, underlying: GenericContainer) extends GenericContainer(underlying) {

  lazy val externalPort = mappedPort(port)

}

object DocumentServiceContainer {
  private val imageName   = "document-service:latest"
  private val exposedPort = 9000

  case object Def
    extends GenericContainer.Def[DocumentServiceContainer](
      new DocumentServiceContainer(
        exposedPort,
        GenericContainer(
          dockerImage = imageName,
          exposedPorts = Seq(exposedPort),
        ),
      ),
    )

  val live: ZLayer[Scope, Throwable, DocumentServiceContainer] = ZLayer.fromZIO {
    ZIO
      .succeed(DocumentServiceContainer.Def)
      .flatMap(
        container =>
          ZIO.acquireRelease(ZIO.attemptBlocking(container.start()))(a => ZIO.attemptBlocking(a.stop()).orDie)
      )
  }
}