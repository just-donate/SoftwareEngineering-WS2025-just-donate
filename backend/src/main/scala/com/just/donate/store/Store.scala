package com.just.donate.store

import cats.effect.IO
import com.just.donate.models.Organisation

trait Store:

  def init(): Unit

  def save(id: String, organisation: Organisation): IO[Unit]

  def load(id: String): IO[Option[Organisation]]

  def list(): IO[List[String]]

  def delete(id: String): IO[Unit]
