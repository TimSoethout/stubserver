package castalia.actors

import akka.http.scaladsl.model.StatusCodes.Forbidden

import akka.actor._
import castalia.EndpointIds
import castalia.matcher.RequestMatch
import castalia.matcher.types.Params
import castalia.model.Model._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

case class DelayComplete( destination: ActorRef, message: StubResponse)

/**
  * Actor that provides answers based on the json configuration that is used to create this actor
  *
  * Created by Jean-Marc van Leerdam on 2016-01-16
  */
class JsonEndpointActor( myStubConfig: StubConfig) extends Actor with ActorLogging {

  override def receive: Receive = {
    case request: RequestMatch =>
      // see if there is a response available for the parameters in the request
        val responseOption = findResponse(request.pathParams)

        responseOption match {
          case Some(response) =>
            (response.response, response.delay) match {
              case (Some(content), Some(delay)) =>
                self ! new DelayedResponse(sender, new StubResponse( response.httpStatusCode, content.toJson.toString()), delay)
              case (Some(content), _) =>
                sender ! new StubResponse( response.httpStatusCode, content.toJson.toString)
              case (_, Some(delay)) =>
                self ! new DelayedResponse(sender, new StubResponse( response.httpStatusCode, ""), delay)
              case (_, _) =>
                sender ! new StubResponse(response.httpStatusCode, "")
            }
          case _ =>
            sender ! new StubResponse( Forbidden.intValue, Forbidden.reason)
        }

    case delayedResponse: DelayedResponse =>
      context.system.scheduler.scheduleOnce(calculateDelayTime(delayedResponse.delay), self, new DelayComplete( delayedResponse.destination, delayedResponse.response))


    case delayComplete: DelayComplete =>
      delayComplete.destination ! delayComplete.message

    case x: Any =>
      log.debug("receive unexpected message [" + x + "]")
  }

  def findResponse( pathParams: Params): Option[ResponseConfig] = {
    def findResponseRecurse( pathParams: Params, responses: List[ResponseConfig]): Option[ResponseConfig] =
      (pathParams, responses) match {
        case (_, Nil) => None
        case (pathParams, first :: rest) => if (paramMatch(pathParams, first.ids)) Some(first) else findResponseRecurse( pathParams, rest)
        case (_, _) => None
      }
    findResponseRecurse(pathParams, myStubConfig.responses)
  }

  def paramMatch( left: Params, right: EndpointIds): Boolean = {
    (left, right) match {
      case (Nil, None) => true
      case (left, None) => false
      case (Nil, Some(right)) => false
      case (left, Some(right)) => left.toMap == right
    }
  }

  def calculateDelayTime( latencyConfig: LatencyConfig): FiniteDuration = {
    (latencyConfig.duration, latencyConfig.duration.isFinite()) match {
      case (duration, true) => FiniteDuration(duration.length, duration.unit)
      case (_, _) => FiniteDuration(10, MILLISECONDS)
    }
  }

}
