package com.jplfds.chat

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{Behaviors, LoggerOps}

object Gabbler {
  def apply(): Behavior[SessionEvent] =
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        case SessionGranted(handle) =>
          handle ! PostMessage("Hello World!")
          Behaviors.same
        case MessagePosted(screenName, message) =>
          context.log.info2("message has been posted by '{}': {}", screenName, message)
          Behaviors.stopped
        case SessionDenied(reason) =>
          context.log.info("they won't let me in because {}", reason)
          Behaviors.stopped
      }
    }
}
