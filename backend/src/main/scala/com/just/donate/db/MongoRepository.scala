package com.just.donate.db

import cats.effect.IO
import org.mongodb.scala.*

object MongoOps:
  implicit class ObservableOps[T](val observable: Observable[T]) extends AnyVal:
    def toIO: IO[Seq[T]] = IO.fromFuture(IO(observable.toFuture()))

trait MongoRepository[T, K](collection: MongoCollection[Document]) extends Repository[T, K] {}
