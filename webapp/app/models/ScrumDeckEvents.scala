package models

import CardType._
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.JsValue
import play.api.libs.iteratee.PushEnumerator
import play.api.libs.json._
import play.api.libs.functional.syntax._

object CardType  {
  val zero = "0"
  val half = "1/2"
  val one = "1"
  val two = "2"
  val three = "3"
  val five = "5"
  val eight ="8"
  val thirteen = "13"
  val twenty = "20"
  val forty = "40"
  val hundred = "100" 
  val question = "?"
  val coffee = "coffee"
  type CardType = String 
}

sealed trait ScrumDeckEvent
sealed trait RoomMgrEvent
sealed trait TokenMgrEvent

// Token dispenser
case class GetUniqueToken() extends TokenMgrEvent

// Unique Token returned as String
case class UniqueToken(value: String, timestamp: Long = System.currentTimeMillis()) extends TokenMgrEvent

case class AddRoom(owner: String) extends ScrumDeckEvent
case class GetRoomByOwner(owner: String) extends ScrumDeckEvent
case class RemoveRoom(owner: String) extends ScrumDeckEvent
case class ParticipantRooms(participantId: String) extends ScrumDeckEvent

case class InviteParticipant(participantId: String) extends RoomMgrEvent
case class RoomConnect(participantId: String, channel : PushEnumerator[JsValue]) extends RoomMgrEvent
case class RevokeParticipant(participantId: String) extends RoomMgrEvent
case object ListParticipants extends RoomMgrEvent
case object GetRoomOwner extends RoomMgrEvent
case class ContainsParticipant(participantId: String) extends RoomMgrEvent

case object StartNewVoteSession extends RoomMgrEvent
case object StopCurrentVoteSession extends RoomMgrEvent
case class SubmitVote(participantId: String, card: CardType) extends RoomMgrEvent

case object CollectVotes extends ScrumDeckEvent
case class Votes(votes: List[Vote]) extends ScrumDeckEvent
case class Vote(val email: String, val card: CardType, val voteTime: Long = System.currentTimeMillis())

object Vote {
  implicit val voteFormatter = Json.writes[Vote]
}
