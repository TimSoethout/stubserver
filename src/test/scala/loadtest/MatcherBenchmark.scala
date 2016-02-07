package loadtest

import akka.actor.ActorRef
import akka.http.scaladsl.model.HttpRequest
import castalia.matcher.Matcher
import org.scalameter.api._

/**
  * Created by m06f791 on 4-2-2016.
  */
object MatcherBenchmark extends Bench.LocalTime {
/* TODO refactor to use actor based request matching
  val sizes = Gen.range("size")(100, 10000, 9900)

  val ranges = for {
    size <- sizes
  } yield 0 until size

  var matcher: RequestMatcher = new RequestMatcher(Nil)
  var endpoints = List(
    List("castalia", "endpoint0", "$param0", "blabla", "endpoint0", "$param0", "blabla", "endpoint0", "$param0", "blabla"),
    List("castalia", "endpoint1", "$param1", "blabla", "endpoint1", "$param1", "blabla", "endpoint1", "$param1", "blabla"),
    List("castalia", "endpoint2", "$param2", "blabla", "endpoint2", "$param2", "blabla", "endpoint2", "$param2", "blabla"),
    List("castalia", "endpoint3", "$param3", "blabla", "endpoint3", "$param3", "blabla", "endpoint3", "$param3", "blabla"),
    List("castalia", "endpoint4", "$param4", "blabla", "endpoint4", "$param4", "blabla", "endpoint4", "$param4", "blabla"))

  for (endpoint <- endpoints) {
    matcher = matcher.addOrReplaceMatcher(new Matcher(endpoint, ActorRef.noSender))
  }

  val httpRequests = (for (i <- 0 until 5) yield (i, HttpRequest(uri = s"castalia/endpoint${i}/${i*i}/blabla/endpoint${i}/${i*i}/blabla/endpoint${i}/${i*i}/blabla"))).toMap

  performance of "Range" in {
    measure method "map" in {
      using(ranges) in {
        r => r.foreach( i => matcher.matchRequest(httpRequests(i % 5)))
      }
    }
  }*/
}