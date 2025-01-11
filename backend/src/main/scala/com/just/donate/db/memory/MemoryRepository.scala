package com.just.donate.db.memory

import com.just.donate.db.Repository

import scala.collection.mutable

trait MemoryRepository[K, E](repository: mutable.Map[K, E]) extends Repository[K, E]
