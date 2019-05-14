package ru.tinkoff.fintech.homework09.monix.crawler

import java.util.UUID

import monix.eval.{Fiber, Task}
import monix.execution.Scheduler
import ru.tinkoff.fintech.homework09.crawler.Url

trait Worker {
  def http: Http

  def parseLinks: Parsr

  val ioCommon = Scheduler.io("common")
  val ioHttp = Scheduler.io("http")

  def worker(workerQueue: MQueue[Url], crawlerQueue: MQueue[CrawlerMessage]): Task[Fiber[Unit]] = {
//    val url = Url("host", "path")

    def body = for {
      _ <- Task.delay(println(s"${Thread.currentThread().getName} before fetch"))
//      url =
      url <- workerQueue.take
      body <- http.get(url)
      _ <- Task.delay(println(s"${Thread.currentThread().getName} fetched url ->"))
      _ <- crawlerQueue.offer(CrawlResult(url, body))
//      _ <- crawlerQueue.offer(CrawlResult(Url("", ""), List(url.copy(path = UUID.randomUUID().toString))))
    } yield ()

    def loop = body.executeOn(ioHttp).restartUntil(_ => false)

    val fiberTask = loop.start
    fiberTask
  }
}
