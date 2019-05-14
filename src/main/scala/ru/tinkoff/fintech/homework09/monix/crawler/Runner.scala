package ru.tinkoff.fintech.homework09.monix.crawler

import monix.eval.Task
import monix.execution.Scheduler
//import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Await
import scala.concurrent.duration._


object Runner extends App {
  val http: Http = new Http {
    val internet = Map[Url, Body](
      url("host0", "path0") -> List(url("host0", "path0"), url("host0", "path1"), url("host1", "path0")),
      url("host1", "path0") -> List(url("host1", "path1"), url("host2", "path0"), url("host3", "path0"))
    )

    override def get(url: Url): Task[Body] = Task(
      internet.getOrElse(url, List.empty)
    )
  }

  val parser: Parsr = (page: Body) => page

  implicit val scheduler: Scheduler = Scheduler.fixedPool("general",10)

  println(
    Await.result(new CrawlRoutines(http, parser)(scheduler).crawl(url("host0", "path0")).runToFuture, 10 seconds)
  )
}

class CrawlRoutines(
                     val http: Http,
                     val parseLinks: Parsr)(val sch: Scheduler) extends Worker with Manager {


  override implicit val scheduler: Scheduler = sch

  def crawl(crawlUrl: Url): Task[Map[Host, Int]] = {

    val crawlerQueue = MQueue.make[CrawlerMessage]
    for {
      _ <- crawlerQueue.offer(Start(crawlUrl))
      r <- crawler(crawlerQueue, CrawlerData(Map(), Set(), Set(), Map()))
    } yield r
  }
}