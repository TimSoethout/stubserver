package castalia

import akka.actor.{Props, ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes._
import akka.testkit.TestActor.{NoAutoPilot, AutoPilot}
import akka.testkit.{TestActor, TestProbe}
import castalia.management.{Manager, ManagerService}
import castalia.model.Messages.{UpsertResponse, Done, UpsertEndpoint}
import castalia.model.Model.{EndpointResponseConfig, ResponseConfig, StubConfig}

class ManagerServiceSpec extends ServiceSpecBase with SprayJsonSupport {

  val receptionistMock = TestProbe()
  val actor = system.actorOf(Manager.props(receptionistMock.ref))
  val parentSystem = system
  val service = new ManagerService {
    override protected def managerActor: ActorRef = actor

    override protected implicit val system: ActorSystem = parentSystem
  }

  "posting new endpoint" should {
    "forward Upsert to Receptionist and eventually result in status HTTP 200" in {
      val stubConfig = new StubConfig("my/endpoint", List(ResponseConfig(None, None, OK.intValue, None)))

      receptionistMock.setAutoPilot(new AutoPilot {
        override def run(sender: ActorRef, msg: Any): AutoPilot = msg match{
          case UpsertEndpoint(config) => sender ! Done(config.endpoint)
            NoAutoPilot
        }
      })

      Post("/castalia/manager/endpoints", stubConfig) ~> service.managementRoute ~> check {
        status shouldBe OK
        responseAs[String] shouldBe stubConfig.endpoint
      }
    }
  }

  "posting new endpoint response" should {
    "forward Upsert to Receptionist and eventually result in status HTTP 200" in {
      val responseConfig = new EndpointResponseConfig("my/endpoint", ResponseConfig(None, None, OK.intValue, None))

      receptionistMock.setAutoPilot(new AutoPilot {
        override def run(sender: ActorRef, msg: Any): AutoPilot = msg match{
          case UpsertResponse(config) => sender ! Done(config.endpoint)
            NoAutoPilot
        }
      })

      Post("/castalia/manager/endpoints/responses", responseConfig) ~> service.managementRoute ~> check {
        status shouldBe OK
        responseAs[String] shouldBe responseConfig.endpoint
      }
    }
  }
}
