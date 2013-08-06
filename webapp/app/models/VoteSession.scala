package models

import CardType._

case class VoteSession(var votes: Map[String, Vote] = Map(), val startTime: Long = System.currentTimeMillis(), var endTime: Option[Long] = None) {
  def submit(email: String, card: CardType) = if (isActive) votes += (email -> Vote(email, card)) else throw new IllegalAccessException("no active session")
  def collectVotes: List[Vote] = votes.values.toList
  def isActive: Boolean = endTime.isEmpty
}
