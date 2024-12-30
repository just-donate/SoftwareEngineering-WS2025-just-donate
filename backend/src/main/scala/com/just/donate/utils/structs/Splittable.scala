package com.just.donate.utils.structs

trait Splittable[T, S]:

  def splitOf(s: S): Split[T, S]
