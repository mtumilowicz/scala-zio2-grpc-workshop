import scalapb.zio_grpc.{ServerMain, ServiceList}
import server.service.DocumentService

object Main  extends ServerMain {

  override def port: Int = 8080

  override def services: ServiceList[Any] = ServiceList.addFromEnvironment[DocumentService].provideLayer(DocumentService.live)
}