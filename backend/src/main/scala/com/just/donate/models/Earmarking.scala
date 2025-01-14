package com.just.donate.models

case class Earmarking (id: String, name: String, description: String, location: (Long, Long) = (0, 0))

object Earmarking:
  
  def apply(name: String, description: String): Earmarking = 
    val id = name.toLowerCase().replace(" ", "-")
    Earmarking(id, name, description)