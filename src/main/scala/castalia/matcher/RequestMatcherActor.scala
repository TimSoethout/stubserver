package castalia.matcher

import akka.actor.{ActorLogging, Props, ActorRef, Actor}
import akka.http.scaladsl.model.HttpRequest
import castalia.matcher.MatcherActor.RespondIfMatched
import castalia.matcher.RequestMatcherActor.{FindMatchAndForward, AddMatcher}

/**
  * Created by m06f791 on 4-2-2016.
  */

object RequestMatcherActor {
  case class FindMatchAndForward(httpRequest: HttpRequest, origin: ActorRef)
  case class AddMatcher(matcherActor: ActorRef)
  def props(): Props = Props[RequestMatcherActor]
}

class RequestMatcherActor extends Actor with ActorLogging {

  def receive: Receive = normal(Set.empty[ActorRef])

  def normal(matchers: Set[ActorRef]): Receive = {
    case AddMatcher(matcher) => {
      log.debug(s"Added matcher ${matcher} to RequestMatcherActor ${self.toString()}")
      context.become(normal(matchers + matcher))
    }
    case FindMatchAndForward(httpRequest, origin) => {
      val parsedUri = new ParsedUri(httpRequest.uri.toString().replace(';', '&'),httpRequest.uri.path,  httpRequest.uri.query().toList)
      log.debug(s"RequestMatcherActor received http request ${parsedUri}")

      val gatherer = context.actorOf(MatchResultGatherer.props(matchers.size, origin))

      matchers.foreach(matchers => matchers forward RespondIfMatched(parsedUri, httpRequest, gatherer))
    }
  }
}