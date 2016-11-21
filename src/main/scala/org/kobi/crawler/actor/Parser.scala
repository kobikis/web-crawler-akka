package org.kobi.crawler.actor

import akka.actor.{Actor, ActorRef}
import org.jsoup.Jsoup

import scala.collection.JavaConverters._
import scala.language.postfixOps


class Parser(manager: ActorRef) extends Actor {
  val baseUrl = "http://localhost:8080"

  def receive: Receive = {
    case Parse(url) =>
//      println("Parsing " + url)

      val links = getLinks(url)
      if(links.size == 1){
        sender() ! Stop
      }else{
        sender() ! links
      }
  }

  def getLinks(url: Url): List[Url] = {
    val response = Jsoup.connect(url.url).ignoreContentType(true).execute()
    val doc = response.parse()
    doc.getElementsByTag("a")
      .asScala
      .map(e => e.attr("href"))
      .map(link => new Url(baseUrl + link))
      .toList
  }

}
