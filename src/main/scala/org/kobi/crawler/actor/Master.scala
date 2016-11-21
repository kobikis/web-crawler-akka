package org.kobi.crawler.actor

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorSystem, Props}
import akka.routing.RoundRobinPool

/**
  * Created by kobikis on 20/11/16.
  *
  * @since 12.0.1
  */
class Master(system: ActorSystem) extends Actor {


  var visitedLinks: ConcurrentHashMap[Url, Boolean] = new ConcurrentHashMap[Url, Boolean]()
  var counter: AtomicInteger = new AtomicInteger()
//  val parser = system.actorOf(Props(new Parser(self)))
  val max = math.pow(2,16) - 1

  //    val parser = system.actorOf(Props(new Parser(self))
  //      .withRouter(new RoundRobinPool(8)))
  //      .withDispatcher("my-dispacher"), "parserActors")
//
//  val parser = system.actorOf(Props(new Parser(self)).withDispatcher("my-thread-pool-dispatcher"))

  val parser = system.actorOf(Props(new Parser(self)).withRouter(new RoundRobinPool(4)).withDispatcher("my-dispatcher"))

  override def receive: Receive = {
    case Start(url : Url) =>
      visitedLinks.put(url, true)
      parser ! Parse(url)

    case urls: List[Url] =>
      for(url <- urls) {
        if (!visitedLinks.containsKey(url)) {
          parser ! Parse(url)
        }
        visitedLinks .put(url, true)
      }

    case Stop =>
      if(counter.incrementAndGet() == max){
        system.terminate()
      }

  }
}
