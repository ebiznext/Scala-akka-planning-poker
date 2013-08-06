package models

import akka.actor.actorRef2Scala
import akka.actor.Actor
import akka.actor.ActorLogging
import java.util.UUID

class UniqueTokenMgr extends Actor with ActorLogging {
  def receive = {
    case GetUniqueToken => sender ! UniqueToken(UUID.randomUUID().toString())
    case _ => println("received unknown message")
  }
  override def preStart() = {
    println("Unique Mgr prestart")
  }
}
