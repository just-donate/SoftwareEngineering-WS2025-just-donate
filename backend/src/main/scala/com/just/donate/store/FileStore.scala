package com.just.donate.store

import com.just.donate.models.Organisation
import io.circe.generic.auto._, io.circe.syntax._

object FileStore:
  
  def save(organisation: Organisation): Unit =
    println(s"Saving organisation: ${organisation.name}")
    
  def load(organisation: String): Option[Organisation] =
    println(s"Loading organisation: $organisation")
    None
