import io.grpc.Server
import io.grpc.document.document.NewDocumentApiInput
import io.grpc.document.document.ZioDocument.DocumentGrpcServiceClient
import scalapb.zio_grpc.{ServerLayer, ServerMain, ServiceList}
import server.service.DocumentService
import zio._
import zio.Console.printLine

object Main  extends ServerMain {
  override def services: ServiceList[Any] = ServiceList.addFromEnvironment[DocumentService].provideLayer(DocumentService.live)
}