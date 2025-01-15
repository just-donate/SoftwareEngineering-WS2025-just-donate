package com.just.donate.notify.messages;

trait MessageType:
  val defaultTemplate: String
  val replacements: Seq[(String, String)]
