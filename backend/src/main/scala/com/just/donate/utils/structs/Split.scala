package com.just.donate.utils.structs

case class Split[T, S](split: Option[T] = None, remain: Option[T] = None, open: Option[S] = None)
