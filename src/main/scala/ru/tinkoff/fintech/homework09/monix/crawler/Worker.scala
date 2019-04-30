package ru.tinkoff.fintech.homework09.monix.crawler

import monix.eval.{Fiber, Task}

trait Worker {
  def http: Http

  def parseLinks: Parsr

  def worker(workerQueue: MQueue[Url], crawlerQueue: MQueue[CrawlerMessage]): Task[Fiber[Unit]] = {
    val task = for {
      url <- workerQueue.take
      body <- http.get(url)
      _ = println(s"${Thread.currentThread().getName} fetched $url → $body")
      _ <- crawlerQueue.offer(CrawlResult(url, body))
    } yield ()

    val fiberTask = task.restartUntil(_ => false).start
    fiberTask
  }
}
