package com.just.donate.utils

import com.just.donate.utils.Split

trait Splittable[T, S]:

  def splitOf(s: S): Split[T, S]
