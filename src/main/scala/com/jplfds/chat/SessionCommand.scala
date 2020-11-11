package com.jplfds.chat

trait SessionCommand
final case class PostMessage(message: String) extends SessionCommand
private final case class NotifyClient(message: MessagePosted) extends SessionCommand