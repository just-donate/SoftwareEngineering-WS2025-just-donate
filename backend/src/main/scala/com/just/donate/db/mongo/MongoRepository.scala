package com.just.donate.db.mongo

import cats.effect.IO
import com.just.donate.db.Repository
import org.mongodb.scala.*

object MongoRepository:
  implicit class ObservableOps[E](val observable: Observable[E]) extends AnyVal:
    def toIO: IO[Seq[E]] = IO.fromFuture(IO(observable.toFuture()))

trait MongoRepository[K, E](collection: MongoCollection[Document]) extends Repository[K, E] {}
