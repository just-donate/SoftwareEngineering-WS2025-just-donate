package com.just.donate.helper

import org.http4s.Uri

object TestHelper:

  def testUri(paths: String*): Uri = Uri.unsafeFromString(paths.mkString("/"))
